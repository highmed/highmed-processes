package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_MULTI_MEDIC_SHARES;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.mpc.ArithmeticShare;
import org.highmed.dsf.bpe.mpc.ArithmeticSharing;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variables.FinalFeasibilityMpcQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityMpcQueryResults;
import org.highmed.dsf.bpe.variables.FinalFeasibilityMpcQueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Targets;

public class CalculateMultiMedicResult extends AbstractServiceDelegate
{
	public CalculateMultiMedicResult(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults shares = (QueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_MULTI_MEDIC_SHARES);

		Targets targets = ((Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS));

		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(targets.getEntries().size());

		Map<String, List<QueryResult>> byCohortId = shares.getResults().stream()
				.collect(Collectors.groupingBy(QueryResult::getCohortId));

		List<FinalFeasibilityMpcQueryResult> reconstructedResults = byCohortId.entrySet().stream()
				.map(group -> toReconstructedResult(arithmeticSharing, group)).collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS, FinalFeasibilityMpcQueryResultsValues
				.create(new FinalFeasibilityMpcQueryResults(reconstructedResults)));
	}

	private FinalFeasibilityMpcQueryResult toReconstructedResult(ArithmeticSharing arithmeticSharing,
			Map.Entry<String, List<QueryResult>> group)
	{
		String cohortId = group.getKey();
		ArithmeticShare[] toReconstruct = group.getValue().stream().map(QueryResult::getCohortSize)
				.map(ArithmeticShare::new).toArray(ArithmeticShare[]::new);

		int reconstructedResult = arithmeticSharing.reconstructSecretToInt(toReconstruct);

		return new FinalFeasibilityMpcQueryResult(cohortId, arithmeticSharing.getNumParties(), reconstructedResult);
	}
}
