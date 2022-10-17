package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_PONG_RECEIVED;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.util.PingStatusGenerator;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogPong extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogPong.class);

	private final PingStatusGenerator responseGenerator;

	public LogPong(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, PingStatusGenerator responseGenerator)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.responseGenerator = responseGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(responseGenerator, "responseGenerator");
	}

	@Override
	public void doExecute(DelegateExecution execution)
	{
		Target target = getTarget(execution);

		logger.info("PONG from {} (endpoint: {})", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue());

		Task leading = getLeadingTaskFromExecutionVariables(execution);
		leading.addOutput(
				responseGenerator.createPingStatusOutput(target, CODESYSTEM_HIGHMED_PING_STATUS_VALUE_PONG_RECEIVED));
		updateLeadingTaskInExecutionVariables(execution, leading);

		Targets targets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);
		targets = targets.removeByEndpointIdentifierValue(target.getEndpointIdentifierValue());
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(targets));
	}

	private Target getTarget(DelegateExecution execution)
	{
		return (Target) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGET);
	}
}
