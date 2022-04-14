package org.highmed.dsf.bpe;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.PingConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class PingProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.6.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 4, 14);

	@Override
	public String getName()
	{
		return "dsf-bpe-process-ping";
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
		return Stream.of("bpe/ping.bpmn", "bpe/pong.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(PingConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		var aPing = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-ping.xml");
		var aPong = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-pong.xml");
		var tPing = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-pong.xml");
		var tStartPing = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-start-ping-process.xml");
		var tPong = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-ping.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of("highmedorg_ping/" + VERSION,
				Arrays.asList(aPing, tPong, tStartPing), "highmedorg_pong/" + VERSION, Arrays.asList(aPong, tPing));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
