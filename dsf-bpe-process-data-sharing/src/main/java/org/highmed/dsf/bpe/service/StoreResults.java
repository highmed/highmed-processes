package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.OPENEHR_MIMETYPE_JSON;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.ResultSet;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

public abstract class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(StoreResults.class);

	private final ObjectMapper openEhrObjectMapper;

	public StoreResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, ObjectMapper openEhrObjectMapper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
		this.openEhrObjectMapper = openEhrObjectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(openEhrObjectMapper, "openEhrObjectMapper");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		String securityIdentifier = getSecurityIdentifier(execution);

		List<QueryResult> savedResults = saveQueryResults(results, securityIdentifier);
		List<QueryResult> postProcessedResults = postProcessStoredResults(savedResults, execution);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(postProcessedResults)));
	}

	/**
	 * @param execution
	 *            the bpmn process execution environment
	 * @return the identifier of the organization that should have read access to the stored Binary resource from the
	 *         local FHIR server
	 */
	protected abstract String getSecurityIdentifier(DelegateExecution execution);

	/**
	 * @param storedResults
	 *            the {@link QueryResult} objects after storing them to the local FHIR server
	 * @param execution
	 *            the bpmn process execution environment
	 * @return the {@link QueryResult} objects after post processing
	 */
	protected List<QueryResult> postProcessStoredResults(List<QueryResult> storedResults, DelegateExecution execution)
	{
		return storedResults;
	}

	private List<QueryResult> saveQueryResults(QueryResults results, String securityIdentifier)
	{
		return results.getResults().stream().map(result -> saveResultSetAsBinary(result, securityIdentifier))
				.collect(Collectors.toList());
	}

	private QueryResult saveResultSetAsBinary(QueryResult result, String securityIdentifier)
	{
		String binaryId = save(result.getResultSet(), securityIdentifier);
		return QueryResult.idResult(result.getOrganizationIdentifier(), result.getCohortId(), binaryId);
	}

	private String save(ResultSet resultSet, String securityIdentifier)
	{
		byte[] content = serializeResultSet(resultSet);
		Reference securityContext = new Reference();
		securityContext.setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue(securityIdentifier);
		Binary binary = new Binary().setContentType(OPENEHR_MIMETYPE_JSON).setSecurityContext(securityContext)
				.setData(content);

		IdType created = createBinaryResource(binary);
		return new IdType(getFhirWebserviceClientProvider().getLocalBaseUrl(), ResourceType.Binary.name(),
				created.getIdPart(), created.getVersionIdPart()).getValue();
	}

	private byte[] serializeResultSet(ResultSet resultSet)
	{
		try
		{
			return openEhrObjectMapper.writeValueAsBytes(resultSet);
		}
		catch (JsonProcessingException exception)
		{
			logger.warn("Error while serializing ResultSet: " + exception.getMessage());
			throw new RuntimeException(exception);
		}
	}

	private IdType createBinaryResource(Binary binary)
	{
		try
		{
			return getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().create(binary);
		}
		catch (Exception exception)
		{
			logger.debug("Binary to create {}", FhirContext.forR4().newJsonParser().encodeResourceToString(binary));
			logger.warn("Error while creating Binary resoruce: " + exception.getMessage());
			throw exception;
		}
	}
}
