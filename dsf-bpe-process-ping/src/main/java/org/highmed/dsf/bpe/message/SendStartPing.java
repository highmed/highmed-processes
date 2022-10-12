package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class SendStartPing extends AbstractTaskMessageSend
{
	public SendStartPing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		return getLeadingTaskFromExecutionVariables(execution).getInput().stream()
				.filter(Task.ParameterComponent::hasType)
				.filter(i -> i.getType().getCoding().stream()
						.anyMatch(c -> CODESYSTEM_HIGHMED_PING.equals(c.getSystem())
								&& CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS.equals(c.getCode())));
	}
}
