package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_TASK;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_NOT_ALLOWED;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_NOT_REACHABLE;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.util.PingResponseHelper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class SendPing extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(SendPing.class);

	public SendPing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Override
	protected void handleSendTaskError(DelegateExecution execution, Target target, Exception exception, Task task)
	{
		Targets targets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);

		// if we are a multi instance message send task, remove target
		if (targets != null)
		{
			Target toRemove = getTargetFromTargets(targets, target.getTargetOrganizationIdentifierValue());
			boolean removed = targets.removeTarget(toRemove);

			if (removed)
			{
				// TODO: after new Target class is available, replace "endpoint-identifier" with actual value from
				// target
				logger.debug("Target organization {} (endpoint {}) with error {} removed from target list",
						toRemove.getTargetOrganizationIdentifierValue(), "endpoint-identifier", exception.getMessage());
			}

			// TODO: does not work if Targets are not updated
			execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, targets);
		}

		if (task != null)
		{
			if (exception instanceof WebApplicationException)
			{
				WebApplicationException webApplicationException = (WebApplicationException) exception;
				if (webApplicationException.getResponse() != null && webApplicationException.getResponse()
						.getStatus() == Response.Status.FORBIDDEN.getStatusCode())
				{
					PingResponseHelper.addResponseToTask(task, target.getTargetOrganizationIdentifierValue(),
							"endpoint-identifier", CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_NOT_ALLOWED);
				}
				else
				{
					PingResponseHelper.addResponseToTask(task, target.getTargetOrganizationIdentifierValue(),
							"endpoint-identifier", CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_NOT_REACHABLE);
				}
			}
			else
			{
				PingResponseHelper.addResponseToTask(task, target.getTargetOrganizationIdentifierValue(),
						"endpoint-identifier", CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_NOT_REACHABLE);
			}

			// TODO: does not work if Task is not updated
			execution.setVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK, task);
		}

		if (targets != null && targets.isEmpty())
		{
			logger.debug("Error while executing Task message send " + getClass().getName(), exception);
			logger.error("Process {} has fatal error in step {} for task with id {}, last reason: {}",
					execution.getProcessDefinitionId(), execution.getActivityInstanceId(),
					task == null ? "?" : task.getId(), exception.getMessage());

			if (task != null)
			{
				task.setStatus(Task.TaskStatus.FAILED);
				getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(task);
			}
			else
				logger.warn("Leading Task null, unable update Task with failed state");

			execution.getProcessEngine().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(),
					exception.getMessage());
		}

	}

	private Target getTargetFromTargets(Targets targets, String organizationIdentifier)
	{
		return targets.getEntries().stream()
				.filter(t -> organizationIdentifier.equals(t.getTargetOrganizationIdentifierValue())).findFirst()
				.orElseThrow();
	}

	@Override
	protected void addErrorMessageToLeadingTask(Task task, String errorMessage)
	{
		// Do noting because error is handled in "handleSendTaskError"
	}
}
