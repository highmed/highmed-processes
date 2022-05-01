package org.highmed.dsf.bpe.util;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_RESPONSE;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_PING_RESPONSE;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_ERROR_MESSAGE;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_PING_RESPONSE;

import org.highmed.dsf.fhir.variables.Target;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;

public class PingResponseGenerator
{
	public TaskOutputComponent createOutput(Target target, String pingResponseCode)
	{
		return createOutput(target, pingResponseCode, null);
	}

	public TaskOutputComponent createOutput(Target target, String pingResponseCode, String errorMessage)
	{
		TaskOutputComponent output = new TaskOutputComponent();
		output.setValue(new Coding().setSystem(CODESYSTEM_HIGHMED_PING_RESPONSE).setCode(pingResponseCode));
		output.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_PING)
				.setCode(CODESYSTEM_HIGHMED_PING_VALUE_PING_RESPONSE);

		Extension extension = output.addExtension();
		extension.setUrl(EXTENSION_URL_PING_RESPONSE);
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
