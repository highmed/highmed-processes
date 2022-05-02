package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_TASK;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_MISSING;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.util.PingResponseGenerator;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogNoResponse extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogNoResponse.class);

	private final PingResponseGenerator responseGenerator;

	public LogNoResponse(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, PingResponseGenerator responseGenerator)
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
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getLeadingTaskFromExecutionVariables();

		Targets targets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);
		targets.getEntries().forEach(t -> logAndAddResponseToTask(task, t));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK, FhirResourceValues.create(task));
	}

	private void logAndAddResponseToTask(Task task, Target target)
	{
		logger.warn("PONG from organization {} (endpoint {}) missing", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue());

		task.addOutput(responseGenerator.createOutput(target, CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_MISSING));
	}
}
