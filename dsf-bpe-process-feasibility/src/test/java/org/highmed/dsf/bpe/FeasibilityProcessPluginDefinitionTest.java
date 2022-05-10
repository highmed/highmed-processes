package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsFeasibility.PROCESS_NAME_FULL_COMPUTE_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROCESS_NAME_FULL_EXECUTE_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROCESS_NAME_FULL_REQUEST_FEASIBILITY;
import static org.highmed.dsf.bpe.FeasibilityProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;

public class FeasibilityProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition dataSharing = new DataSharingProcessPluginDefinition();
		ResourceProvider dataSharingProvider = dataSharing.getResourceProvider(FhirContext.forR4(),
				getClass().getClassLoader(), new StandardEnvironment());
		assertNotNull(dataSharingProvider);

		ProcessPluginDefinition definition = new FeasibilityProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		var computeFeasibility = provider.getResources(PROCESS_NAME_FULL_COMPUTE_FEASIBILITY + "/" + VERSION,
				s -> dataSharingProvider);
		assertNotNull(computeFeasibility);
		assertEquals(5, computeFeasibility.count());

		var executeFeasibilityMpcSingleShare = provider
				.getResources(PROCESS_NAME_FULL_EXECUTE_FEASIBILITY + "/" + VERSION, s -> dataSharingProvider);
		assertNotNull(executeFeasibilityMpcSingleShare);
		assertEquals(4, executeFeasibilityMpcSingleShare.count());

		var requestFeasibilityMpc = provider.getResources(PROCESS_NAME_FULL_REQUEST_FEASIBILITY + "/" + VERSION,
				s -> dataSharingProvider);
		assertNotNull(requestFeasibilityMpc);
		assertEquals(6, requestFeasibilityMpc.count());
	}
}
