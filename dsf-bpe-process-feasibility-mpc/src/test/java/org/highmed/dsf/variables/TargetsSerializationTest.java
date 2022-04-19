package org.highmed.dsf.variables;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.fhir.variables.Targets;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TargetsSerializationTest
{
	@Test
	public void serializeDeserializeTargets() throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();

		String serialized = objectMapper.writeValueAsString(new Targets(null));
		Targets deserialized = objectMapper.readValue(serialized, Targets.class);

		assertNotNull(deserialized);
	}

	@Test
	public void serializeDeserializeTargets2() throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

		String serialized = objectMapper.writeValueAsString(new Targets(null));
		Targets deserialized = objectMapper.readValue(serialized, Targets.class);

		assertNotNull(deserialized);
	}
}
