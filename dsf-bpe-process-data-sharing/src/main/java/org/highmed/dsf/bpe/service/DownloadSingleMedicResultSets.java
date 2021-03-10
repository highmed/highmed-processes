package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.OPENEHR_MIMETYPE_JSON;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.openehr.model.structure.ResultSet;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DownloadSingleMedicResultSets extends DownloadResultSets
{
	public DownloadSingleMedicResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ObjectMapper openEhrObjectMapper)
	{
		super(clientProvider, taskHelper, openEhrObjectMapper);
	}

	@Override
	protected QueryResults getQueryResults(DelegateExecution execution)
	{
		return (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
	}
}
