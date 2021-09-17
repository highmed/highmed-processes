package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DownloadSingleMedicResultSets extends DownloadResultSets
{
	public DownloadSingleMedicResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, ObjectMapper openEhrObjectMapper)
	{
		super(clientProvider, taskHelper, readAccessHelper, openEhrObjectMapper);
	}

	@Override
	protected List<QueryResult> getQueryResults(DelegateExecution execution)
	{
		return ((QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS)).getResults();
	}
}
