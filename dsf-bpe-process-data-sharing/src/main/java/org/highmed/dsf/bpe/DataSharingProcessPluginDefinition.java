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
import org.highmed.dsf.fhir.resources.NamingSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;

import ca.uhn.fhir.context.FhirContext;

public class DataSharingProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.4.0";

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

		var cF = CodeSystemResource.file("fhir/CodeSystem/highmed-data-sharing.xml");
		var cQT = CodeSystemResource.file("fhir/CodeSystem/highmed-query-type.xml");

		var n = NamingSystemResource.file("fhir/NamingSystem/highmed-research-study-identifier.xml");

		var sExtG = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-extension-group-id.xml");
		var sExtPartMeDic = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-extension-participating-medic.xml");
		var sExtPartTtp = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-extension-participating-ttp.xml");
		var sExtQ = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-extension-query.xml");
		var sG = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-group.xml");
		var sR = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-research-study-data-sharing.xml");
		var sTCom = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-compute-data-sharing.xml");
		var sTExe = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-execute-data-sharing.xml");
		var sTErr = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-error-data-sharing.xml");
		var sTResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-data-sharing.xml");
		var sTReq = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-request-data-sharing.xml");
		var sTResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-data-sharing.xml");

		var vF = ValueSetResource.file("fhir/ValueSet/highmed-data-sharing.xml");
		var vQT = ValueSetResource.file("fhir/ValueSet/highmed-query-type.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				"computeDataSharing/" + VERSION, Arrays.asList(aCom, cF, sExtG, sG, sTCom, sTResS, vF),
				"executeDataSharing/" + VERSION, Arrays.asList(aExe, cF, cQT, n, sR, sExtPartMeDic, sExtPartTtp, sExtQ, sG, sTExe, vF, vQT),
				"requestDataSharing/" + VERSION, Arrays.asList(aReq, cF, cQT, n, sExtG, sExtPartMeDic, sExtPartTtp, sG, sR, sExtQ, sTReq, sTResM, sTErr, vF, vQT));

		return ResourceProvider
				.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader,
						resourcesByProcessKeyAndVersion);
	}
}
