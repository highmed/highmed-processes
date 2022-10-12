package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.stream.Collectors;

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

public abstract class AbstractCheckResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractCheckResults.class);

	public AbstractCheckResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> filteredResults = filterErroneousResultsAndAddErrorsToCurrentTaskOutputs(execution, results);
		List<QueryResult> postProcessedResults = postProcessAllPassingResults(execution, filteredResults);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(postProcessedResults)));
	}

	private List<QueryResult> filterErroneousResultsAndAddErrorsToCurrentTaskOutputs(DelegateExecution execution,
			QueryResults results)
	{
		Task task = getLeadingTaskFromExecutionVariables(execution);
		return results.getResults().stream().filter(r -> testResultAndAddPossibleError(r, task))
				.collect(Collectors.toList());
	}

	/**
	 * @param result
	 *            not <code>null</code>
	 * @param task
	 *            not <code>null</code>
	 * @return <code>true</code> if the supplied {@link QueryResult} passed all checks, <code>false</code> otherwise
	 */
	protected boolean testResultAndAddPossibleError(QueryResult result, Task task)
	{
		return checkColumns(result, task) && checkRows(result, task);
	}

	/**
	 * @param execution
	 *            not <code>null</code>
	 * @param passedResults
	 *            all {@link QueryResult} objects that passed the checks executed by
	 *            {@link #testResultAndAddPossibleError(QueryResult, Task)}
	 * @return a list of {@link QueryResult} objects after post processing
	 */
	protected List<QueryResult> postProcessAllPassingResults(DelegateExecution execution,
			List<QueryResult> passedResults)
	{
		return passedResults;
	}

	protected void addError(Task task, String organizationIdentifier, String cohortId, String error)
	{
		String errorMessage = "QueryResult check failed for group with id='" + cohortId
				+ "' supplied from organization " + organizationIdentifier + ", reason: " + error;
		logger.warn(errorMessage);

		task.getOutput().add(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
				errorMessage));
	}

	protected boolean checkColumns(QueryResult result, Task task)
	{
		boolean passed = result.getResultSet().getColumns().size() > 0;

		if (!passed)
			addError(task, result.getOrganizationIdentifier(), result.getCohortId(), "no columns present");

		return passed;
	}

	protected boolean checkRows(QueryResult result, Task task)
	{
		boolean passed = result.getResultSet().getRows().size() > 0;

		if (!passed)
			addError(task, result.getOrganizationIdentifier(), result.getCohortId(), "no rows present");

		return passed;
	}
}
