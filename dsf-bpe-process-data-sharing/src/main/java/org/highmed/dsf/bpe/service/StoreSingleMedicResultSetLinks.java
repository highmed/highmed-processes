package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SET_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.EXTENSION_HIGHMED_GROUP_ID;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

public class StoreSingleMedicResultSetLinks extends AbstractServiceDelegate
{
	public StoreSingleMedicResultSetLinks(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		QueryResults currentResults = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> newResults = getNewResults();

		List<QueryResult> extendedResults = Stream.of(currentResults.getResults(), newResults)
				.flatMap(Collection::stream).collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(extendedResults)));
	}

	private List<QueryResult> getNewResults()
	{
		Task task = getCurrentTaskFromExecutionVariables();
		Reference requester = task.getRequester();

		return getTaskHelper().getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SET_REFERENCE, EXTENSION_HIGHMED_GROUP_ID)
				.map(input -> getQueryResult(input, requester)).collect(Collectors.toList());
	}

	private QueryResult getQueryResult(Task.ParameterComponent input, Reference requester)
	{
		String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
		String resultSetUrl = ((Reference) input.getValue()).getReference();
		return QueryResult.idResult(requester.getIdentifier().getValue(), cohortId, resultSetUrl);
	}
}
