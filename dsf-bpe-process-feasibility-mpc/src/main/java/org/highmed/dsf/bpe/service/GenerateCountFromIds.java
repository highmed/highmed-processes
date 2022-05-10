package org.highmed.dsf.bpe.service;

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

public class GenerateCountFromIds extends AbstractServiceDelegate
{
	public GenerateCountFromIds(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		List<QueryResult> filteredResults = count(results.getResults());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(filteredResults)));
	}

	private List<QueryResult> count(List<QueryResult> results)
	{
		return results.stream().map(this::count).collect(Collectors.toList());
	}

	protected QueryResult count(QueryResult result)
	{
		return QueryResult.countResult(result.getOrganizationIdentifier(), result.getCohortId(),
				result.getResultSet().getRows().size());
	}
}
