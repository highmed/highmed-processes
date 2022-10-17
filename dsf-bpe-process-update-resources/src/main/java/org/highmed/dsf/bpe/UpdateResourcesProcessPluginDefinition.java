package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROCESS_NAME_FULL_EXECUTE_UPDATE_RESOURCES;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROCESS_NAME_FULL_REQUEST_UPDATE_RESOURCES;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.UpdateResourcesConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class UpdateResourcesProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.7.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 10, 18);

	@Override
	public String getName()
	{
		return "dsf-bpe-process-update-resources";
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
		return Stream.of("bpe/executeUpdateResources.bpmn", "bpe/requestUpdateResources.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(UpdateResourcesConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		var c = CodeSystemResource.file("fhir/CodeSystem/highmed-update-resources.xml");

		var aExec = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-executeUpdateResources.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestUpdateResources.xml");

		var sExec = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-execute-update-resources.xml");
		var sReq = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-request-update-resources.xml");

		var v = ValueSetResource.file("fhir/ValueSet/highmed-update-resources.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				PROCESS_NAME_FULL_EXECUTE_UPDATE_RESOURCES + "/" + VERSION, Arrays.asList(c, aExec, sExec, v),
				PROCESS_NAME_FULL_REQUEST_UPDATE_RESOURCES + "/" + VERSION, Arrays.asList(c, aReq, sReq, v));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
