package org.highmed.dsf.bpe;

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
	public static final String VERSION = "0.6.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 4, 14);

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
		var aDown = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-downloadAllowList.xml");
		var aUp = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-updateAllowList.xml");
		var c = CodeSystemResource.file("fhir/CodeSystem/highmed-update-allow-list.xml");
		var sDown = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-download-allow-list.xml");
		var sUp = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-update-allow-list.xml");
		var v = ValueSetResource.file("fhir/ValueSet/highmed-update-allow-list.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				"highmedorg_downloadAllowList/" + VERSION, Arrays.asList(aDown, c, sDown, v),
				"highmedorg_updateAllowList/" + VERSION, Arrays.asList(aUp, c, sUp, v));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
