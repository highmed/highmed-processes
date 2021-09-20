package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1;

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

public abstract class AbstractDownloadAllowListFromTtpViaMedic1ExampleStarter
{
	protected void main(String[] args, String baseUrl, String ttpUrl) throws Exception
	{
		ExampleStarter starter = ExampleStarter.forServer(args, baseUrl);
		Task task = createStartResource(starter, ttpUrl);
		starter.startWith(task);
	}

	private Task createStartResource(ExampleStarter starter, String ttpUrl) throws Exception
	{
		Bundle allowList = getAllowList(starter, ttpUrl);

		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_AND_LATEST_VERSION);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_DOWNLOAD_ALLOW_LIST_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput()
				.setValue(new Reference(new IdType(ttpUrl, ResourceType.Bundle.name(),
						allowList.getIdElement().getIdPart(), allowList.getIdElement().getVersionIdPart())))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST)
				.setCode(CODESYSTEM_HIGHMED_UPDATE_ALLOW_LIST_VALUE_ALLOW_LIST);

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
