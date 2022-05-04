package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROCESS_NAME_FULL_DOWNLOAD_ALLOW_LIST;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROCESS_NAME_FULL_UPDATE_ALLOW_LIST;
import static org.highmed.dsf.bpe.UpdateAllowListProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;

public class UpdateAllowListProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition definition = new UpdateAllowListProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		var download = provider.getResources(PROCESS_NAME_FULL_DOWNLOAD_ALLOW_LIST + "/" + VERSION,
				s -> ResourceProvider.empty());
		assertNotNull(download);
		assertEquals(4, download.count());

		var update = provider.getResources(PROCESS_NAME_FULL_UPDATE_ALLOW_LIST + "/" + VERSION,
				s -> ResourceProvider.empty());
		assertNotNull(update);
		assertEquals(4, update.count());
	}
}
