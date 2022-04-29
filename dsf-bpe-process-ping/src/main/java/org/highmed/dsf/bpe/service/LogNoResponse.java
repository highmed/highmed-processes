package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_TASK;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_MISSING;

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

public class LogNoResponse extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogNoResponse.class);

	public LogNoResponse(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);

	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getLeadingTaskFromExecutionVariables();

		Targets targets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);
		targets.getEntries().forEach(t -> logAndAddResponseToTask(task, t));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK, task);
	}

	private void logAndAddResponseToTask(Task task, Target target)
	{
		// TODO: after new Target class is available, replace "endpoint-identifier" with actual value from target
		logger.warn("PONG from organization {} (endpoint {}) missing", target.getTargetOrganizationIdentifierValue(),
				"endpoint-identifier");
		PingResponseHelper.addResponseToTask(task, target.getTargetOrganizationIdentifierValue(), "endpoint-identifier",
				CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_MISSING);
	}
}
