package org.highmed.dsf.bpe.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.highmed.dsf.fhir.json.ObjectMapperFactory;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

public class TargetsSerializationTest
{
	private static final Logger logger = LoggerFactory.getLogger(TargetsSerializationTest.class);

	@Test
	public void serializeDeserializeTargets() throws Exception
	{
		ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		Target target = Target.createUniDirectionalTarget("identifier", "endpoint");
		Targets targets = new Targets(List.of(target));

		String serialized = objectMapper.writeValueAsString(targets);

		logger.debug("Targets: '{}'", serialized);

		Targets deserialized = objectMapper.readValue(serialized, Targets.class);

		assertNotNull(deserialized);
		assertEquals(1, deserialized.getEntries().size());
		assertNotNull(deserialized.getEntries().get(0));
		assertEquals(deserialized.getEntries().get(0).getTargetOrganizationIdentifierValue(),
				target.getTargetOrganizationIdentifierValue());
		assertEquals(deserialized.getEntries().get(0).getTargetEndpointUrl(), target.getTargetEndpointUrl());
		assertEquals(deserialized.getEntries().get(0).getCorrelationKey(), target.getCorrelationKey());
	}
}
