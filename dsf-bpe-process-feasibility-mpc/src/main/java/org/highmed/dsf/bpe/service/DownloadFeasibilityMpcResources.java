package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_COHORTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_CORRELATION_KEY;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.highmed.dsf.fhir.variables.FhirResourcesListValues;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DownloadFeasibilityMpcResources extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadFeasibilityMpcResources.class);

	private final OrganizationProvider organizationProvider;

	public DownloadFeasibilityMpcResources(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables(execution);

		IdType researchStudyId = getResearchStudyId(task);
		FhirWebserviceClient client = getWebserviceClient(researchStudyId);
		Bundle bundle = getResearchStudyAndCohortDefinitions(researchStudyId, client);

		ResearchStudy researchStudy = (ResearchStudy) bundle.getEntryFirstRep().getResource();
		execution.setVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY, FhirResourceValues.create(researchStudy));

		List<Group> cohortDefinitions = getCohortDefinitions(bundle, client.getBaseUrl());
		execution.setVariable(BPMN_EXECUTION_VARIABLE_COHORTS, FhirResourcesListValues.create(cohortDefinitions));

		boolean needsConsentCheck = getNeedsConsentCheck(task);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		String correlationKey = getCorrelationKey(task);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_CORRELATION_KEY, correlationKey);
	}

	private IdType getResearchStudyId(Task task)
	{
		Reference researchStudyReference = getTaskHelper().getInputParameterReferenceValues(task,
				CODESYSTEM_HIGHMED_DATA_SHARING, CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE)
				.findFirst().get();

		return new IdType(researchStudyReference.getReference());
	}

	private FhirWebserviceClient getWebserviceClient(IdType researchStudyId)
	{
		if (researchStudyId.getBaseUrl() == null
				|| researchStudyId.getBaseUrl().equals(getFhirWebserviceClientProvider().getLocalBaseUrl()))
		{
			return getFhirWebserviceClientProvider().getLocalWebserviceClient();
		}
		else
		{
			return getFhirWebserviceClientProvider().getWebserviceClient(researchStudyId.getBaseUrl());
		}
	}

	private Bundle getResearchStudyAndCohortDefinitions(IdType researchStudyId, FhirWebserviceClient client)
	{
		try
		{
			Bundle bundle = client.searchWithStrictHandling(ResearchStudy.class,
					Map.of("_id", Collections.singletonList(researchStudyId.getIdPart()), "_include",
							Collections.singletonList("ResearchStudy:enrollment")));

			if (bundle.getEntry().size() < 2)
			{
				throw new RuntimeException("Returned search-set contained less then two entries");
			}
			else if (!bundle.getEntryFirstRep().hasResource()
					|| !(bundle.getEntryFirstRep().getResource() instanceof ResearchStudy))
			{
				throw new RuntimeException("Returned search-set did not contain ResearchStudy at index == 0");
			}
			else if (bundle.getEntry().stream().skip(1).map(c -> c.hasResource() && c.getResource() instanceof Group)
					.filter(b -> !b).findAny().isPresent())
			{
				throw new RuntimeException("Returned search-set contained unexpected resource at index >= 1");
			}

			return bundle;
		}
		catch (Exception e)
		{
			logger.warn("Error while reading ResearchStudy  with id {} including Groups from {}: {}",
					researchStudyId.getIdPart(), client.getBaseUrl(), e.getMessage());
			throw e;
		}
	}

	private List<Group> getCohortDefinitions(Bundle bundle, String baseUrl)
	{
		return bundle.getEntry().stream().skip(1).map(e ->
		{
			Group group = (Group) e.getResource();
			IdType oldId = group.getIdElement();
			group.setIdElement(
					new IdType(baseUrl, oldId.getResourceType(), oldId.getIdPart(), oldId.getVersionIdPart()));
			return group;
		}).collect(Collectors.toList());
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK)
				.orElseThrow(() -> new IllegalArgumentException("NeedsConsentCheck boolean is not set in task with id='"
						+ task.getId() + "', this error should have been caught by resource validation"));
	}

	private String getCorrelationKey(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
						CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY)
				.orElseThrow(() -> new IllegalArgumentException("CorrelationKey is not set in task with id='"
						+ task.getId() + "', this error should have been caught by resource validation"));
	}
}
