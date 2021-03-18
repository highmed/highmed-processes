package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;
import static org.highmed.pseudonymization.crypto.AesGcmUtil.AES;

import java.security.Key;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.crypto.KeyProvider;
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
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtp;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class TranslateSingleMedicResultSets extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(TranslateSingleMedicResultSets.class);

	private static final int RBF_LENGTH = 3000;
	private static final RecordBloomFilterGeneratorImpl.FieldWeights FBF_WEIGHTS = new RecordBloomFilterGeneratorImpl.FieldWeights(
			0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
	private static final RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths FBF_LENGTHS = new RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths(
			500, 500, 250, 50, 500, 250, 500, 500, 500);

	private final OrganizationProvider organizationProvider;
	private final KeyProvider keyProvider;
	private final String ehrIdColumnPath;
	private final MasterPatientIndexClient masterPatientIndexClient;
	private final BouncyCastleProvider bouncyCastleProvider;

	public TranslateSingleMedicResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, KeyProvider keyProvider, String ehrIdColumnPath,
			MasterPatientIndexClient masterPatientIndexClient, BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
		this.keyProvider = keyProvider;
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

		if (!(this instanceof TranslateSingleMedicResultSetsWithoutRbf))
		{
			Objects.requireNonNull(masterPatientIndexClient, "masterPatientIndexClient");
			Objects.requireNonNull(bouncyCastleProvider, "bouncyCastleProvider");
		}
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		String organizationIdentifier = organizationProvider.getLocalIdentifierValue();
		SecretKey idatKey = getIdatKey(organizationIdentifier);
		String researchStudyIdentifier = getResearchStudyIdentifier(execution);
		SecretKey mdatKey = getMdatKey(execution);
		boolean needsRecordLinkage = (boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);
		RecordBloomFilterGenerator bloomFilterGenerator = needsRecordLinkage
				? createRecordBloomFilterGenerator(execution)
				: null;

		ResultSetTranslatorToTtp translator = createResultSetTranslator(organizationIdentifier, idatKey,
				researchStudyIdentifier, mdatKey, ehrIdColumnPath, masterPatientIndexClient, bloomFilterGenerator);

		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> translatedResults = translateResults(translator, results);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(translatedResults)));
	}

	private SecretKey getIdatKey(String organizationIdentifier)
	{
		return (SecretKey) keyProvider.get(organizationIdentifier);
	}

	private String getResearchStudyIdentifier(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY);
		return researchStudy.getIdentifier().stream()
				.filter(s -> s.getSystem().equals(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Identifier is not set in research study with id='"
						+ researchStudy.getId() + "', this error should have been caught by resource validation"))
				.getValue();
	}

	private SecretKey getMdatKey(DelegateExecution execution)
	{
		byte[] encodedKey = (byte[]) execution.getVariable(BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY);
		return new SecretKeySpec(encodedKey, AES);
	}

	private RecordBloomFilterGenerator createRecordBloomFilterGenerator(DelegateExecution execution)
	{
		BloomFilterConfig bloomFilterConfig = (BloomFilterConfig) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG);
		return new RecordBloomFilterGeneratorImpl(RBF_LENGTH, bloomFilterConfig.getPermutationSeed(), FBF_WEIGHTS,
				FBF_LENGTHS,
				() -> new BloomFilterGenerator.HmacSha2HmacSha3BiGramHasher(bloomFilterConfig.getHmacSha2Key(),
						bloomFilterConfig.getHmacSha3Key(), bouncyCastleProvider));
	}

	/**
	 * @param organizationIdentifier
	 *            the FHIR identifier of the current organization
	 * @param idatKey
	 *            the key to encrypt the IDAT
	 * @param researchStudyIdentifier
	 *            the FHIR identifier of the ResearchStudy defining the data sharing request
	 * @param mdatKey
	 *            the key provided by the leading MeDIC to encrypt the MDAT
	 * @param ehrIdColumnPath
	 *            the path to the external subject id column
	 * @param masterPatientIndexClient
	 *            the client to retrieve the {@link org.highmed.mpi.client.Idat} from the MPI, may be null
	 * @param recordBloomFilterGenerator
	 *            the generator to create RBFs based on the {@link org.highmed.mpi.client.Idat} provided by the
	 *            masterPatientIndexClient, may be null
	 * @return {@link ResultSetTranslatorToTtp}
	 */
	abstract protected ResultSetTranslatorToTtp createResultSetTranslator(String organizationIdentifier,
			SecretKey idatKey, String researchStudyIdentifier, SecretKey mdatKey, String ehrIdColumnPath,
			MasterPatientIndexClient masterPatientIndexClient, RecordBloomFilterGenerator recordBloomFilterGenerator);

	private List<QueryResult> translateResults(ResultSetTranslatorToTtp translator, QueryResults results)
	{
		return results.getResults().stream().map(result -> translateAndCreateBinary(translator, result))
				.collect(Collectors.toList());
	}

	private QueryResult translateAndCreateBinary(ResultSetTranslatorToTtp translator, QueryResult result)
	{
		ResultSet translatedResultSet = translate(translator, result.getResultSet());
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
