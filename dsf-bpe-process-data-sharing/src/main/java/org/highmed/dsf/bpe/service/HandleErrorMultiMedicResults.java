package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleErrorMultiMedicResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(HandleErrorMultiMedicResults.class);

	public HandleErrorMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		Task currentTask = getCurrentTaskFromExecutionVariables();
		Task leadingTask = getLeadingTaskFromExecutionVariables();

		currentTask.getInput().stream().filter(this::isErrorInput)
				.forEach(input -> transformToOutput(currentTask.getId(), input, leadingTask));

		// The current task finishes here but is not automatically set to completed
		// because it is an additional task during the execution of the main process
		currentTask.setStatus(Task.TaskStatus.COMPLETED);
		getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(currentTask);
	}

	private boolean isErrorInput(Task.ParameterComponent input)
	{
		return input.getType().getCoding().stream()
				.anyMatch(code -> code.getSystem().equals(ConstantsBase.CODESYSTEM_HIGHMED_BPMN)
						&& code.getCode().equals(ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR));
	}

	private void transformToOutput(String taskId, Task.ParameterComponent input, Task leadingTask)
	{
		String errorMessage = input.getValue().primitiveValue();
		logger.warn("Received data sharing error in task with id='{}', reason: {}", taskId, errorMessage);
		leadingTask.getOutput().add(getTaskHelper().createOutput(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, errorMessage));
	}
}
