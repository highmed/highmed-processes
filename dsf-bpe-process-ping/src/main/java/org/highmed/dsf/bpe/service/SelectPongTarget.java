package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SelectPongTarget extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPongTarget.class);

	private final EndpointProvider endpointProvider;

	public SelectPongTarget(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, EndpointProvider endpointProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.endpointProvider = endpointProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(endpointProvider, "endpointProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables(execution);

		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).get();
		String targetOrganizationIdentifierValue = task.getRequester().getIdentifier().getValue();
		String targetEndpointIdentifierValue = getEndpointIdentifierValue(task);

		String targetEndpointAddress = endpointProvider.getEndpointAddress(targetEndpointIdentifierValue)
				.orElseThrow(() ->
				{
					logger.warn(
							"Pong response target (organization {}, endpoint {}) not found locally or not active, not sending pong",
							targetOrganizationIdentifierValue, targetEndpointIdentifierValue);
					return new BpmnError("target_not_allowed");
				});

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET,
				TargetValues.create(Target.createBiDirectionalTarget(targetOrganizationIdentifierValue,
						targetEndpointIdentifierValue, targetEndpointAddress, correlationKey)));
	}

	private String getEndpointIdentifierValue(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterReferenceValue(task, CODESYSTEM_HIGHMED_PING,
						CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER)
				.map(Reference::getIdentifier).map(Identifier::getValue).get();
	}
}
