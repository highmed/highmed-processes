package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSingleMedicResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckSingleMedicResults.class);

	public CheckSingleMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		Task currentTask = getCurrentTaskFromExecutionVariables(execution);
		List<QueryResult> filteredResults = filterErroneousResultsAndAddErrorsToCurrentTaskOutputs(results,
				currentTask);

		// TODO: add percentage filter over results

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(filteredResults)));
	}

	private List<QueryResult> filterErroneousResultsAndAddErrorsToCurrentTaskOutputs(QueryResults results, Task task)
	{
		List<QueryResult> filteredResults = new ArrayList<>();
		for (QueryResult result : results.getResults())
		{
			Optional<String> errorReason = testResultAndReturnErrorReason(result);
			if (errorReason.isPresent())
				addError(task, result.getCohortId(), errorReason.get());
			else
				filteredResults.add(result);
		}

		return filteredResults;
	}

	protected Optional<String> testResultAndReturnErrorReason(QueryResult result)
	{
		// TODO: implement check
		// cohort size > 0
		// other filter criteria tbd
		return Optional.empty();
	}

	private void addError(Task task, String cohortId, String error)
	{
		String errorMessage = "Feasibility query result check failed for group with id '" + cohortId + "': " + error;
		logger.info(errorMessage);

		task.getOutput().add(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
				errorMessage));
	}
}
