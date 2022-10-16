package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsPing.BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_TIMER_INTERVAL;
import static org.highmed.dsf.bpe.ConstantsPing.TIMER_INTERVAL_DEFAULT_VALUE;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetTargetAndConfigureTimer extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SetTargetAndConfigureTimer.class);

	private final OrganizationProvider organizationProvider;
	private final EndpointProvider endpointProvider;

	public SetTargetAndConfigureTimer(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider,
			EndpointProvider endpointProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.organizationProvider = organizationProvider;
		this.endpointProvider = endpointProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(endpointProvider, "endpointProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws BpmnError, Exception
	{
		String timerInterval = getTimerInterval(execution);
		logger.debug("Setting variable '{}' to {}", BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL, timerInterval);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL, Variables.stringValue(timerInterval));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET,
				TargetValues.create(Target.createUniDirectionalTarget(organizationProvider.getLocalIdentifierValue(),
						endpointProvider.getLocalEndpointIdentifier().getValue(),
						getFhirWebserviceClientProvider().getLocalBaseUrl())));
	}

	private String getTimerInterval(DelegateExecution execution)
	{
		return getTaskHelper()
				.getFirstInputParameterStringValue(getLeadingTaskFromExecutionVariables(execution),
						CODESYSTEM_HIGHMED_PING, CODESYSTEM_HIGHMED_PING_VALUE_TIMER_INTERVAL)
				.orElse(TIMER_INTERVAL_DEFAULT_VALUE);
	}
}
