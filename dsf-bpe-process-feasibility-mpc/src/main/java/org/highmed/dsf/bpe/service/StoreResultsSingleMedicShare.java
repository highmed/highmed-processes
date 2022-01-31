package org.highmed.dsf.bpe.service;

import static java.util.stream.Collectors.toList;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SHARE;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.mpc.ArithmeticShare;
import org.highmed.dsf.bpe.mpc.QueryResultShare;
import org.highmed.dsf.bpe.mpc.QueryResultShares;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UnsignedIntType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class StoreResultsSingleMedicShare extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(StoreResultsSingleMedicShare.class);

	public StoreResultsSingleMedicShare(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResultShares shares = (QueryResultShares) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES);

		Task task = getCurrentTaskFromExecutionVariables();

		List<QueryResultShare> extendedShares = new ArrayList<>();
		extendedShares.addAll(shares.getShares());
		extendedShares.addAll(getShares(task));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES,
				new QueryResultShares(extendedShares));
	}

	private List<QueryResultShare> getShares(Task task)
	{
		TaskHelper taskHelper = getTaskHelper();
		Reference requester = task.getRequester();

		return taskHelper
				.getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SHARE, EXTENSION_HIGHMED_GROUP_ID)
				.map(input -> toQueryResultShare(requester, input)).collect(toList());
	}

	private QueryResultShare toQueryResultShare(Reference requester, Task.ParameterComponent input)
	{
		String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
		String organizationIdentifier = requester.getIdentifier().getValue();
		int shareSize = ((UnsignedIntType) input.getValue()).getValue();

		return new QueryResultShare(cohortId, organizationIdentifier,
				new ArithmeticShare(BigInteger.valueOf(shareSize)));
	}
}
