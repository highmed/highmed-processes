package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;

public class CalculateMultiMedicResults extends AbstractServiceDelegate
{
	public CalculateMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		List<QueryResult> results = ((QueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS)).getResults();

		List<FinalFeasibilityQueryResult> finalResults = calculateResults(results);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS,
				FinalFeasibilityQueryResultsValues.create(new FinalFeasibilityQueryResults(finalResults)));
	}

	private List<FinalFeasibilityQueryResult> calculateResults(List<QueryResult> results)
	{
		Map<String, List<QueryResult>> byCohortId = results.stream()
				.collect(Collectors.groupingBy(QueryResult::getCohortId));

		return byCohortId.entrySet().stream().map(e -> new FinalFeasibilityQueryResult(e.getKey(),
				toInt(e.getValue().stream().filter(r -> r.getCount() > 0).count()),
				toInt(e.getValue().stream().mapToLong(QueryResult::getCount).sum())))
				.collect(Collectors.toList());
	}

	private int toInt(long l)
	{
		if (l > Integer.MAX_VALUE)
			throw new IllegalArgumentException("long > " + Integer.MAX_VALUE);
		else
			return (int) l;
	}
}
