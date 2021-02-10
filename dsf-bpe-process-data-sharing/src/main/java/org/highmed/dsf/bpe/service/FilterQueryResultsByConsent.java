package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.ResultSet;

public class FilterQueryResultsByConsent extends AbstractServiceDelegate
{
	public FilterQueryResultsByConsent(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		List<QueryResult> filteredResults = filterResults(results.getResults());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(filteredResults)));
	}

	private List<QueryResult> filterResults(List<QueryResult> results)
	{
		return results.stream().map(this::filterResult).collect(Collectors.toList());
	}

	protected QueryResult filterResult(QueryResult result)
	{
		return QueryResult.resultSet(result.getOrganizationIdentifier(), result.getCohortId(),
				filterResultSet(result.getResultSet()));
	}

	private ResultSet filterResultSet(ResultSet resultSet)
	{
		// TODO implement

		return resultSet;
	}
}
