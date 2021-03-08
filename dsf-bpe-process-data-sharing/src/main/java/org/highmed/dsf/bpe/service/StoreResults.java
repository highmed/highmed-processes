package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.EXTENSION_HIGHMED_GROUP_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsDataSharing;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreResults extends AbstractServiceDelegate
{

	private static final Logger logger = LoggerFactory.getLogger(StoreResults.class);

	public StoreResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> extendedResults = new ArrayList<>(results.getResults());

		Task task = getCurrentTaskFromExecutionVariables();
		extendedResults.addAll(getResults(task,
				ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SET_REFERENCE));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(extendedResults)));
	}

	private List<QueryResult> getResults(Task task, String code)
	{
		TaskHelper taskHelper = getTaskHelper();
		Reference requester = task.getRequester();

		return taskHelper
				.getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_DATA_SHARING, code, EXTENSION_HIGHMED_GROUP_ID)
				.map(input ->
				{
					String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
					String resultSetUrl = ((Reference) input.getValue()).getReference();

					return QueryResult.idResult(requester.getIdentifier().getValue(), cohortId, resultSetUrl);
				}).collect(Collectors.toList());
	}
}
