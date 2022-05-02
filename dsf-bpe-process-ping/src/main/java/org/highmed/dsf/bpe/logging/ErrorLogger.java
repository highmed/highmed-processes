package org.highmed.dsf.bpe.logging;

import org.highmed.dsf.fhir.variables.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorLogger
{
	private static final Logger pingLogger = LoggerFactory.getLogger("ping-error-logger");
	private static final Logger pongLogger = LoggerFactory.getLogger("pong-error-logger");

	public void logPingStatus(Target target, String statusCode)
	{
		pingLogger.debug("Ping: Organization {} at Endpoint {} {}", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue(), statusCode);
	}

	public void logPingStatus(Target target, String statusCode, String errorMessage)
	{
		pingLogger.debug("Ping: Organization {} at Endpoint {} {}: {}", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue(), statusCode, errorMessage);
	}

	public void logPongStatus(Target target, String statusCode, String errorMessage)
	{
		pongLogger.debug("Pong: Organization {} at Endpoint {} {}: {}", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue(), statusCode, errorMessage);
	}
}
