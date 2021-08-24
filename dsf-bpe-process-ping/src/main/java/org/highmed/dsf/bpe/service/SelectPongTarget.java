package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class SelectPongTarget extends AbstractServiceDelegate implements InitializingBean
{
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
		Task task = getCurrentTaskFromExecutionVariables();

		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).get();
		Identifier targetOrganizationIdentifier = task.getRequester().getIdentifier();

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET,
				TargetValues.create(Target.createBiDirectionalTarget(targetOrganizationIdentifier.getValue(),
						endpointProvider.getFirstDefaultEndpointAddress(targetOrganizationIdentifier.getValue()).get(),
						correlationKey)));
	}
}
