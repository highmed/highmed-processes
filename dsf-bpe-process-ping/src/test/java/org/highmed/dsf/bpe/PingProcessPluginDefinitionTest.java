package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_FULL_NAME_PING;
import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_FULL_NAME_PING_AUTOSTART;
import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_FULL_NAME_PONG;
import static org.highmed.dsf.bpe.PingProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.stream.Stream;

import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.hl7.fhir.r4.model.MetadataResource;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;

public class PingProcessPluginDefinitionTest
{
	@Test
	public void testGetResourceProvider() throws Exception
	{
		PingProcessPluginDefinition definition = new PingProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		assertResourcesStream(8,
				provider.getResources(PROCESS_FULL_NAME_PING + "/" + VERSION, s -> ResourceProvider.empty()));
		assertResourcesStream(5,
				provider.getResources(PROCESS_FULL_NAME_PING_AUTOSTART + "/" + VERSION, s -> ResourceProvider.empty()));
		assertResourcesStream(7,
				provider.getResources(PROCESS_FULL_NAME_PONG + "/" + VERSION, s -> ResourceProvider.empty()));
	}

	private void assertResourcesStream(int expectedResources, Stream<MetadataResource> resourcesStream)
	{
		assertNotNull(resourcesStream);
		assertEquals(expectedResources, resourcesStream.count());
	}
}
