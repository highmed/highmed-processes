package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDICS;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.UnsignedIntType;

public class CheckMultiMedicResults extends AbstractServiceDelegate
{
	public CheckMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task currentTask = getCurrentTaskFromExecutionVariables(execution);
		Task leadingTask = getLeadingTaskFromExecutionVariables(execution);

		addFinalFeasibilityQueryErrorsToLeadingTask(currentTask, leadingTask);

		FinalFeasibilityQueryResults results = readFinalFeasibilityQueryResultsFromCurrentTask(currentTask);
		FinalFeasibilityQueryResults checkedResults = checkResults(results);

		addFinalFeasibilityQueryResultsToLeadingTask(checkedResults, leadingTask);

		// The current task finishes here but is not automatically set to completed
		// because it is an additional task during the execution of the main process
		currentTask.setStatus(Task.TaskStatus.COMPLETED);
		getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(currentTask);
	}

	private void addFinalFeasibilityQueryErrorsToLeadingTask(Task toRead, Task toWrite)
	{
		toRead.getInput().stream()
				.filter(in -> in.hasType() && in.getType().hasCoding()
						&& CODESYSTEM_HIGHMED_BPMN.equals(in.getType().getCodingFirstRep().getSystem())
						&& CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR.equals(in.getType().getCodingFirstRep().getCode()))
				.forEach(in -> toWrite.getOutput().add(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN,
						CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, in.getValue().primitiveValue())));
	}

	private FinalFeasibilityQueryResults readFinalFeasibilityQueryResultsFromCurrentTask(Task task)
	{
		List<FinalFeasibilityQueryResult> results = task.getInput().stream()
				.filter(in -> in.hasType() && in.getType().hasCoding()
						&& CODESYSTEM_HIGHMED_DATA_SHARING.equals(in.getType().getCodingFirstRep().getSystem())
						&& CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT
								.equals(in.getType().getCodingFirstRep().getCode()))
				.map(in -> toResult(task, in)).collect(Collectors.toList());
		return new FinalFeasibilityQueryResults(results);
	}

	private FinalFeasibilityQueryResult toResult(Task task, ParameterComponent in)
	{
		String cohortId = ((Reference) in.getExtensionByUrl(EXTENSION_HIGHMED_GROUP_ID).getValue()).getReference();
		int participatingMedics = getParticipatingMedicsCountByCohortId(task, cohortId);
		int cohortSize = ((UnsignedIntType) in.getValue()).getValue();
		return new FinalFeasibilityQueryResult(cohortId, participatingMedics, cohortSize);
	}

	private int getParticipatingMedicsCountByCohortId(Task task, String cohortId)
	{
		return task.getInput().stream().filter(in -> in.hasType() && in.getType().hasCoding()
				&& CODESYSTEM_HIGHMED_DATA_SHARING.equals(in.getType().getCodingFirstRep().getSystem())
				&& CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDICS
						.equals(in.getType().getCodingFirstRep().getCode())
				&& cohortId.equals(
						((Reference) in.getExtensionByUrl(EXTENSION_HIGHMED_GROUP_ID).getValue()).getReference()))
				.mapToInt(in -> ((UnsignedIntType) in.getValue()).getValue()).findFirst().getAsInt();
	}

	protected FinalFeasibilityQueryResults checkResults(FinalFeasibilityQueryResults results)
	{
		// TODO implement check for results
		// - criterias tbd
		return results;
	}

	private void addFinalFeasibilityQueryResultsToLeadingTask(FinalFeasibilityQueryResults results, Task toWrite)
	{
		results.getResults().forEach(result -> addResultOutput(result, toWrite));
	}

	private void addResultOutput(FinalFeasibilityQueryResult result, Task toWrite)
	{
		TaskOutputComponent output1 = getTaskHelper().createOutputUnsignedInt(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT, result.getCohortSize());
		output1.addExtension(createCohortIdExtension(result.getCohortId()));
		toWrite.addOutput(output1);

		TaskOutputComponent output2 = getTaskHelper().createOutputUnsignedInt(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDICS, result.getParticipatingMedics());
		output2.addExtension(createCohortIdExtension(result.getCohortId()));
		toWrite.addOutput(output2);
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(EXTENSION_HIGHMED_GROUP_ID, new Reference(cohortId));
	}
}
