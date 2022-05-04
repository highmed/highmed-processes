package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.*;
import static org.highmed.dsf.bpe.FeasibilityMpcProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;

public class FeasibilityMpcProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition dataSharing = new DataSharingProcessPluginDefinition();
		ResourceProvider dataSharingProvider = dataSharing.getResourceProvider(FhirContext.forR4(),
				getClass().getClassLoader(), new StandardEnvironment());
		assertNotNull(dataSharingProvider);

		ProcessPluginDefinition definition = new FeasibilityMpcProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		var executeFeasibilityMpcMultiShare = provider.getResources(
				PROCESS_NAME_FULL_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE + "/" + VERSION, s -> dataSharingProvider);
		assertNotNull(executeFeasibilityMpcMultiShare);
		assertEquals(5, executeFeasibilityMpcMultiShare.count());

		var executeFeasibilityMpcSingleShare = provider.getResources(
				PROCESS_NAME_FULL_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE + "/" + VERSION, s -> dataSharingProvider);
		assertNotNull(executeFeasibilityMpcSingleShare);
		assertEquals(4, executeFeasibilityMpcSingleShare.count());

		var requestFeasibilityMpc = provider.getResources(PROCESS_NAME_FULL_REQUEST_FEASIBILITY_MPC + "/" + VERSION,
				s -> dataSharingProvider);
		assertNotNull(requestFeasibilityMpc);
		assertEquals(5, requestFeasibilityMpc.count());
	}
}
