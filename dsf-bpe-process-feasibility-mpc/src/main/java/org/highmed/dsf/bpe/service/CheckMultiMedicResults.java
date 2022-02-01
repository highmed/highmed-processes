package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDICS;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FinalFeasibilityMpcQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityMpcQueryResults;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckMultiMedicResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckMultiMedicResults.class);

	public CheckMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task leadingTask = getLeadingTaskFromExecutionVariables();

		FinalFeasibilityMpcQueryResults results = (FinalFeasibilityMpcQueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS);

		FinalFeasibilityMpcQueryResults checkedResults = checkResults(results);

		addFinalFeasibilityQueryResultsToLeadingTask(checkedResults, leadingTask);
	}

	protected FinalFeasibilityMpcQueryResults checkResults(FinalFeasibilityMpcQueryResults results)
	{
		// TODO implement check for results
		// - criterias tbd
		return results;
	}

	private void addFinalFeasibilityQueryResultsToLeadingTask(FinalFeasibilityMpcQueryResults results, Task toWrite)
	{
		results.getResults().forEach(result -> addResultOutput(result, toWrite));
	}

	private void addResultOutput(FinalFeasibilityMpcQueryResult result, Task toWrite)
	{
		Task.TaskOutputComponent output1 = getTaskHelper().createOutputUnsignedInt(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT, result.getCohortSize());
		output1.addExtension(createCohortIdExtension(result.getCohortId()));
		toWrite.addOutput(output1);

		Task.TaskOutputComponent output2 = getTaskHelper().createOutputUnsignedInt(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDICS, result.getParticipatingMedics());
		output2.addExtension(createCohortIdExtension(result.getCohortId()));
		toWrite.addOutput(output2);

		logger.info("Storing MultiMedicResult with cohortId={} and size={}", result.getCohortId(),
				result.getCohortSize());
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(EXTENSION_HIGHMED_GROUP_ID, new Reference(cohortId));
	}
}
