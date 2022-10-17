package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SHARE;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_MULTI_MEDIC_SHARES;

import java.util.ArrayList;
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
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UnsignedIntType;
import org.springframework.beans.factory.InitializingBean;

public class StoreResultsMultiMedicShare extends AbstractServiceDelegate implements InitializingBean
{
	public StoreResultsMultiMedicShare(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_MULTI_MEDIC_SHARES);

		Task task = getCurrentTaskFromExecutionVariables(execution);

		List<QueryResult> extendedResults = new ArrayList<>();
		extendedResults.addAll(results.getResults());
		extendedResults.addAll(getResults(task));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_MULTI_MEDIC_SHARES,
				QueryResultsValues.create(new QueryResults(extendedResults)));
	}

	private List<QueryResult> getResults(Task task)
	{
		TaskHelper taskHelper = getTaskHelper();
		Reference requester = task.getRequester();

		return taskHelper
				.getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SHARE, EXTENSION_HIGHMED_GROUP_ID)
				.map(input ->
				{
					String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
					int cohortSize = ((UnsignedIntType) input.getValue()).getValue();

					return QueryResult.mpcCountResult(requester.getIdentifier().getValue(), cohortId, cohortSize);
				}).collect(Collectors.toList());
	}
}