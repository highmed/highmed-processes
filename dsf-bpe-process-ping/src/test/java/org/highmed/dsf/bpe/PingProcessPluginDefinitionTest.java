package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_NAME_FULL_PING;
import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_NAME_FULL_PING_AUTOSTART;
import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_NAME_FULL_PONG;
import static org.highmed.dsf.bpe.PingProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;

public class PingProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition definition = new PingProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		var ping = provider.getResources(PROCESS_NAME_FULL_PING + "/" + VERSION, s -> ResourceProvider.empty());
		assertNotNull(ping);
		assertEquals(8, ping.count());

		var pingAutostart = provider.getResources(PROCESS_NAME_FULL_PING_AUTOSTART + "/" + VERSION,
				s -> ResourceProvider.empty());
		assertNotNull(pingAutostart);
		assertEquals(5, pingAutostart.count());

		var pong = provider.getResources(PROCESS_NAME_FULL_PONG + "/" + VERSION, s -> ResourceProvider.empty());
		assertNotNull(pong);
		assertEquals(7, pong.count());
	}
}
