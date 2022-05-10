package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_COHORTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourcesList;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckDataSharingResources extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckDataSharingResources.class);

	public CheckDataSharingResources(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		checkConsentCheck(execution);
		checkFullyQualifiedCohortIds(execution);

		// TODO: define and implement further checks
	}

	private void checkConsentCheck(DelegateExecution execution)
	{
		Boolean needsConsentCheck = (Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK);

		if (!needsConsentCheck)
		{
			ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY);
			logger.warn("Execution process executeDataSharing for ResearchStudy with id {} without consent check",
					researchStudy.getId());
		}

	}

	private void checkFullyQualifiedCohortIds(DelegateExecution execution)
	{
		List<Group> cohorts = ((FhirResourcesList) execution.getVariable(BPMN_EXECUTION_VARIABLE_COHORTS))
				.getResourcesAndCast();

		if (cohorts.stream().anyMatch(g -> !g.getIdElement().hasBaseUrl()))
		{
			throw new RuntimeException("Not all cohorts have fully qualified ids (containing server base url)");
		}
	}
}
