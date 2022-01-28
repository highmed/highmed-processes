package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
import org.hl7.fhir.r4.model.Task;

public class StoreSingleMedicResultShareCorrelationKeys extends AbstractServiceDelegate
{
	public StoreSingleMedicResultShareCorrelationKeys(FhirWebserviceClientProvider clientProvider,
			TaskHelper taskHelper, ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables();

		Targets targets = getTargets(task);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(targets));

		String correlationKey = getCorrelationKey(task);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_CORRELATION_KEY, correlationKey);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES,
				QueryResultsValues.create(new QueryResults(null)));
	}

	private Targets getTargets(Task task)
	{
		List<Target> targets = getTaskHelper()
				.getInputParameterStringValues(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY)
				.map(correlationKey -> Target.createBiDirectionalTarget("", "", correlationKey))
				.collect(Collectors.toList());

		return new Targets(targets);
	}

	private String getCorrelationKey(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
						CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY)
				.orElseThrow(() -> new IllegalArgumentException("CorrelationKey is not set in task with id='"
						+ task.getId() + "', this error should have been caught by resource validation"));
	}
}
