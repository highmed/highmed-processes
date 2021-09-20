package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public abstract class AbstractUpdateResource3MedicTtpExampleStarter
{
	protected void main(String[] args, String baseUrl) throws Exception
	{
		ExampleStarter starter = ExampleStarter.forServer(args, baseUrl);
		Task task = createStartResource(starter, baseUrl);
		starter.startWith(task);
	}

	private Task createStartResource(ExampleStarter starter, String ttpUrl) throws Exception
	{
		Bundle allowList = getAllowList(starter, ttpUrl);

		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_AND_LATEST_VERSION);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP);

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_REQUEST_UPDATE_RESOURCES_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		task.addInput()
				.setValue(new Reference(new IdType(ResourceType.Bundle.name(), allowList.getIdElement().getIdPart(),
						allowList.getIdElement().getVersionIdPart())))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_UPDATE_RESOURCE)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE);

		task.addInput().setValue(new StringType(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|")).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_UPDATE_RESOURCE)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER);

		return task;
	}

	private Bundle getAllowList(ExampleStarter starter, String ttpUrl) throws Exception
	{
		FhirWebserviceClient client = starter.createClient(ttpUrl);
		Bundle searchResult = client.searchWithStrictHandling(Bundle.class, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allow-list|allow_list")));

		if (searchResult.getTotal() != 1 && searchResult.getEntryFirstRep().getResource() instanceof Bundle)
			throw new IllegalStateException("Expected a single allow list Bundle");

		return (Bundle) searchResult.getEntryFirstRep().getResource();
	}
}
