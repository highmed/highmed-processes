package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsDataSharing.PROCESS_NAME_FULL_COMPUTE_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROCESS_NAME_FULL_EXECUTE_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROCESS_NAME_FULL_REQUEST_DATA_SHARING;
import static org.highmed.dsf.bpe.DataSharingProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;

public class DataSharingProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition definition = new DataSharingProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		var computeDataSharing = provider.getResources(PROCESS_NAME_FULL_COMPUTE_DATA_SHARING + "/" + VERSION,
				s -> ResourceProvider.empty());
		assertNotNull(computeDataSharing);
		assertEquals(5, computeDataSharing.count());

		var executeDataSharing = provider.getResources(PROCESS_NAME_FULL_EXECUTE_DATA_SHARING + "/" + VERSION,
				s -> ResourceProvider.empty());
		assertNotNull(executeDataSharing);
		assertEquals(5, executeDataSharing.count());

		var requestDataSharing = provider.getResources(PROCESS_NAME_FULL_REQUEST_DATA_SHARING + "/" + VERSION,
				s -> ResourceProvider.empty());
		assertNotNull(requestDataSharing);
		assertEquals(7, requestDataSharing.count());
	}
}
