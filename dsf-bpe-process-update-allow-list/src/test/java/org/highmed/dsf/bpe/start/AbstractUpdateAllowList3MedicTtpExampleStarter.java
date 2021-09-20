package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsUpdateAllowList.PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP;

import java.util.Date;

import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public abstract class AbstractUpdateAllowList3MedicTtpExampleStarter
{
	protected void main(String[] args, String baseUrl) throws Exception
	{
		Task task = createStartResource();
		ExampleStarter.forServer(args, baseUrl).startWith(task);
	}

	private Task createStartResource()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_AND_LATEST_VERSION);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP);

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_UPDATE_ALLOW_LIST_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		return task;
	}
}
