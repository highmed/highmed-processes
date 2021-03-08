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

public class DownloadResultSets extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResultSets.class);

	private final ObjectMapper openEhrObjectMapper;

	public DownloadResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ObjectMapper openEhrObjectMapper)
	{
		super(clientProvider, taskHelper);
		this.openEhrObjectMapper = openEhrObjectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(openEhrObjectMapper, "openEhrObjectMapper");
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		List<QueryResult> resultsWithResultSets = download(results);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(resultsWithResultSets)));
	}

	private List<QueryResult> download(QueryResults results)
	{
		return results.getResults().stream().map(this::download).collect(Collectors.toList());
	}

	private QueryResult download(QueryResult result)
	{
		IdType id = new IdType(result.getResultSetUrl());
		FhirWebserviceClient client = getFhirWebserviceClientProvider().getRemoteWebserviceClient(id.getBaseUrl());

		InputStream binary = readBinaryResource(client, id.getIdPart());
		ResultSet resultSet = deserializeResultSet(binary);

		return QueryResult.idResult(result.getOrganizationIdentifier(), result.getCohortId(), resultSet);
	}

	private InputStream readBinaryResource(FhirWebserviceClient client, String id)
	{
		try
		{
			logger.info("Reading binary from {} with id {}", client.getBaseUrl(), id);
			return client.readBinary(id, MediaType.valueOf(OPENEHR_MIMETYPE_JSON));
		}	
		catch (Exception e)
		{
			logger.warn("Error while reading Binary resource: " + e.getMessage(), e);
			throw e;
		}
	}

	private ResultSet deserializeResultSet(InputStream content)
	{
		try (content)
		{
			return openEhrObjectMapper.readValue(content, ResultSet.class);
		}
		catch (IOException e)
		{
			logger.warn("Error while deserializing ResultSet: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
