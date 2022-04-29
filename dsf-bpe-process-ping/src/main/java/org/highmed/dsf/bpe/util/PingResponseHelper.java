package org.highmed.dsf.bpe.util;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_RESPONSE;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_PING_RESPONSE;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Task;

public class PingResponseHelper
{
	public static void addResponseToTask(Task task, String organizationIdentifier, String endpointIdentifier,
			String code)
	{
		Coding value = new Coding().setSystem(CODESYSTEM_HIGHMED_PING_RESPONSE).setCode(code);
		value.addExtension().setUrl(EXTENSION_URL_ORGANIZATION_IDENTIFIER).setValue(
				new Coding().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setCode(organizationIdentifier));
		value.addExtension().setUrl(EXTENSION_URL_ENDPOINT_IDENTIFIER)
				.setValue(new Coding().setSystem(NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER).setCode(endpointIdentifier));

		task.addOutput().setValue(value).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_PING)
				.setCode(CODESYSTEM_HIGHMED_PING_VALUE_PING_RESPONSE);
	}
}
