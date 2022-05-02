package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.mpc.ArithmeticShare;
import org.highmed.dsf.bpe.mpc.ArithmeticSharing;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;

public class CalculateSingleMedicResultShares extends AbstractServiceDelegate
{
	public CalculateSingleMedicResultShares(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults queryResults = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		Targets targets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);

		List<QueryResult> shares = queryResults.getResults().stream()
				.flatMap(queryResult -> toArithmeticSharesForCohortAndOrganization(queryResult, targets))
				.collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES,
				QueryResultsValues.create(new QueryResults(shares)));
	}

	private Stream<QueryResult> toArithmeticSharesForCohortAndOrganization(QueryResult queryResult, Targets targets)
	{
		List<Target> organizations = targets.getEntries();

		int numParties = organizations.size();
		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(numParties);

		int secret = queryResult.getCohortSize();
		int maxSecret = arithmeticSharing.getRingSize().divide(BigInteger.valueOf(numParties)).intValueExact();

		if (secret > maxSecret)
			throw new IllegalStateException(
					"Secret > maxSecret (" + maxSecret + ") for " + numParties + " participating organizations");

		ArithmeticShare[] shares = arithmeticSharing.createShares(secret);

		if (shares.length != numParties)
			throw new IllegalStateException("Number of shares does not match number of targets");

		return IntStream.range(0, numParties)
				.mapToObj(i -> QueryResult.mpcCountResult(organizations.get(i).getTargetOrganizationIdentifierValue(),
						queryResult.getCohortId(), shares[i].getValue().intValueExact()));
	}
}
