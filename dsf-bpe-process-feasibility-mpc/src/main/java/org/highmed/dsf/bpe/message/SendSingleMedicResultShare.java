package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SHARE;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class SendSingleMedicResultShare extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(SendSingleMedicResultShare.class);

	public SendSingleMedicResultShare(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		// TODO: use calculated single share
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		return results.getResults().stream().map(this::toInput);
	}

	private Task.ParameterComponent toInput(QueryResult result)
	{
		ParameterComponent input = getTaskHelper().createInputUnsignedInt(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SHARE, result.getCohortSize());
		input.addExtension(createCohortIdExtension(result.getCohortId()));

		return input;
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(EXTENSION_HIGHMED_GROUP_ID, new Reference(cohortId));
	}
}
