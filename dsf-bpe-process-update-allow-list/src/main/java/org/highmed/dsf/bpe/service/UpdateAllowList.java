package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class UpdateAllowList extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateAllowList.class);

	public UpdateAllowList(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		FhirWebserviceClient client = getFhirWebserviceClientProvider().getLocalWebserviceClient();

		Bundle searchSet = client.searchWithStrictHandling(Organization.class,
				Map.of("active", Collections.singletonList("true"), "identifier",
						Collections.singletonList(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|"), "_include",
						Collections.singletonList("Organization:endpoint"), "_revinclude",
						Collections.singletonList("OrganizationAffiliation:participating-organization")));

		Map<String, String> identifierByTypeAndId = searchSet.getEntry().stream()
				.filter(BundleEntryComponent::hasResource).map(BundleEntryComponent::getResource)
				.filter(r -> r instanceof Organization || r instanceof Endpoint).filter(Resource::hasIdElement)
				.collect(Collectors.toMap(r -> r.getIdElement().toUnqualifiedVersionless().getValue(),
						this::getIdentifierValue));
		Map<String, String> tempIdsByTypeAndId = searchSet.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.map(BundleEntryComponent::getResource)
				.filter(r -> r instanceof Organization || r instanceof Endpoint || r instanceof OrganizationAffiliation)
				.filter(Resource::hasIdElement)
				.collect(Collectors.toMap(r -> r.getIdElement().toUnqualifiedVersionless().getValue(),
						r -> "urn:uuid:" + UUID.randomUUID().toString()));

		Bundle transaction = new Bundle().setType(BundleType.TRANSACTION);

		getReadAccessHelper().addAll(transaction);

		transaction.getIdentifier().setSystem(CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST)
				.setValue(CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST);

		List<BundleEntryComponent> entries = searchSet.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.map(BundleEntryComponent::getResource)
				.filter(r -> r instanceof Organization || r instanceof Endpoint || r instanceof OrganizationAffiliation)
				.map(toAllowListBundleEntry(identifierByTypeAndId, tempIdsByTypeAndId)).collect(Collectors.toList());

		transaction.setEntry(entries);

		logger.debug("Uploading new allow list transaction bundle: {}",
				FhirContext.forR4().newJsonParser().encodeResourceToString(transaction));

		IdType result = client.withMinimalReturn().updateConditionaly(transaction,
				Map.of("identifier", Collections.singletonList(CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST + "|"
						+ CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST)));

		Task task = getLeadingTaskFromExecutionVariables(execution);
		task.addOutput().setValue(new Reference(new IdType("Bundle", result.getIdPart(), result.getVersionIdPart())))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST);
	}

	private String getIdentifierValue(Resource resource)
	{
		if (resource instanceof Organization)
			return ((Organization) resource).getIdentifier().stream().filter(Identifier::hasSystem)
					.filter(i -> NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER.equals(i.getSystem())).findFirst()
					.filter(Identifier::hasValue).map(Identifier::getValue)
					.orElseThrow(() -> new RuntimeException("Organization is missing identifier value with system "
							+ NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER));
		else if (resource instanceof Endpoint)
			return ((Endpoint) resource).getIdentifier().stream().filter(Identifier::hasSystem)
					.filter(i -> NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER.equals(i.getSystem())).findFirst()
					.filter(Identifier::hasValue).map(Identifier::getValue)
					.orElseThrow(() -> new RuntimeException("Endpoint is missing identifier value with system "
							+ NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER));
		else
			throw new IllegalStateException("Organization or Endpoint expected");
	}

	private Function<Resource, BundleEntryComponent> toAllowListBundleEntry(Map<String, String> identifierByTypeAndId,
			Map<String, String> tempIdsByTypeAndId)
	{
		return resource ->
		{
			if (resource instanceof Organization)
				return toOrganizationEntry((Organization) resource, identifierByTypeAndId, tempIdsByTypeAndId);
			else if (resource instanceof Endpoint)
				return toEndpointEntry((Endpoint) resource, identifierByTypeAndId, tempIdsByTypeAndId);
			else if (resource instanceof OrganizationAffiliation)
				return toOrganizationAffiliationEntry((OrganizationAffiliation) resource, identifierByTypeAndId,
						tempIdsByTypeAndId);
			else
				throw new IllegalStateException("Organization, Endpoint or OrganizationAffiliation expected");
		};
	}

	private BundleEntryComponent toOrganizationEntry(Organization organization,
			Map<String, String> identifierByTypeAndId, Map<String, String> tempIdsByTypeAndId)
	{
		String typeAndId = organization.getIdElement().toUnqualifiedVersionless().getValue();
		String uuid = tempIdsByTypeAndId.get(typeAndId);
		String organizationIdentifier = identifierByTypeAndId.get(typeAndId);

		organization.setIdElement(new IdType(uuid));
		organization.getMeta().setVersionIdElement(null).setLastUpdatedElement(null);

		if (organization.hasEndpoint())
		{
			List<Reference> endpoints = organization.getEndpoint().stream()
					.map(e -> tempIdsByTypeAndId.get(e.getReference()))
					.map(tempId -> new Reference().setType("Endpoint").setReference(tempId))
					.collect(Collectors.toList());
			organization.setEndpoint(endpoints);
		}

		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setFullUrl(uuid);
		entry.setResource(organization);
		entry.getRequest().setMethod(HTTPVerb.PUT).setUrl("Organization?identifier="
				+ NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|" + organizationIdentifier);
		return entry;
	}

	private BundleEntryComponent toEndpointEntry(Endpoint endpoint, Map<String, String> identifierByTypeAndId,
			Map<String, String> tempIdsByTypeAndId)
	{
		String typeAndId = endpoint.getIdElement().toUnqualifiedVersionless().getValue();
		String uuid = tempIdsByTypeAndId.get(typeAndId);
		String endpointIdentifier = identifierByTypeAndId.get(typeAndId);

		endpoint.setIdElement(new IdType(uuid));
		endpoint.getMeta().setVersionIdElement(null).setLastUpdatedElement(null);

		if (endpoint.hasManagingOrganization())
		{
			String organizationTempId = tempIdsByTypeAndId.get(endpoint.getManagingOrganization().getReference());
			endpoint.setManagingOrganization(new Reference().setType("Organization").setReference(organizationTempId));
		}

		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setFullUrl(uuid);
		entry.setResource(endpoint);
		entry.getRequest().setMethod(HTTPVerb.PUT)
				.setUrl("Endpoint?identifier=" + NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER + "|" + endpointIdentifier);
		return entry;
	}

	private BundleEntryComponent toOrganizationAffiliationEntry(OrganizationAffiliation affiliation,
			Map<String, String> identifierByTypeAndId, Map<String, String> tempIdsByTypeAndId)
	{
		String uuid = tempIdsByTypeAndId.get(affiliation.getIdElement().toUnqualifiedVersionless().getValue());

		affiliation.setIdElement(new IdType(uuid));
		affiliation.getMeta().setVersionIdElement(null).setLastUpdatedElement(null);

		String primaryOrganizatioIdentifier;
		if (affiliation.hasOrganization())
		{
			String ref = affiliation.getOrganization().getReference();
			primaryOrganizatioIdentifier = identifierByTypeAndId.get(ref);
			String primaryOrganizationTempId = tempIdsByTypeAndId.get(ref);

			affiliation
					.setOrganization(new Reference().setType("Organization").setReference(primaryOrganizationTempId));
		}
		else
			throw new IllegalStateException("OrganizationAffiliation with primary organization expected");

		String participatingOrganizationIdentifier;
		if (affiliation.hasParticipatingOrganization())
		{
			String ref = affiliation.getParticipatingOrganization().getReference();
			participatingOrganizationIdentifier = identifierByTypeAndId.get(ref);
			String participatingOrganizationTempId = tempIdsByTypeAndId.get(ref);

			affiliation.setParticipatingOrganization(
					new Reference().setType("Organization").setReference(participatingOrganizationTempId));
		}
		else
			throw new IllegalStateException("OrganizationAffiliation with participating organization expected");

		if (affiliation.hasEndpoint())
		{
			List<Reference> endpoints = affiliation.getEndpoint().stream()
					.map(e -> tempIdsByTypeAndId.get(e.getReference()))
					.map(tempId -> new Reference().setType("Endpoint").setReference(tempId))
					.collect(Collectors.toList());
			affiliation.setEndpoint(endpoints);
		}

		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setFullUrl(uuid);
		entry.setResource(affiliation);
		entry.getRequest().setMethod(HTTPVerb.PUT)
				.setUrl("OrganizationAffiliation?primary-organization:identifier="
						+ NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|" + primaryOrganizatioIdentifier
						+ "&participating-organization:identifier=" + NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|"
						+ participatingOrganizationIdentifier);
		return entry;
	}
}
