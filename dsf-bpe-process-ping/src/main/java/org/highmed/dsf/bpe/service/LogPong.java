package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_TASK;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_RECEIVED;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.util.PingResponseHelper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogPong extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogPong.class);

	public LogPong(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution)
	{
		Task current = getCurrentTaskFromExecutionVariables();
		Task leading = getLeadingTaskFromExecutionVariables();

		Targets targets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);
		String organizationIdentifier = current.getRequester().getIdentifier().getValue();

		Target target = getTargetFromTargets(targets, organizationIdentifier);

		// TODO: after new Target class is available, replace "endpoint-identifier" with actual value from target
		logger.info("PONG from {} (endpoint: {})", target.getTargetOrganizationIdentifierValue(),
				"endpoint-identifier");
		PingResponseHelper.addResponseToTask(leading, target.getTargetOrganizationIdentifierValue(),
				"endpoint-identifier", CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_RECEIVED);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK, leading);

		targets.removeTarget(target);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, targets);
	}

	private Target getTargetFromTargets(Targets targets, String organizationIdentifier)
	{
		return targets.getEntries().stream()
				.filter(t -> organizationIdentifier.equals(t.getTargetOrganizationIdentifierValue())).findFirst()
				.orElseThrow();
	}
}
