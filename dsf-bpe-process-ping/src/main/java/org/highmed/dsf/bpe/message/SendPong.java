package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_ALLOWED;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_REACHABLE;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_PONG_SEND;

import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.logging.ErrorLogger;
import org.highmed.dsf.bpe.util.PingStatusGenerator;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class SendPong extends AbstractTaskMessageSend
{
	private final PingStatusGenerator statusGenerator;
	private final ErrorLogger errorLogger;

	public SendPong(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext,
			PingStatusGenerator statusGenerator, ErrorLogger errorLogger)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);

		this.statusGenerator = statusGenerator;
		this.errorLogger = errorLogger;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(statusGenerator, "statusGenerator");
		Objects.requireNonNull(errorLogger, "errorLogger");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		super.doExecute(execution);

		Target target = getTarget();
		Task task = getLeadingTaskFromExecutionVariables();
		task.addOutput(statusGenerator.createPongStatusOutput(target, CODESYSTEM_HIGHMED_PING_STATUS_VALUE_PONG_SEND));
		updateLeadingTaskInExecutionVariables(task);
	}

	@Override
	protected void handleEndEventError(Exception exception, String errorMessage)
	{
		Target target = getTarget();
		Task task = getLeadingTaskFromExecutionVariables();

		if (task != null)
		{
			String statusCode = CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_REACHABLE;
			if (exception instanceof WebApplicationException)
			{
				WebApplicationException webApplicationException = (WebApplicationException) exception;
				if (webApplicationException.getResponse() != null && webApplicationException.getResponse()
						.getStatus() == Response.Status.FORBIDDEN.getStatusCode())
				{
					statusCode = CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_ALLOWED;
				}
			}

			String specialErrorMessage = createErrorMessage(exception);

			task.addOutput(statusGenerator.createPongStatusOutput(target, statusCode, specialErrorMessage));
			updateLeadingTaskInExecutionVariables(task);

			errorLogger.logPongStatus(target, statusCode, specialErrorMessage);
		}

		super.handleEndEventError(exception, errorMessage);
	}

	private String createErrorMessage(Exception exception)
	{
		return exception.getClass().getSimpleName()
				+ ((exception.getMessage() != null && !exception.getMessage().isBlank())
						? (": " + exception.getMessage())
						: "");
	}
}
