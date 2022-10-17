package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsFeasibility.PROCESS_NAME_FULL_COMPUTE_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROCESS_NAME_FULL_EXECUTE_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROCESS_NAME_FULL_REQUEST_FEASIBILITY;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.DataSharingSerializerConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilityConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilitySerializerConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class FeasibilityProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.7.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 10, 18);

	private static final String DEPENDENCY_DATA_SHARING_VERSION = "0.7.0";
	private static final String DEPENDENCY_DATA_SHARING_NAME_AND_VERSION = "dsf-bpe-process-data-sharing-0.7.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-feasibility";
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
		return Stream.of("bpe/requestFeasibility.bpmn", "bpe/computeFeasibility.bpmn", "bpe/executeFeasibility.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(FeasibilityConfig.class, FeasibilitySerializerConfig.class, DataSharingSerializerConfig.class);
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

		var aCom = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-computeFeasibility.xml");
		var aExe = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-executeFeasibility.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestFeasibility.xml");

		var sCom = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-compute-feasibility.xml");
		var sErr = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-error-feasibility.xml");
		var sExe = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-execute-feasibility.xml");
		var sResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-feasibility.xml");
		var sReq = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-request-feasibility.xml");
		var sResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-feasibility.xml");

		var v = ValueSetResource.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION,
				"http://highmed.org/fhir/ValueSet/data-sharing", DEPENDENCY_DATA_SHARING_VERSION);

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				PROCESS_NAME_FULL_COMPUTE_FEASIBILITY + "/" + VERSION, Arrays.asList(c, aCom, sCom, sResS, v),
				PROCESS_NAME_FULL_EXECUTE_FEASIBILITY + "/" + VERSION, Arrays.asList(c, aExe, sExe, v),
				PROCESS_NAME_FULL_REQUEST_FEASIBILITY + "/" + VERSION, Arrays.asList(c, aReq, sReq, sResM, sErr, v));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
