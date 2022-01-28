package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.DataSharingSerializerConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilityMpcConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilityMpcSerializerConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class FeasibilityMpcProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.6.0";

	private static final String DEPENDENCY_DATA_SHARING_VERSION = "0.6.0";
	private static final String DEPENDENCY_DATA_SHARING_NAME_AND_VERSION = "dsf-bpe-process-data-sharing-0.6.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-feasibility-mpc";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/requestFeasibilityMpc.bpmn", "bpe/executeFeasibilityMpcSingleShare.bpmn",
				"bpe/executeFeasibilityMpcMultiShare.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(FeasibilityMpcConfig.class, FeasibilityMpcSerializerConfig.class,
				DataSharingSerializerConfig.class);
	}

	@Override
	public List<String> getDependencyNamesAndVersions()
	{
		return Arrays.asList(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		var aExeM = ActivityDefinitionResource
				.file("fhir/ActivityDefinition/highmed-executeFeasibilityMpcMultiShare.xml");
		var aExeS = ActivityDefinitionResource
				.file("fhir/ActivityDefinition/highmed-executeFeasibilityMpcSingleShare.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestFeasibilityMpc.xml");

		var cDS = CodeSystemResource.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION,
				"http://highmed.org/fhir/CodeSystem/data-sharing", DEPENDENCY_DATA_SHARING_VERSION);

		var sTExeM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-feasibility-mpc-multi-share.xml");
		var sTExeS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-feasibility-mpc-single-share.xml");
		var sTReq = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-request-feasibility-mpc.xml");
		var sTResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-share-feasibility-mpc.xml");
		var sTResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-share-feasibility-mpc.xml");

		var vDS = ValueSetResource.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION,
				"http://highmed.org/fhir/ValueSet/data-sharing", DEPENDENCY_DATA_SHARING_VERSION);

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				"highmedorg_executeFeasibilityMpcMultiShare/" + VERSION, Arrays.asList(aExeM, cDS, sTExeM, sTResS, vDS),
				"highmedorg_executeFeasibilityMpcSingleShare/" + VERSION, Arrays.asList(aExeS, cDS, sTExeS, vDS),
				"highmedorg_requestFeasibilityMpc/" + VERSION, Arrays.asList(aReq, cDS, sTReq, sTResM, vDS));

		return ResourceProvider.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resolver, resourcesByProcessKeyAndVersion);
	}
}
