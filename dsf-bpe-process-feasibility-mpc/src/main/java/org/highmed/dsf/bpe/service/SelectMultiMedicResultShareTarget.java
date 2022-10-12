package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_ROLE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_MEDIC;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_HIGHMED_CONSORTIUM;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.BPMN_EXECUTION_VARIABLE_CORRELATION_KEY;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Identifier;
import org.springframework.beans.factory.InitializingBean;

public class SelectMultiMedicResultShareTarget extends AbstractServiceDelegate implements InitializingBean
{
	private final EndpointProvider endpointProvider;

	public SelectMultiMedicResultShareTarget(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		String identifier = getLeadingTaskFromExecutionVariables(execution).getRequester().getIdentifier().getValue();
		String correlationKey = (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_CORRELATION_KEY);

		Endpoint endpoint = endpointProvider.getFirstConsortiumEndpoint(
				NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_HIGHMED_CONSORTIUM, CODESYSTEM_HIGHMED_ORGANIZATION_ROLE,
				CODESYSTEM_HIGHMED_ORGANIZATION_ROLE_VALUE_MEDIC, identifier).get();

		Target medicTarget = Target.createBiDirectionalTarget(identifier, getEndpointIdentifierValue(endpoint),
				endpoint.getAddress(), correlationKey);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET, TargetValues.create(medicTarget));
	}

	private String getEndpointIdentifierValue(Endpoint endpoint)
	{
		return endpoint.getIdentifier().stream()
				.filter(i -> NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER.equals(i.getSystem())).findFirst()
				.map(Identifier::getValue).get();
	}
}
