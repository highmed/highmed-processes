package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectResponseTargetMedic extends AbstractServiceDelegate
{

	private static final Logger logger = LoggerFactory.getLogger(SelectResponseTargetMedic.class);

	public SelectResponseTargetMedic(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		Task task = getLeadingTaskFromExecutionVariables();

		Target medicTarget = Target.createUniDirectionalTarget(task.getRequester().getIdentifier().getValue());
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET, TargetValues.create(medicTarget));
	}
}
