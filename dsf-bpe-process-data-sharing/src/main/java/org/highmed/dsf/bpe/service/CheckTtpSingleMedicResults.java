package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_ERROR_CODE_MULTI_MEDIC_PSEUDONYMIZED_DATA_SHARING_RESULT;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckTtpSingleMedicResults extends AbstractCheckResults
{
	private static final Logger logger = LoggerFactory.getLogger(CheckTtpSingleMedicResults.class);

	public CheckTtpSingleMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected boolean testResultAndAddPossibleError(QueryResult result, Task task)
	{
		return super.testResultAndAddPossibleError(result, task); // TODO: define further checks
	}

	@Override
	protected List<QueryResult> postProcessAllPassingResults(List<QueryResult> passedResults)
	{
		if (!atLeastOneResultExists(passedResults))
			throw new BpmnError(BPMN_EXECUTION_ERROR_CODE_MULTI_MEDIC_PSEUDONYMIZED_DATA_SHARING_RESULT);

		return passedResults;
	}

	private boolean atLeastOneResultExists(List<QueryResult> results)
	{
		Task leadingTask = getLeadingTaskFromExecutionVariables();
		String taskId = leadingTask.getId();
		String businessKey = getTaskHelper().getFirstInputParameterStringValue(leadingTask, CODESYSTEM_HIGHMED_BPMN,
				CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(leadingTask, CODESYSTEM_HIGHMED_BPMN,
				CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);

		if (results.size() < 1)
		{
			logger.warn(
					"Did not receive any valid results from any MeDIC for any cohort definition in the data sharing request with "
							+ "task-id='{}', business-key='{}' and correlation-key='{}', are there results present?",
					taskId, businessKey, correlationKey);

			leadingTask.getOutput()
					.add(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
							"Did not receive any valid results from any MeDIC for any cohort definition"));

			return false;
		}

		return true;
	}
}
