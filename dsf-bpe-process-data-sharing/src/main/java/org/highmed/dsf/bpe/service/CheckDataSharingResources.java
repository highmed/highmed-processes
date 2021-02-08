package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_COHORTS;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourcesList;
import org.hl7.fhir.r4.model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckDataSharingResources extends AbstractServiceDelegate
{

	private static final Logger logger = LoggerFactory.getLogger(CheckDataSharingResources.class);

	public CheckDataSharingResources(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		logger.info(this.getClass().getName() + " doExecute called");

		List<Group> cohorts = ((FhirResourcesList) execution.getVariable(BPMN_EXECUTION_VARIABLE_COHORTS))
				.getResourcesAndCast();

		checkFullyQualifiedCohortIds(cohorts);

		// TODO: Define further checks
	}

	private void checkFullyQualifiedCohortIds(List<Group> cohorts)
	{
		if (cohorts.stream().anyMatch(g -> !g.getIdElement().hasBaseUrl()))
		{
			throw new RuntimeException("Not all cohorts have fully qualified ids (containing server base url)");
		}
	}
}
