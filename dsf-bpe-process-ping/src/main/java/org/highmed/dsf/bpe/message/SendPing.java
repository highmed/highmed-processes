package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_ALLOWED;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_REACHABLE;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.mail.ErrorMailService;
import org.highmed.dsf.bpe.util.PingStatusGenerator;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;

public class SendPing extends AbstractTaskMessageSend
{
	private final PingStatusGenerator statusGenerator;
	private final ErrorMailService errorLogger;

	public SendPing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext,
			PingStatusGenerator statusGenerator, ErrorMailService errorLogger)
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
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		return Stream.of(getTaskHelper().createInput(CODESYSTEM_HIGHMED_PING,
				CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER,
				new Reference().setIdentifier(getLocalEndpointIdentifier()).setType(ResourceType.Endpoint.name())));
	}

	@Override
	protected void handleSendTaskError(DelegateExecution execution, Exception exception, String errorMessage)
	{
		Target target = getTarget(execution);
		Task task = getLeadingTaskFromExecutionVariables(execution);

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
			updateLeadingTaskInExecutionVariables(execution, task);

			if (CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode))
				errorLogger.endpointNotReachableForPing(task.getIdElement(), target, specialErrorMessage);
			else if (CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
				errorLogger.endpointReachablePingForbidden(task.getIdElement(), target, specialErrorMessage);
		}

		super.handleSendTaskError(execution, exception, errorMessage);
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

	private Identifier getLocalEndpointIdentifier()
	{
		Bundle bundle = getFhirWebserviceClientProvider().getLocalWebserviceClient().search(Endpoint.class,
				Map.of("address", Collections.singletonList(getFhirWebserviceClientProvider().getLocalBaseUrl())));
		return bundle.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.filter(e -> e.getResource() instanceof Endpoint).map(e -> (Endpoint) e.getResource()).findFirst()
				.filter(e -> e.hasIdentifier())
				.flatMap(e -> e.getIdentifier().stream()
						.filter(i -> NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER.equals(i.getSystem())).findFirst())
				.orElseThrow(() -> new IllegalStateException("No Identifier for Endpoint or Endpoint with address "
						+ getFhirWebserviceClientProvider().getLocalBaseUrl() + " found"));
	}
}
