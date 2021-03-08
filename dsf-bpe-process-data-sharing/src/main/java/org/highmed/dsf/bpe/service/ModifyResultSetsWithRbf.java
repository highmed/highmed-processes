package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.BloomFilterConfig;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtp;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpImpl;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ModifyResultSetsWithRbf extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ModifyResultSetsWithRbf.class);

	private static final int RBF_LENGTH = 3000;
	private static final RecordBloomFilterGeneratorImpl.FieldWeights FBF_WEIGHTS = new RecordBloomFilterGeneratorImpl.FieldWeights(
			0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
	private static final RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths FBF_LENGTHS = new RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths(
			500, 500, 250, 50, 500, 250, 500, 500, 500);

	private final OrganizationProvider organizationProvider;
	private final String ehrIdColumnPath;
	private final MasterPatientIndexClient masterPatientIndexClient;
	private final BouncyCastleProvider bouncyCastleProvider;

	public ModifyResultSetsWithRbf(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, String ehrIdColumnPath,
			MasterPatientIndexClient masterPatientIndexClient, BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
		this.ehrIdColumnPath = ehrIdColumnPath;
		this.masterPatientIndexClient = masterPatientIndexClient;
		this.bouncyCastleProvider = bouncyCastleProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");
		Objects.requireNonNull(masterPatientIndexClient, "masterPatientIndexClient");
		Objects.requireNonNull(bouncyCastleProvider, "bouncyCastleProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		String organizationIdentifier = organizationProvider.getLocalIdentifierValue();
			String researchStudyIdentifier = getResearchStudyIdentifier(execution);
		SecretKey mdatKey = (SecretKey) execution.getVariable(BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY);
		BloomFilterConfig bloomFilterConfig = (BloomFilterConfig) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG);

		ResultSetTranslatorToTtp resultSetTranslator = createResultSetTranslator(organizationIdentifier,
				researchStudyIdentifier, mdatKey, bloomFilterConfig);

		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> translatedResults = results.getResults().stream()
				.map(result -> translateAndCreateBinary(resultSetTranslator, result)).collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(translatedResults)));
	}

	protected String getResearchStudyIdentifier(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY);
		return researchStudy.getIdentifier().stream()
				.filter(s -> s.getSystem().equals(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Identifier is not set in research study with id='"
						+ researchStudy.getId() + "', this error should have been caught by resource validation"))
				.getValue();
	}

	protected ResultSetTranslatorToTtp createResultSetTranslator(String organizationIdentifier,
			String researchStudyIdentifier, SecretKey mdatKey, BloomFilterConfig bloomFilterConfig)
			throws NoSuchAlgorithmException
	{
		RecordBloomFilterGenerator recordBloomFilterGenerator = createRecordBloomFilterGenerator(
				bloomFilterConfig.getPermutationSeed(), bloomFilterConfig.getHmacSha2Key(),
				bloomFilterConfig.getHmacSha3Key());

		// TODO: should be provided by properties or pseudonym provider
		SecretKey idatKey = AesGcmUtil.generateAES256Key();

		return new ResultSetTranslatorToTtpImpl(organizationIdentifier, idatKey, researchStudyIdentifier, mdatKey,
				ehrIdColumnPath, recordBloomFilterGenerator, masterPatientIndexClient);
	}

	protected RecordBloomFilterGenerator createRecordBloomFilterGenerator(long permutationSeed, Key hmacSha2Key,
			Key hmacSha3Key)
	{
		return new RecordBloomFilterGeneratorImpl(RBF_LENGTH, permutationSeed, FBF_WEIGHTS, FBF_LENGTHS,
				() -> new BloomFilterGenerator.HmacSha2HmacSha3BiGramHasher(hmacSha2Key, hmacSha3Key,
						bouncyCastleProvider));
	}

	private QueryResult translateAndCreateBinary(ResultSetTranslatorToTtp resultSetTranslator, QueryResult result)
	{
		ResultSet translatedResultSet = translate(resultSetTranslator, result.getResultSet());
		return QueryResult.idResult(result.getOrganizationIdentifier(), result.getCohortId(), translatedResultSet);
	}

	private ResultSet translate(ResultSetTranslatorToTtp resultSetTranslator, ResultSet resultSet)
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
}
