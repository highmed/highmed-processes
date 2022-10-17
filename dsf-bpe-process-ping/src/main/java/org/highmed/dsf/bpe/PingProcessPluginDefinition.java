package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_NAME_FULL_PING;
import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_NAME_FULL_PING_AUTOSTART;
import static org.highmed.dsf.bpe.ConstantsPing.PROCESS_NAME_FULL_PONG;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.PingConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class PingProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.7.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 10, 18);

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
		return Stream.of("bpe/ping-autostart.bpmn", "bpe/ping.bpmn", "bpe/pong.bpmn");
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
		var aPingAutostart = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-ping-autostart.xml");
		var aPong = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-pong.xml");

		var cPing = CodeSystemResource.file("fhir/CodeSystem/highmed-ping.xml");
		var cPingStatus = CodeSystemResource.file("fhir/CodeSystem/highmed-ping-status.xml");

		var sPingStatus = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-extension-ping-status.xml");
		var sPing = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-ping.xml");
		var sPong = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-pong.xml");
		var sStartPing = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-start-ping.xml");
		var sStartPingAutostart = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-start-ping-autostart.xml");
		var sStopPingAutostart = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-stop-ping-autostart.xml");

		var vPing = ValueSetResource.file("fhir/ValueSet/highmed-ping.xml");
		var vPingStatus = ValueSetResource.file("fhir/ValueSet/highmed-ping-status.xml");
		var vPongStatus = ValueSetResource.file("fhir/ValueSet/highmed-pong-status.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				PROCESS_NAME_FULL_PING + "/" + VERSION,
				Arrays.asList(aPing, cPing, cPingStatus, sPingStatus, sStartPing, sPong, vPing, vPingStatus),
				PROCESS_NAME_FULL_PING_AUTOSTART + "/" + VERSION,
				Arrays.asList(aPingAutostart, cPing, sStartPingAutostart, sStopPingAutostart, vPing),
				PROCESS_NAME_FULL_PONG + "/" + VERSION,
				Arrays.asList(aPong, cPing, cPingStatus, sPingStatus, sPing, vPing, vPongStatus));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
