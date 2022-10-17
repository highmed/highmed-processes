package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogPing extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogPing.class);

	public LogPing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables(execution);

		logger.info("PING from {} (endpoint: {})", task.getRequester().getIdentifier().getValue(),
				getEndpointIdentifierValue(task));
	}

	private String getEndpointIdentifierValue(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterReferenceValue(task, CODESYSTEM_HIGHMED_PING,
						CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER)
				.map(Reference::getIdentifier).map(Identifier::getValue).get();
	}
}
