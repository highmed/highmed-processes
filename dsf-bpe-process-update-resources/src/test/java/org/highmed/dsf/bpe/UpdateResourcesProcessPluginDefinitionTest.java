package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROCESS_NAME_FULL_EXECUTE_UPDATE_RESOURCES;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROCESS_NAME_FULL_REQUEST_UPDATE_RESOURCES;
import static org.highmed.dsf.bpe.UpdateResourcesProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;

public class UpdateResourcesProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition definition = new UpdateResourcesProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		var exec = provider.getResources(PROCESS_NAME_FULL_EXECUTE_UPDATE_RESOURCES + "/" + VERSION,
				s -> ResourceProvider.empty());
		assertNotNull(exec);
		assertEquals(4, exec.count());


		var req = provider.getResources(PROCESS_NAME_FULL_REQUEST_UPDATE_RESOURCES + "/" + VERSION,
				s -> ResourceProvider.empty());
		assertNotNull(req);
		assertEquals(4, req.count());
	}
}
