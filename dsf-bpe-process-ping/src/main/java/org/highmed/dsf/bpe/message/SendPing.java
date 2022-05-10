package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_ALLOWED;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_REACHABLE;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER;

import java.util.Objects;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.logging.ErrorLogger;
import org.highmed.dsf.bpe.util.PingStatusGenerator;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;

public class SendPing extends AbstractTaskMessageSend
{
	private final EndpointProvider endpointProvider;
	private final PingStatusGenerator statusGenerator;
	private final ErrorLogger errorLogger;

	public SendPing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext,
			EndpointProvider endpointProvider, PingStatusGenerator statusGenerator, ErrorLogger errorLogger)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);

		this.endpointProvider = endpointProvider;
		this.statusGenerator = statusGenerator;
		this.errorLogger = errorLogger;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(endpointProvider, "endpointProvider");
		Objects.requireNonNull(statusGenerator, "statusGenerator");
		Objects.requireNonNull(errorLogger, "errorLogger");
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		return Stream.of(
				getTaskHelper().createInput(CODESYSTEM_HIGHMED_PING, CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER,
						new Reference().setIdentifier(endpointProvider.getLocalEndpointIdentifier())
								.setType(ResourceType.Endpoint.name())));
	}

	@Override
	protected void handleSendTaskError(Exception exception, String errorMessage)
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

			task.addOutput(statusGenerator.createPingStatusOutput(target, statusCode, specialErrorMessage));
			updateLeadingTaskInExecutionVariables(task);

			errorLogger.logPingStatus(target, statusCode, specialErrorMessage);
		}

		super.handleSendTaskError(exception, errorMessage);

	}

	@Override
	protected void addErrorMessage(Task task, String errorMessage)
	{
		// error message part of
	}

	private String createErrorMessage(Exception exception)
	{
		return exception.getClass().getSimpleName()
				+ ((exception.getMessage() != null && !exception.getMessage().isBlank())
						? (": " + exception.getMessage())
						: "");
	}
}
