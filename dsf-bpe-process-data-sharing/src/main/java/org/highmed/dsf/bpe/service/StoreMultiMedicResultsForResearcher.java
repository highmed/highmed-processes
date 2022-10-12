package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_MEDIC_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SET_REFERENCE;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StoreMultiMedicResultsForResearcher extends StoreResults
{
	public StoreMultiMedicResultsForResearcher(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, ObjectMapper openEhrObjectMapper)
	{
		super(clientProvider, taskHelper, readAccessHelper, openEhrObjectMapper);
	}

	@Override
	protected String getSecurityIdentifier(DelegateExecution execution)
	{
		return (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_LEADING_MEDIC_IDENTIFIER);
	}

	@Override
	protected List<QueryResult> postProcessStoredResults(List<QueryResult> storedResults, DelegateExecution execution)
	{
		Task currentTask = getCurrentTaskFromExecutionVariables(execution);
		Task leadingTask = getLeadingTaskFromExecutionVariables(execution);

		storedResults.forEach(result -> addOutput(leadingTask, result));

		// The current task finishes here but is not automatically set to completed
		// because it is an additional task during the execution of the main process
		currentTask.setStatus(Task.TaskStatus.COMPLETED);
		getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(currentTask);

		return storedResults;
	}

	private void addOutput(Task task, QueryResult result)
	{
		Task.TaskOutputComponent output = getTaskHelper().createOutput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SET_REFERENCE,
				new Reference(result.getResultSetUrl()));
		output.addExtension(createCohortIdExtension(result.getCohortId()));
		task.addOutput(output);
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(EXTENSION_HIGHMED_GROUP_ID, new Reference(cohortId));
	}
}
