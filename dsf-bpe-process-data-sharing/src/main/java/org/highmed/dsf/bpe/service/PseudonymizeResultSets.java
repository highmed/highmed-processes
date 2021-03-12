package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.domain.PseudonymizedPersonWithMdat;
import org.highmed.pseudonymization.domain.impl.MatchedPersonImpl;
import org.highmed.pseudonymization.domain.impl.PseudonymizedPersonImpl;
import org.highmed.pseudonymization.psn.PseudonymGenerator;
import org.highmed.pseudonymization.psn.PseudonymGeneratorImpl;
import org.highmed.pseudonymization.psn.PseudonymizedPersonFactory;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcher;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcherImpl;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPersonFactory;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicNoRbfImpl;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToMedicImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class PseudonymizeResultSets extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(PseudonymizeResultSets.class);

	private final ObjectMapper psnObjectMapper;

	public PseudonymizeResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ObjectMapper psnObjectMapper)
	{
		super(clientProvider, taskHelper);
		this.psnObjectMapper = psnObjectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(psnObjectMapper, "psnObjectMapper");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		String researchStudyIdentifier = (String) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER);

		// TODO: store key with corresponding research study id
		SecretKey researchStudyKey = AesGcmUtil.generateAES256Key();

		Map<String, List<QueryResult>> byCohortId = groupByCohortId(results);
		QueryResults finalResults = createFinalResults(researchStudyIdentifier, researchStudyKey, byCohortId);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS, QueryResultsValues.create(finalResults));
	}

	private Map<String, List<QueryResult>> groupByCohortId(QueryResults results)
	{
		return results.getResults().stream().collect(Collectors.groupingBy(QueryResult::getCohortId));
	}

	private QueryResults createFinalResults(String researchStudyIdentifier, SecretKey reserchStudyKey,
			Map<String, List<QueryResult>> groupedResults) throws NoSuchAlgorithmException
	{
		ResultSetTranslatorFromMedic translatorFromMedic = createResultSetTranslatorFromMedic();
		ResultSetTranslatorToMedic translatorToMedic = createResultSetTranslatorToMedic();
		FederatedMatcher<PersonWithMdat> matcher = createFederatedMatcher();
		PseudonymGenerator<PersonWithMdat, PseudonymizedPersonWithMdat> psnGenerator = createPseudonymGenerator(
				researchStudyIdentifier, reserchStudyKey);

		List<QueryResult> finalResults = groupedResults.entrySet().stream().filter(r -> !r.getValue().isEmpty())
				.map(e -> translateMatchAndPseudonymize(translatorFromMedic, matcher, psnGenerator, translatorToMedic,
						e.getKey(), e.getValue()))
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
		Set<MatchedPerson<PersonWithMdat>> matchedPersons = matchPersons(persons, matcher, translatorFromMedic);
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
		catch (Exception e)
		{
			logger.warn("Error while translating ResultSet: " + e.getMessage(), e);
			throw e;
		}
	}

	private Set<MatchedPerson<PersonWithMdat>> matchPersons(List<List<PersonWithMdat>> persons,
			FederatedMatcher<PersonWithMdat> matcher, ResultSetTranslatorFromMedic translator)
	{
		if (translator instanceof ResultSetTranslatorFromMedicNoRbfImpl)
			return persons.stream().flatMap(List::stream).map(MatchedPersonImpl::new).collect(Collectors.toSet());
		else
			return matcher.matchPersons(persons);
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
		catch (Exception e)
		{
			logger.warn("Error while translating ResultSet: " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * @return
	 */
	protected abstract ResultSetTranslatorFromMedic createResultSetTranslatorFromMedic();

	private ResultSetTranslatorToMedic createResultSetTranslatorToMedic()
	{
		return new ResultSetTranslatorToMedicImpl();
	}

	private FederatedMatcher<PersonWithMdat> createFederatedMatcher()
	{
		MatchedPersonFactory<PersonWithMdat> matchedPersonFactory = MatchedPersonImpl::new;
		return new FederatedMatcherImpl<>(matchedPersonFactory);
	}

	private PseudonymGenerator<PersonWithMdat, PseudonymizedPersonWithMdat> createPseudonymGenerator(
			String researchStudyIdentifier, SecretKey researchStudyKey)
	{
		PseudonymizedPersonFactory<PersonWithMdat, PseudonymizedPersonWithMdat> psnPersonFactory = PseudonymizedPersonImpl::new;
		return new PseudonymGeneratorImpl<>(researchStudyIdentifier, researchStudyKey, psnObjectMapper,
				psnPersonFactory);
	}
}
