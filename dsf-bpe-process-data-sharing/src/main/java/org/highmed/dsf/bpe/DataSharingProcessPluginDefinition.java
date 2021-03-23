package org.highmed.dsf.bpe;

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

import ca.uhn.fhir.context.FhirContext;

public class DataSharingProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.5.0";

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
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		var aCom = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-computeDataSharing.xml");
		var aExe = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-executeDataSharing.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestDataSharing.xml");

		var cDS = CodeSystemResource.file("fhir/CodeSystem/highmed-data-sharing.xml");

		var sRSDS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-research-study-data-sharing.xml");
		var sTCom = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-compute-data-sharing.xml");
		var sTExe = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-execute-data-sharing.xml");
		var sTErrM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-error-data-sharing.xml");
		var sTResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-data-sharing.xml");
		var sTReq = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-request-data-sharing.xml");
		var sTResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-data-sharing.xml");

		var vDS = ValueSetResource.file("fhir/ValueSet/highmed-data-sharing.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("computeDataSharing/" + VERSION,
				Arrays.asList(aCom, cDS, sTCom, sTResS, vDS), "executeDataSharing/" + VERSION,
				Arrays.asList(aExe, cDS, sTExe, sRSDS, vDS), "requestDataSharing/" + VERSION,
				Arrays.asList(aReq, cDS, sTReq, sRSDS, sTResM, sTErrM, vDS));

		return ResourceProvider.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resourcesByProcessKeyAndVersion);
	}
}
