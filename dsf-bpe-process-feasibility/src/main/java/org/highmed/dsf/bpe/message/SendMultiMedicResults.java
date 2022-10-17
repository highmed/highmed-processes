package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDICS;

import java.util.List;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;

public class SendMultiMedicResults extends AbstractTaskMessageSend
{
	public SendMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		FinalFeasibilityQueryResults results = (FinalFeasibilityQueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS);

		Stream<ParameterComponent> resultInputs = results.getResults().stream().flatMap(this::toInputs);
		Stream<ParameterComponent> errorInput = getErrorInput(execution);

		return Stream.concat(resultInputs, errorInput);
	}

	private Stream<ParameterComponent> toInputs(FinalFeasibilityQueryResult result)
	{
		ParameterComponent input1 = getTaskHelper().createInputUnsignedInt(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT, result.getCohortSize());
		input1.addExtension(createCohortIdExtension(result.getCohortId()));

		ParameterComponent input2 = getTaskHelper().createInputUnsignedInt(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDICS, result.getParticipatingMedics());
		input2.addExtension(createCohortIdExtension(result.getCohortId()));

		return Stream.of(input1, input2);
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(EXTENSION_HIGHMED_GROUP_ID, new Reference(cohortId));
	}

	private Stream<ParameterComponent> getErrorInput(DelegateExecution execution)
	{
		List<Task.TaskOutputComponent> outputs = getLeadingTaskFromExecutionVariables(execution).getOutput();

		if (hasErrorOutput(outputs))
		{
			Task task = getLeadingTaskFromExecutionVariables(execution);

			String taskUrl = new Reference(new IdType(getFhirWebserviceClientProvider().getLocalBaseUrl() + "/Task",
					task.getIdElement().getIdPart())).getReference();

			Task.ParameterComponent input = getTaskHelper().createInput(CODESYSTEM_HIGHMED_BPMN,
					CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
					"Errors occurred for missing cohorts while calculating their multi medic feasibility "
							+ "result, see task with url='" + taskUrl + "'");
			return Stream.of(input);
		}

		return Stream.empty();
	}

	private boolean hasErrorOutput(List<Task.TaskOutputComponent> outputs)
	{
		return outputs.stream()
				.anyMatch(output -> output.getType().getCoding().stream()
						.anyMatch(coding -> coding.getSystem().equals(CODESYSTEM_HIGHMED_BPMN)
								&& coding.getCode().equals(CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR)));
	}
}