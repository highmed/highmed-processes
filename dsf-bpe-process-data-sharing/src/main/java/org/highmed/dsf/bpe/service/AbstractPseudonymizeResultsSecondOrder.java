package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.crypto.KeyConsumer;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.domain.PseudonymizedPersonWithMdat;
import org.highmed.pseudonymization.domain.impl.PseudonymizedPersonImpl;
import org.highmed.pseudonymization.psn.PseudonymGenerator;
import org.highmed.pseudonymization.psn.PseudonymGeneratorImpl;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcher;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToMedicImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractPseudonymizeResultsSecondOrder extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractPseudonymizeResultsSecondOrder.class);

	private final KeyConsumer keyConsumer;

	private final ResultSetTranslatorFromMedic resultSetTranslatorFromMedic;
	private final FederatedMatcher<PersonWithMdat> federatedMatcher;

	private final ObjectMapper psnObjectMapper;

	public AbstractPseudonymizeResultsSecondOrder(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, KeyConsumer keyConsumer,
			ResultSetTranslatorFromMedic resultSetTranslatorFromMedic,
			FederatedMatcher<PersonWithMdat> federatedMatcher, ObjectMapper psnObjectMapper)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.keyConsumer = keyConsumer;
		this.resultSetTranslatorFromMedic = resultSetTranslatorFromMedic;
		this.federatedMatcher = federatedMatcher;
		this.psnObjectMapper = psnObjectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(keyConsumer, "keyConsumer");
		Objects.requireNonNull(resultSetTranslatorFromMedic, "resultSetTranslatorFromMedic");
		Objects.requireNonNull(federatedMatcher, "federatedMatcher");
		Objects.requireNonNull(psnObjectMapper, "psnObjectMapper");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		String researchStudyIdentifier = (String) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER);

		SecretKey researchStudyKey = createAndStoreResearchStudyKey(researchStudyIdentifier);

		Map<String, List<QueryResult>> byCohortId = groupByCohortId(results);
		QueryResults finalResults = createFinalResults(researchStudyIdentifier, researchStudyKey, byCohortId);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS, QueryResultsValues.create(finalResults));
	}

	private SecretKey createAndStoreResearchStudyKey(String researchStudyIdentifier) throws Exception
	{
		SecretKey key = AesGcmUtil.generateAES256Key();
		keyConsumer.store(researchStudyIdentifier, key);
		return key;
	}

	private Map<String, List<QueryResult>> groupByCohortId(QueryResults results)
	{
		return results.getResults().stream().collect(Collectors.groupingBy(QueryResult::getCohortId));
	}

	private QueryResults createFinalResults(String researchStudyIdentifier, SecretKey reserchStudyKey,
			Map<String, List<QueryResult>> groupedResults)
	{
		ResultSetTranslatorToMedic translatorToMedic = createResultSetTranslatorToMedic();
		PseudonymGenerator<PersonWithMdat, PseudonymizedPersonWithMdat> psnGenerator = createPseudonymGenerator(
				researchStudyIdentifier, reserchStudyKey);

		List<QueryResult> finalResults = groupedResults.entrySet().stream().filter(r -> !r.getValue().isEmpty())
				.map(e -> translateMatchAndPseudonymize(resultSetTranslatorFromMedic, federatedMatcher, psnGenerator,
						translatorToMedic, e.getKey(), e.getValue()))
				.collect(Collectors.toList());

		return new QueryResults(finalResults);
	}

	private QueryResult translateMatchAndPseudonymize(ResultSetTranslatorFromMedic translatorFromMedic,
			FederatedMatcher<PersonWithMdat> matcher,
			PseudonymGenerator<PersonWithMdat, PseudonymizedPersonWithMdat> psnGenerator,
			ResultSetTranslatorToMedic translatorToMedic, String cohortId, List<QueryResult> results)
	{
		logger.debug("Translating, matching and pseudonymizing results for cohort {}", cohortId);

		List<List<PersonWithMdat>> persons = translateFromMedicResultSets(translatorFromMedic, results);
		Set<MatchedPerson<PersonWithMdat>> matchedPersons = matcher.matchPersons(persons);
		List<PseudonymizedPersonWithMdat> pseudonymizedPersons = psnGenerator
				.createPseudonymsAndShuffle(matchedPersons);
		ResultSet resultSet = translateToMedicResultSet(results.get(0).getResultSet(), translatorToMedic,
				pseudonymizedPersons);

		return QueryResult.idResult("ttp", cohortId, resultSet);
	}

	private List<List<PersonWithMdat>> translateFromMedicResultSets(ResultSetTranslatorFromMedic translator,
			List<QueryResult> results)
	{
		return results.stream().map(r -> translateFromMedicResultSet(translator, r)).collect(Collectors.toList());
	}

	private List<PersonWithMdat> translateFromMedicResultSet(ResultSetTranslatorFromMedic translator,
			QueryResult toTranslate)
	{
		try
		{
			return translator.translate(toTranslate.getOrganizationIdentifier(), toTranslate.getResultSet());
		}
		catch (Exception exception)
		{
			logger.warn("Error while translating ResultSet: " + exception.getMessage());
			throw exception;
		}
	}

	private ResultSet translateToMedicResultSet(ResultSet toTranslate, ResultSetTranslatorToMedic translator,
			List<PseudonymizedPersonWithMdat> pseudonymizedPersons)
	{
		try
		{
			Meta meta = toTranslate.getMeta() == null ? new Meta("", "", "", "", "", "") : toTranslate.getMeta();
			List<Column> columns = toTranslate.getColumns();
			return translator.translate(meta, columns, pseudonymizedPersons);
		}
		catch (Exception exception)
		{
			logger.warn("Error while translating ResultSet: " + exception.getMessage());
			throw exception;
		}
	}

	private ResultSetTranslatorToMedic createResultSetTranslatorToMedic()
	{
		return new ResultSetTranslatorToMedicImpl();
	}

	private PseudonymGenerator<PersonWithMdat, PseudonymizedPersonWithMdat> createPseudonymGenerator(
			String researchStudyIdentifier, SecretKey researchStudyKey)
	{
		return new PseudonymGeneratorImpl<>(researchStudyIdentifier, researchStudyKey, psnObjectMapper,
				PseudonymizedPersonImpl::new);
	}
}
