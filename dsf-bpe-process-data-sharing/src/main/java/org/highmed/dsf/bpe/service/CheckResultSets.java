package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CheckResultSets extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckResultSets.class);

	public CheckResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> filteredResults = filterErroneousResultsAndAddErrorsToCurrentTaskOutputs(results);
		List<QueryResult> postProcessedResults = postProcessAllPassingResults(filteredResults);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(postProcessedResults)));
	}

	private List<QueryResult> filterErroneousResultsAndAddErrorsToCurrentTaskOutputs(QueryResults results)
	{
		Task task = getCurrentTaskFromExecutionVariables();
		return results.getResults().stream().filter(r -> testResultAndAddPossibleError(r, task))
				.collect(Collectors.toList());
	}

	private boolean testResultAndAddPossibleError(QueryResult result, Task task)
	{
		boolean hasFailingCheck = getChecks(result, task).map(check -> check.apply(result, task))
				.anyMatch(Boolean.FALSE::equals);
		return !hasFailingCheck;
	}

	/**
	 * @param result
	 *            the {@link QueryResult} that should be checked
	 * @param task
	 *            the task to which errors should be added as outputs if the check is not passed, use
	 *            {@link #getChecks(QueryResult, Task)} to add the error output
	 * @return a list of checks that should be performed for given {@link QueryResult}, each check returning
	 *         <code>true</code> if the check is passed, <code>false</code> otherwise
	 */
	protected abstract Stream<BiFunction<QueryResult, Task, Boolean>> getChecks(QueryResult result, Task task);

	/**
	 * @param passedResults
	 *            all {@link QueryResult} objects that passed the checks defined by
	 *            {@link #getChecks(QueryResult, Task)}
	 * @return a list of {@link QueryResult} objects after post processing
	 */
	protected List<QueryResult> postProcessAllPassingResults(List<QueryResult> passedResults)
	{
		return passedResults;
	}

	protected void addError(Task task, String cohortId, String error)
	{
		String errorMessage = "Data sharing query result check failed for group with id='" + cohortId + "', reason: "
				+ error;
		logger.warn(errorMessage);

		task.getOutput().add(getTaskHelper().createOutput(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, errorMessage));
	}

	protected boolean checkColumns(QueryResult result, Task task)
	{
		boolean hasError = result.getResultSet().getColumns().size() < 1;

		if (hasError)
			addError(task, result.getCohortId(), "no columns present");

		return hasError;
	}

	protected boolean checkRows(QueryResult result, Task task)
	{
		boolean hasError = result.getResultSet().getRows().size() < 1;

		if (hasError)
			addError(task, result.getCohortId(), "no rows present");

		return hasError;
	}
}
