package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class SelectPingTargets extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPingTargets.class);

	private static final Pattern endpointResouceTypes = Pattern.compile(
			"Endpoint|HealthcareService|ImagingStudy|InsurancePlan|Location|Organization|OrganizationAffiliation|PractitionerRole");

	private final OrganizationProvider organizationProvider;

	public SelectPingTargets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getLeadingTaskFromExecutionVariables(execution);
		task.addOutput(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY,
				execution.getBusinessKey()));
		updateLeadingTaskInExecutionVariables(execution, task);

		Stream<Endpoint> targetEndpoints = getTargetEndpointsSearchParameter(execution).map(this::searchForEndpoints)
				.orElse(allEndpointsNotLocal());

		Map<String, Identifier> organizationIdentifierByOrganizationId = getAllActiveOrganizations()
				.collect(Collectors.toMap(o -> o.getIdElement().getIdPart(),
						o -> o.getIdentifier().stream()
								.filter(i -> NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER.equals(i.getSystem()))
								.findFirst().get()));

		Stream<Endpoint> remoteTargetEndpointsWithActiveOrganization = targetEndpoints
				.filter(e -> getOrganizationIdentifier(e, organizationIdentifierByOrganizationId).isPresent());

		List<Target> targets = remoteTargetEndpointsWithActiveOrganization.map(e ->
		{
			String organizationIdentifier = getOrganizationIdentifier(e, organizationIdentifierByOrganizationId).get();
			String endpointIdentifier = getEndpointIdentifier(e).get();
			String endpointAddress = getEndpointAddress(e).get();
			return Target.createBiDirectionalTarget(organizationIdentifier, endpointIdentifier, endpointAddress,
					UUID.randomUUID().toString());
		}).collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(new Targets(targets)));
	}

	private Optional<UriComponents> getTargetEndpointsSearchParameter(DelegateExecution execution)
	{
		Task task = getLeadingTaskFromExecutionVariables(execution);
		return getTaskHelper()
				.getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_PING,
						CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS)
				.map(requestUrl -> UriComponentsBuilder.fromUriString(requestUrl).build());
	}

	private Stream<Endpoint> searchForEndpoints(UriComponents searchParameters)
	{
		return searchForEndpoints(searchParameters, 1, 0);
	}

	private Stream<Endpoint> searchForEndpoints(UriComponents searchParameters, int page, int currentTotal)
	{
		if (searchParameters.getPathSegments().isEmpty())
			return Stream.empty();

		Optional<Class<? extends Resource>> resourceType = getResourceType(searchParameters);
		if (resourceType.isEmpty())
			return Stream.empty();

		Map<String, List<String>> queryParameters = new HashMap<String, List<String>>();
		queryParameters.putAll(searchParameters.getQueryParams());
		queryParameters.put("_page", Collections.singletonList(String.valueOf(page)));

		Bundle searchResult = getFhirWebserviceClientProvider().getLocalWebserviceClient()
				.searchWithStrictHandling(resourceType.get(), queryParameters);

		if (searchResult.getTotal() > currentTotal + searchResult.getEntry().size())
			return Stream.concat(toEndpoints(searchResult),
					searchForEndpoints(searchParameters, page + 1, currentTotal + searchResult.getEntry().size()));
		else
			return toEndpoints(searchResult);
	}

	@SuppressWarnings("unchecked")
	private Optional<Class<? extends Resource>> getResourceType(UriComponents searchParameters)
	{
		if (searchParameters.getPathSegments().isEmpty())
			return Optional.empty();

		String type = searchParameters.getPathSegments().get(searchParameters.getPathSegments().size() - 1);
		if (!endpointResouceTypes.matcher(type).matches())
			return Optional.empty();

		try
		{
			return Optional.of((Class<? extends Resource>) Class.forName("org.hl7.fhir.r4.model." + type));
		}
		catch (ClassNotFoundException e)
		{
			logger.error("Unable to find class for FHIR resource type " + type, e);
			return Optional.empty();
		}
	}

	private Stream<Endpoint> allEndpointsNotLocal()
	{
		return allEndpoints(1, 0).filter(isLocalEndpoint().negate());
	}

	private Predicate<? super Endpoint> isLocalEndpoint()
	{
		return e -> Objects.equals(getFhirWebserviceClientProvider().getLocalBaseUrl(), e.getAddress());
	}

	private Stream<Endpoint> allEndpoints(int page, int currentTotal)
	{
		Bundle searchResult = getFhirWebserviceClientProvider().getLocalWebserviceClient()
				.searchWithStrictHandling(Endpoint.class, Map.of("status", Collections.singletonList("active"), "_page",
						Collections.singletonList(String.valueOf(page))));

		if (searchResult.getTotal() > currentTotal + searchResult.getEntry().size())
			return Stream.concat(toEndpoints(searchResult),
					allEndpoints(page + 1, currentTotal + searchResult.getEntry().size()));
		else
			return toEndpoints(searchResult);
	}

	private Stream<Endpoint> toEndpoints(Bundle searchResult)
	{
		Objects.requireNonNull(searchResult, "searchResult");

		return searchResult.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.filter(e -> e.getResource() instanceof Endpoint).map(e -> (Endpoint) e.getResource())
				.filter(Endpoint::hasStatus).filter(e -> EndpointStatus.ACTIVE.equals(e.getStatus()));
	}

	private Optional<String> getOrganizationIdentifier(Endpoint endpoint,
			Map<String, Identifier> organizationIdentifierByOrganizationId)
	{
		if (!endpoint.hasManagingOrganization() || !endpoint.getManagingOrganization().hasReferenceElement())
			return Optional.empty();

		return Optional
				.ofNullable(organizationIdentifierByOrganizationId
						.get(endpoint.getManagingOrganization().getReferenceElement().getIdPart()))
				.map(Identifier::getValue);
	}

	private Optional<String> getEndpointIdentifier(Endpoint endpoint)
	{
		return endpoint.getIdentifier().stream()
				.filter(i -> NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER.equals(i.getSystem())).findFirst()
				.map(Identifier::getValue);
	}

	private Optional<String> getEndpointAddress(Endpoint endpoint)
	{
		return endpoint.hasAddress() ? Optional.of(endpoint.getAddress()) : Optional.empty();
	}

	private Stream<Organization> getAllActiveOrganizations()
	{
		return getActiveOrganizations(1, 0);
	}

	private Stream<Organization> getActiveOrganizations(int page, int currentTotal)
	{
		Map<String, List<String>> queryParameters = new HashMap<String, List<String>>();
		queryParameters.put("active", Collections.singletonList("true"));
		queryParameters.put("_page", Collections.singletonList(String.valueOf(page)));

		Bundle searchResult = getFhirWebserviceClientProvider().getLocalWebserviceClient()
				.searchWithStrictHandling(Organization.class, queryParameters);

		if (searchResult.getTotal() > currentTotal + searchResult.getEntry().size())
			return Stream.concat(toOrganization(searchResult),
					getActiveOrganizations(page + 1, currentTotal + searchResult.getEntry().size()));
		else
			return toOrganization(searchResult);
	}

	private Stream<Organization> toOrganization(Bundle searchResult)
	{
		Objects.requireNonNull(searchResult, "searchResult");

		return searchResult.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.filter(e -> e.getResource() instanceof Organization).map(e -> (Organization) e.getResource())
				.filter(e -> e.getActive());
	}
}
