package org.highmed.dsf.bpe.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_MULTI_MEDIC_SHARES;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.mpc.ArithmeticShare;
import org.highmed.dsf.bpe.mpc.ArithmeticSharing;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Targets;
import org.springframework.beans.factory.InitializingBean;

public class CalculateMultiMedicResultShares extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public CalculateMultiMedicResultShares(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvided");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults shares = (QueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES);

		Targets targets = ((Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS));

		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(targets.getEntries().size());
		String organizationIdentifier = organizationProvider.getLocalIdentifierValue();

		Map<String, List<QueryResult>> byCohortId = shares.getResults().stream()
				.collect(groupingBy(QueryResult::getCohortId));

		List<QueryResult> reconstructedResults = byCohortId.entrySet().stream()
				.map(group -> toReconstructedResult(arithmeticSharing, organizationIdentifier, group))
				.collect(toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_MULTI_MEDIC_SHARES,
				QueryResultsValues.create(new QueryResults(reconstructedResults)));
	}

	private QueryResult toReconstructedResult(ArithmeticSharing arithmeticSharing, String organizationIdentifier,
			Map.Entry<String, List<QueryResult>> group)
	{
		String cohortId = group.getKey();
		List<ArithmeticShare> toReconstruct = group.getValue().stream().map(s -> toArithmeticShare(s.getCohortSize()))
				.collect(toList());
		int reconstructedResult = arithmeticSharing.reconstructSecretToInt(toReconstruct);

		return QueryResult.mpcCountResult(organizationIdentifier, cohortId, reconstructedResult);
	}

	private ArithmeticShare toArithmeticShare(int share)
	{
		return new ArithmeticShare(BigInteger.valueOf(share));
	}
}
