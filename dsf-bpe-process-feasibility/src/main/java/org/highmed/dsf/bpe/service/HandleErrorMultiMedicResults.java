package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;

public class HandleErrorMultiMedicResults extends AbstractServiceDelegate
{
	public HandleErrorMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task currentTask = getCurrentTaskFromExecutionVariables(execution);
		Task leadingTask = getLeadingTaskFromExecutionVariables(execution);

		currentTask.getInput().forEach(input ->
		{
			boolean isErrorInput = input.getType().getCoding().stream()
					.anyMatch(code -> code.getSystem().equals(CODESYSTEM_HIGHMED_BPMN)
							&& code.getCode().equals(CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR));

			if (isErrorInput)
			{
				leadingTask.getOutput().add(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN,
						CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, input.getValue().primitiveValue()));
			}
		});

		// The current task finishes here but is not automatically set to completed
		// because it is an additional task during the execution of the main process
		currentTask.setStatus(Task.TaskStatus.COMPLETED);
		getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(currentTask);
	}
}
