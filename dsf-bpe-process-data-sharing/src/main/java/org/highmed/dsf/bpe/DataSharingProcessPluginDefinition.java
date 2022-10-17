package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsDataSharing.PROCESS_NAME_FULL_COMPUTE_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROCESS_NAME_FULL_EXECUTE_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROCESS_NAME_FULL_REQUEST_DATA_SHARING;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.DataSharingConfig;
import org.highmed.dsf.bpe.spring.config.DataSharingSerializerConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class DataSharingProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.7.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 10, 18);

	@Override
	public String getName()
	{
		return "dsf-bpe-process-data-sharing";
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
		return Stream.of("bpe/requestDataSharing.bpmn", "bpe/executeDataSharing.bpmn", "bpe/computeDataSharing.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(DataSharingConfig.class, DataSharingSerializerConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		var c = CodeSystemResource.file("fhir/CodeSystem/highmed-data-sharing.xml");

		var aCom = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-computeDataSharing.xml");
		var aExe = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-executeDataSharing.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestDataSharing.xml");

		var sRSDS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-research-study-data-sharing.xml");
		var sCom = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-compute-data-sharing.xml");
		var sExe = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-execute-data-sharing.xml");
		var sErrM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-error-data-sharing.xml");
		var sResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-data-sharing.xml");
		var sReq = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-request-data-sharing.xml");
		var sResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-data-sharing.xml");

		var v = ValueSetResource.file("fhir/ValueSet/highmed-data-sharing.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				PROCESS_NAME_FULL_COMPUTE_DATA_SHARING + "/" + VERSION, Arrays.asList(c, aCom, sCom, sResS, v),
				PROCESS_NAME_FULL_EXECUTE_DATA_SHARING + "/" + VERSION, Arrays.asList(c, aExe, sExe, sRSDS, v),
				PROCESS_NAME_FULL_REQUEST_DATA_SHARING + "/" + VERSION,
				Arrays.asList(c, aReq, sReq, sRSDS, sResM, sErrM, v));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
