package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsLocalServices.PROCESS_NAME_FULL_LOCAL_SERVICES_INTEGRATION;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.DataSharingSerializerConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilitySerializerConfig;
import org.highmed.dsf.bpe.spring.config.LocalServicesConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class LocalServicesProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.7.0";
	public static final LocalDate RELEASE_DATE = LocalDate.of(2022, 10, 18);

	private static final String DEPENDENCY_DATA_SHARING_VERSION = "0.7.0";
	private static final String DEPENDENCY_DATA_SHARING_NAME_AND_VERSION = "dsf-bpe-process-data-sharing-0.7.0";
	private static final String DEPENDENCY_FEASIBILITY_NAME_AND_VERSION = "dsf-bpe-process-feasibility-0.7.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-local-services";
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
		return Stream.of("bpe/localServicesIntegration.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(LocalServicesConfig.class, FeasibilitySerializerConfig.class,
				DataSharingSerializerConfig.class);
	}

	@Override
	public List<String> getDependencyNamesAndVersions()
	{
		return Arrays.asList(DEPENDENCY_FEASIBILITY_NAME_AND_VERSION, DEPENDENCY_DATA_SHARING_NAME_AND_VERSION);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		var c = CodeSystemResource.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION,
				"http://highmed.org/fhir/CodeSystem/data-sharing", DEPENDENCY_DATA_SHARING_VERSION);

		var a = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-localServicesIntegration.xml");

		var s = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-local-services-integration.xml");

		var v = ValueSetResource.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION,
				"http://highmed.org/fhir/ValueSet/data-sharing", DEPENDENCY_DATA_SHARING_VERSION);

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map
				.of(PROCESS_NAME_FULL_LOCAL_SERVICES_INTEGRATION + "/" + VERSION, Arrays.asList(c, a, s, v));

		return ResourceProvider.read(VERSION, RELEASE_DATE,
				() -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader, resolver,
				resourcesByProcessKeyAndVersion);
	}
}
