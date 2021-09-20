package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.BloomFilterConfig;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtp;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpCreateRbf;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpCreateRbfImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class GenerateBloomFilters extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(GenerateBloomFilters.class);

	private static final int RBF_LENGTH = 3000;
	private static final RecordBloomFilterGeneratorImpl.FieldWeights FBF_WEIGHTS = new RecordBloomFilterGeneratorImpl.FieldWeights(
			0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
	private static final RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths FBF_LENGTHS = new RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths(
			500, 500, 250, 50, 500, 250, 500, 500, 500);

	private final MasterPatientIndexClient masterPatientIndexClient;
	private final String ehrIdColumnPath;
	private final BouncyCastleProvider bouncyCastleProvider;

	public GenerateBloomFilters(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, MasterPatientIndexClient masterPatientIndexClient,
			String ehrIdColumnPath, BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.masterPatientIndexClient = masterPatientIndexClient;
		this.ehrIdColumnPath = ehrIdColumnPath;
		this.bouncyCastleProvider = bouncyCastleProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(masterPatientIndexClient, "masterPatientIndexClient");
		Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");
		Objects.requireNonNull(bouncyCastleProvider, "bouncyCastleProvided");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		RecordBloomFilterGenerator recordBloomFilterGenerator = createRecordBloomFilterGenerator(execution);
		ResultSetTranslatorToTtpCreateRbf translator = createResultSetTranslator(ehrIdColumnPath,
				recordBloomFilterGenerator, masterPatientIndexClient);

		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> translatedResults = translateResults(translator, results);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(translatedResults)));
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

	private ResultSetTranslatorToTtpCreateRbf createResultSetTranslator(String ehrIdColumnPath,
			RecordBloomFilterGenerator recordBloomFilterGenerator, MasterPatientIndexClient masterPatientIndexClient)
	{
		return new ResultSetTranslatorToTtpCreateRbfImpl(ehrIdColumnPath, recordBloomFilterGenerator,
				masterPatientIndexClient);
	}

	private List<QueryResult> translateResults(ResultSetTranslatorToTtp translator, QueryResults results)
	{
		return results.getResults().stream().map(result -> translateResult(translator, result))
				.collect(Collectors.toList());
	}

	private QueryResult translateResult(ResultSetTranslatorToTtp translator, QueryResult result)
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
		catch (Exception exception)
		{
			logger.warn("Error while generating bloom filter: " + exception.getMessage(), exception);
			throw exception;
		}
	}
}
