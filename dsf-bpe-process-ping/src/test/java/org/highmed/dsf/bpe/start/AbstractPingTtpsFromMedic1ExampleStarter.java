package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.*;

import java.util.Date;

import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public abstract class AbstractPingTtpsFromMedic1ExampleStarter
{
	protected void main(String[] args, String baseUrl) throws Exception
	{
		Task task = createStartResource();
		ExampleStarter.forServer(args, baseUrl).startWith(task);
	}

	private Task createStartResource()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_START_PING_AND_LATEST_VERSION);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_START_PING_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		task.addInput().setValue(new StringType(
				"OrganizationAffiliation?primary-organization:identifier=http://highmed.org/sid/organization-identifier|highmed.org"
						+ "&role=http://highmed.org/fhir/CodeSystem/organization-role|TTP"
						+ "&_include=OrganizationAffiliation:endpoint"))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_PING)
				.setCode(CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS);

		return task;
	}
}
