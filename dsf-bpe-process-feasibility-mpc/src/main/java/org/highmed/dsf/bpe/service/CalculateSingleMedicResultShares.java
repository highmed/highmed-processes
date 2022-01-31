package org.highmed.dsf.bpe.service;

import static java.util.stream.Collectors.toList;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.mpc.ArithmeticShare;
import org.highmed.dsf.bpe.mpc.ArithmeticSharing;
import org.highmed.dsf.bpe.mpc.QueryResultShare;
import org.highmed.dsf.bpe.mpc.QueryResultShares;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalculateSingleMedicResultShares extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CalculateSingleMedicResultShares.class);

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

		List<QueryResultShare> shares = queryResults.getResults().stream()
				.flatMap(queryResult -> toArithmeticSharesForCohortAndOrganization(queryResult, targets))
				.collect(toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES, new QueryResultShares(shares));
	}

	private Stream<QueryResultShare> toArithmeticSharesForCohortAndOrganization(QueryResult queryResult,
			Targets targets)
	{
		List<Target> organizations = targets.getEntries();
		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(organizations.size());
		List<ArithmeticShare> shares = arithmeticSharing.createShares(queryResult.getCohortSize());

		if (shares.size() != organizations.size())
			throw new IllegalStateException("Number of shares does not match number of targets");

		return IntStream.range(0, shares.size()).mapToObj(i -> new QueryResultShare(queryResult.getCohortId(),
				organizations.get(i).getTargetOrganizationIdentifierValue(), shares.get(i)));
	}

	private int getNumberOfParticipatingMedics(DelegateExecution execution)
	{
		return ((Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS)).getEntries().size();
	}
}
