package org.highmed.dsf.bpe.mail;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.highmed.dsf.bpe.service.MailService;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.variables.Target;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ErrorMailService implements InitializingBean
{
	private static final Logger pingProcessErrorLogger = LoggerFactory.getLogger("ping-process-error-logger");
	private static final Logger pongProcessErrorLogger = LoggerFactory.getLogger("pong-process-error-logger");

	private static final String SUBJECT_PING_PROCESS_FAILED = "Ping Process Failed";
	private static final String SUBJECT_PONG_PROCESS_FAILED = "Pong Process Failed";

	private final String localOrganizationIdentifierValue;

	private final MailService mailService;
	private final FhirWebserviceClientProvider clientProvider;

	private final boolean sendPingProcessFailedMail;
	private final boolean sendPongProcessFailedMail;

	public ErrorMailService(MailService mailService, FhirWebserviceClientProvider clientProvider,
			String localOrganizationIdentifierValue, boolean sendPingProcessFailedMail,
			boolean sendPongProcessFailedMail)
	{
		this.mailService = mailService;
		this.clientProvider = clientProvider;
		this.localOrganizationIdentifierValue = localOrganizationIdentifierValue;

		this.sendPingProcessFailedMail = sendPingProcessFailedMail;
		this.sendPongProcessFailedMail = sendPongProcessFailedMail;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(mailService, "mailService");
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(localOrganizationIdentifierValue, "localOrganizationIdentifierValue");
	}

	private String localEndpointIdentifierValue()
	{
		Bundle result = clientProvider.getLocalWebserviceClient().searchWithStrictHandling(Endpoint.class,
				Map.of("address", Collections.singletonList(clientProvider.getLocalBaseUrl())));

		if (result.getTotal() != 1)
			return "?";

		return result.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.map(BundleEntryComponent::getResource).filter(r -> r instanceof Endpoint).map(r -> (Endpoint) r)
				.findFirst().stream().flatMap(e -> e.getIdentifier().stream())
				.filter(i -> NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER.equals(i.getSystem())).findFirst()
				.map(Identifier::getValue).orElse("?");
	}

	private String createMessage(Target target, String message, String messageDetails, IdType taskId)
	{
		StringBuilder b = new StringBuilder();

		b.append(localOrganizationIdentifierValue);
		b.append('/');
		b.append(localEndpointIdentifierValue());

		b.append(" -> ");

		b.append(target.getOrganizationIdentifierValue());
		b.append('/');
		b.append(target.getEndpointIdentifierValue());

		b.append(": ");
		b.append(message);

		if (messageDetails != null)
		{
			b.append("\n\t");
			b.append(messageDetails);
		}

		b.append("\n\nProcess started by: ");
		b.append(taskId.toVersionless().withServerBase(clientProvider.getLocalBaseUrl(), "Task").getValue());

		return b.toString();
	}

	public void pongMessageNotReceived(IdType taskId, Target target)
	{
		pingProcessErrorLogger.debug("No pong from organization '{}', Endpoint '{}' received",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue());

		if (sendPingProcessFailedMail)
		{
			mailService.send(SUBJECT_PING_PROCESS_FAILED,
					createMessage(target, "No pong message received", null, taskId));
		}
	}

	public void endpointNotReachableForPing(IdType taskId, Target target, String errorMessage)
	{
		pingProcessErrorLogger.debug("Endpoint '{}' at organization '{}' not reachable with ping: {}",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(), errorMessage);

		if (sendPingProcessFailedMail)
		{
			mailService.send(SUBJECT_PING_PROCESS_FAILED,
					createMessage(target, "Not reachable with ping", errorMessage, taskId));
		}
	}

	public void endpointReachablePingForbidden(IdType taskId, Target target, String errorMessage)
	{
		pingProcessErrorLogger.debug("Endpoint '{}' at organization '{}' reachable, ping forbidden: {}",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(), errorMessage);

		if (sendPongProcessFailedMail)
		{
			mailService.send(SUBJECT_PING_PROCESS_FAILED,
					createMessage(target, "Ping forbidden", errorMessage, taskId));
		}
	}

	public void endpointNotReachableForPong(IdType taskId, Target target, String errorMessage)
	{
		pongProcessErrorLogger.debug("Endpoint '{}' at organization '{}' not reachable with pong: {}",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(), errorMessage);

		if (sendPongProcessFailedMail)
		{
			mailService.send(SUBJECT_PONG_PROCESS_FAILED,
					createMessage(target, "Not reachable with pong", errorMessage, taskId));
		}
	}

	public void endpointReachablePongForbidden(IdType taskId, Target target, String errorMessage)
	{
		pongProcessErrorLogger.debug("Endpoint '{}' at organization '{}' reachable, pong forbidden: {}",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(), errorMessage);

		if (sendPongProcessFailedMail)
		{
			mailService.send(SUBJECT_PONG_PROCESS_FAILED,
					createMessage(target, "Pong forbidden", errorMessage, taskId));
		}
	}
}
