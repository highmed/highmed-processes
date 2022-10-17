package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROCESS_NAME_FULL_DOWNLOAD_ALLOW_LIST;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROCESS_NAME_FULL_UPDATE_ALLOW_LIST;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.UpdateAllowListConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class UpdateAllowListProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.7.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 10, 18);

	@Override
	public String getName()
	{
		return "dsf-bpe-process-update-allow-list";
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
		return Stream.of("bpe/updateAllowList.bpmn", "bpe/downloadAllowList.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(UpdateAllowListConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		var c = CodeSystemResource.file("fhir/CodeSystem/highmed-update-allow-list.xml");

		var aDown = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-downloadAllowList.xml");
		var aUp = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-updateAllowList.xml");

		var sDown = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-download-allow-list.xml");
		var sUp = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-update-allow-list.xml");

		var v = ValueSetResource.file("fhir/ValueSet/highmed-update-allow-list.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				PROCESS_NAME_FULL_DOWNLOAD_ALLOW_LIST + "/" + VERSION, Arrays.asList(c, aDown, sDown, v),
				PROCESS_NAME_FULL_UPDATE_ALLOW_LIST + "/" + VERSION, Arrays.asList(c, aUp, sUp, v));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
