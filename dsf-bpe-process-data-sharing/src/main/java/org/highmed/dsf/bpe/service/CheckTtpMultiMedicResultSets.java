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
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckTtpMultiMedicResultSets extends CheckResultSets
{
	private static final Logger logger = LoggerFactory.getLogger(CheckTtpMultiMedicResultSets.class);

	public CheckTtpMultiMedicResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected Stream<BiFunction<QueryResult, Task, Boolean>> getChecks(QueryResult result, Task task)
	{
		// TODO: define and implement further result set checks
		return Stream.of(this::checkColumns, this::checkRows);
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
					"Could not calculate result for any cohort definition in the data sharing request with "
							+ "task-id='{}', business-key='{}' and correlation-key='{}'",
					taskId, businessKey, correlationKey);

			leadingTask.getOutput().add(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN,
					CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, "Could not calculate result for any cohort definition"));

			return false;
		}

		return true;
	}
}
