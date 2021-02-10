package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TTP_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.OPENEHR_MIMETYPE_JSON;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RBF_RESULTS;

import java.security.Key;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.BloomFilterConfig;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpRbfOnly;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpRbfOnlyImpl;
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

public class GenerateBloomFilters extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(GenerateBloomFilters.class);

	private static final int RBF_LENGTH = 3000;
	private static final RecordBloomFilterGeneratorImpl.FieldWeights FBF_WEIGHTS = new RecordBloomFilterGeneratorImpl.FieldWeights(
			0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
	private static final RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths FBF_LENGTHS = new RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths(
			500, 500, 250, 50, 500, 250, 500, 500, 500);

	private final String ehrIdColumnPath;
	private final MasterPatientIndexClient masterPatientIndexClient;
	private final ObjectMapper openEhrObjectMapper;
	private final BouncyCastleProvider bouncyCastleProvider;

	public GenerateBloomFilters(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			String ehrIdColumnPath, MasterPatientIndexClient masterPatientIndexClient, ObjectMapper openEhrObjectMapper,
			BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper);

		this.ehrIdColumnPath = ehrIdColumnPath;
		this.masterPatientIndexClient = masterPatientIndexClient;
		this.openEhrObjectMapper = openEhrObjectMapper;
		this.bouncyCastleProvider = bouncyCastleProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");
		Objects.requireNonNull(masterPatientIndexClient, "masterPatientIndexClient");
		Objects.requireNonNull(openEhrObjectMapper, "openEhrObjectMapper");
		Objects.requireNonNull(bouncyCastleProvider, "bouncyCastleProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		String securityIdentifier = (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_TTP_IDENTIFIER);
		BloomFilterConfig bloomFilterConfig = (BloomFilterConfig) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG);

		ResultSetTranslatorToTtpRbfOnly resultSetTranslator = createResultSetTranslator(bloomFilterConfig);

		List<QueryResult> translatedResults = results.getResults().stream()
				.map(result -> translateAndCreateBinary(resultSetTranslator, result, securityIdentifier))
				.collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_RBF_RESULTS,
				QueryResultsValues.create(new QueryResults(translatedResults)));
	}

	protected ResultSetTranslatorToTtpRbfOnly createResultSetTranslator(BloomFilterConfig bloomFilterConfig)
	{
		return new ResultSetTranslatorToTtpRbfOnlyImpl(ehrIdColumnPath,
				createRecordBloomFilterGenerator(bloomFilterConfig.getPermutationSeed(),
						bloomFilterConfig.getHmacSha2Key(), bloomFilterConfig.getHmacSha3Key()),
				masterPatientIndexClient, ResultSetTranslatorToTtpRbfOnlyImpl.FILTER_ON_IDAT_NOT_FOUND_EXCEPTION);
	}

	protected RecordBloomFilterGenerator createRecordBloomFilterGenerator(long permutationSeed, Key hmacSha2Key,
			Key hmacSha3Key)
	{
		return new RecordBloomFilterGeneratorImpl(RBF_LENGTH, permutationSeed, FBF_WEIGHTS, FBF_LENGTHS,
				() -> new BloomFilterGenerator.HmacSha2HmacSha3BiGramHasher(hmacSha2Key, hmacSha3Key,
						bouncyCastleProvider));
	}

	private QueryResult translateAndCreateBinary(ResultSetTranslatorToTtpRbfOnly resultSetTranslator,
			QueryResult result, String ttpIdentifier)
	{
		ResultSet translatedResultSet = translate(resultSetTranslator, result.getResultSet());
		String resultSetUrl = saveResultSetAsBinaryForTtp(translatedResultSet, ttpIdentifier);

		return QueryResult.resultSet(result.getOrganizationIdentifier(), result.getCohortId(), resultSetUrl);
	}

	private ResultSet translate(ResultSetTranslatorToTtpRbfOnly resultSetTranslator, ResultSet resultSet)
	{
		try
		{
			return resultSetTranslator.translate(resultSet);
		}
		catch (Exception e)
		{
			logger.warn("Error while translating ResultSet: " + e.getMessage(), e);
			throw e;
		}
	}

	protected String saveResultSetAsBinaryForTtp(ResultSet resultSet, String securityIdentifier)
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
		catch (JsonProcessingException e)
		{
			logger.warn("Error while serializing ResultSet: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private IdType createBinaryResource(Binary binary)
	{
		try
		{
			return getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().create(binary);
		}
		catch (Exception e)
		{
			logger.debug("Binary to create {}", FhirContext.forR4().newJsonParser().encodeResourceToString(binary));
			logger.warn("Error while creating Binary resoruce: " + e.getMessage(), e);
			throw e;
		}
	}
}
