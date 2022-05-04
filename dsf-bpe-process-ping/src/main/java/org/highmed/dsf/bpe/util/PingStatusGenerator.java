package org.highmed.dsf.bpe.util;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_PING_STATUS;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_PONG_STATUS;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_ERROR_MESSAGE;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_PING_STATUS;

import org.highmed.dsf.fhir.variables.Target;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;

public class PingStatusGenerator
{
	public TaskOutputComponent createPingStatusOutput(Target target, String statusCode)
	{
		return createPingStatusOutput(target, statusCode, null);
	}

	public TaskOutputComponent createPingStatusOutput(Target target, String statusCode, String errorMessage)
	{
		return createStatusOutput(target, CODESYSTEM_HIGHMED_PING_VALUE_PING_STATUS, statusCode, errorMessage);
	}

	public TaskOutputComponent createPongStatusOutput(Target target, String statusCode)
	{
		return createPongStatusOutput(target, statusCode, null);
	}

	public TaskOutputComponent createPongStatusOutput(Target target, String statusCode, String errorMessage)
	{
		return createStatusOutput(target, CODESYSTEM_HIGHMED_PING_VALUE_PONG_STATUS, statusCode, errorMessage);
	}

	private TaskOutputComponent createStatusOutput(Target target, String outputParameter, String statusCode,
			String errorMessage)
	{
		TaskOutputComponent output = new TaskOutputComponent();
		output.setValue(new Coding().setSystem(CODESYSTEM_HIGHMED_PING_STATUS).setCode(statusCode));
		output.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_PING).setCode(outputParameter);

		Extension extension = output.addExtension();
		extension.setUrl(EXTENSION_URL_PING_STATUS);
		extension.addExtension(EXTENSION_URL_CORRELATION_KEY, new StringType(target.getCorrelationKey()));
		extension.addExtension().setUrl(EXTENSION_URL_ORGANIZATION_IDENTIFIER)
				.setValue(new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
						.setValue(target.getOrganizationIdentifierValue()));
		extension.addExtension().setUrl(EXTENSION_URL_ENDPOINT_IDENTIFIER).setValue(new Identifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER).setValue(target.getEndpointIdentifierValue()));
		if (errorMessage != null)
			extension.addExtension().setUrl(EXTENSION_URL_ERROR_MESSAGE).setValue(new StringType(errorMessage));

		return output;
	}
}
