package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.PROCESS_NAME_FULL_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.PROCESS_NAME_FULL_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.PROCESS_NAME_FULL_REQUEST_FEASIBILITY_MPC;

import java.time.LocalDate;
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
	public static final String VERSION = "0.7.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 10, 18);

	private static final String DEPENDENCY_DATA_SHARING_VERSION = "0.7.0";
	private static final String DEPENDENCY_DATA_SHARING_NAME_AND_VERSION = "dsf-bpe-process-data-sharing-0.7.0";

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
	public LocalDate getReleaseDate()
	{
		return RELEASE_DATE;
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
		var c = CodeSystemResource.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION,
				"http://highmed.org/fhir/CodeSystem/data-sharing", DEPENDENCY_DATA_SHARING_VERSION);

		var aExeM = ActivityDefinitionResource
				.file("fhir/ActivityDefinition/highmed-executeFeasibilityMpcMultiShare.xml");
		var aExeS = ActivityDefinitionResource
				.file("fhir/ActivityDefinition/highmed-executeFeasibilityMpcSingleShare.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestFeasibilityMpc.xml");

		var sExeM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-feasibility-mpc-multi-share.xml");
		var sExeS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-feasibility-mpc-single-share.xml");
		var sReq = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-request-feasibility-mpc.xml");
		var sResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-share-feasibility-mpc.xml");
		var sResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-share-feasibility-mpc.xml");

		var v = ValueSetResource.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION,
				"http://highmed.org/fhir/ValueSet/data-sharing", DEPENDENCY_DATA_SHARING_VERSION);

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				PROCESS_NAME_FULL_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE + "/" + VERSION,
				Arrays.asList(c, aExeM, sExeM, sResS, v),
				PROCESS_NAME_FULL_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE + "/" + VERSION,
				Arrays.asList(c, aExeS, sExeS, v), PROCESS_NAME_FULL_REQUEST_FEASIBILITY_MPC + "/" + VERSION,
				Arrays.asList(c, aReq, sReq, sResM, v));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
