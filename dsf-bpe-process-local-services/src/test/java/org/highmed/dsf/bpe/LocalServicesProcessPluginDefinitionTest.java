package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsLocalServices.*;
import static org.highmed.dsf.bpe.LocalServicesProcessPluginDefinition.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;

public class LocalServicesProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition dataSharing = new DataSharingProcessPluginDefinition();
		ResourceProvider dataSharingProvider = dataSharing.getResourceProvider(FhirContext.forR4(),
				getClass().getClassLoader(), new StandardEnvironment());
		assertNotNull(dataSharingProvider);

		ProcessPluginDefinition feasibility = new FeasibilityProcessPluginDefinition();
		ResourceProvider feasibilityProvider = feasibility.getResourceProvider(FhirContext.forR4(),
				getClass().getClassLoader(), new StandardEnvironment());
		assertNotNull(feasibilityProvider);

		ProcessPluginDefinition definition = new LocalServicesProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		var localServicesIntegration = provider
				.getResources(PROCESS_NAME_FULL_LOCAL_SERVICES_INTEGRATION + "/" + VERSION, s -> dataSharingProvider);
		assertNotNull(localServicesIntegration);
		assertEquals(4, localServicesIntegration.count());
	}
}
