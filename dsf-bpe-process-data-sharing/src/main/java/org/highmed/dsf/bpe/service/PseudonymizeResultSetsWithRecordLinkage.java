package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
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
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.domain.PseudonymizedPersonWithMdat;
import org.highmed.pseudonymization.domain.impl.MatchedPersonImpl;
import org.highmed.pseudonymization.domain.impl.PseudonymizedPersonImpl;
import org.highmed.pseudonymization.psn.PseudonymGeneratorImpl;
import org.highmed.pseudonymization.psn.PseudonymizedPersonFactory;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcher;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcherImpl;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPersonFactory;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicImpl;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToMedicImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymizeResultSetsWithRecordLinkage extends AbstractServiceDelegate
{

	private static final Logger logger = LoggerFactory.getLogger(PseudonymizeResultSetsWithRecordLinkage.class);

	private final ObjectMapper psnObjectMapper;

	public PseudonymizeResultSetsWithRecordLinkage(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ObjectMapper psnObjectMapper)
	{
		super(clientProvider, taskHelper);
		this.psnObjectMapper = psnObjectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		// TODO: load ResearchStudy Id from Task
		String researchStudyIdentifier = "test-id";

		// TODO: store key with research study id
		SecretKey researchStudyKey = AesGcmUtil.generateAES256Key();

		Map<String, List<QueryResult>> byCohortId = groupByCohortId(results);
		QueryResults finalResults = createFinalResults(researchStudyIdentifier, researchStudyKey, byCohortId);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS, QueryResultsValues.create(finalResults));
	}

	protected Map<String, List<QueryResult>> groupByCohortId(QueryResults results)
	{
		return results.getResults().stream().collect(Collectors.groupingBy(QueryResult::getCohortId));
	}

	protected QueryResults createFinalResults(String researchStudyIdentifier, SecretKey reserchStudyKey,
			Map<String, List<QueryResult>> groupedResults) throws NoSuchAlgorithmException
	{
		ResultSetTranslatorFromMedic translatorFromMedic = createResultSetTranslatorFromMedic();
		ResultSetTranslatorToMedic translatorToMedic = createResultSetTranslatorToMedic();
		FederatedMatcher<PersonWithMdat> matcher = createFederatedMatcher();
		PseudonymGeneratorImpl<PersonWithMdat, PseudonymizedPersonWithMdat> psnGenerator = createPseudonymGenerator(
				researchStudyIdentifier, reserchStudyKey);

		List<QueryResult> finalResults = groupedResults.entrySet().stream().filter(r -> !r.getValue().isEmpty())
				.map(e -> translateMatchAndPseudonymize(translatorFromMedic, matcher, psnGenerator, translatorToMedic,
						e.getKey(), e.getValue()))
				.collect(Collectors.toList());

		return new QueryResults(finalResults);
	}

	private QueryResult translateMatchAndPseudonymize(ResultSetTranslatorFromMedic translatorFromMedic,
			FederatedMatcher<PersonWithMdat> matcher,
			PseudonymGeneratorImpl<PersonWithMdat, PseudonymizedPersonWithMdat> psnGenerator,
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
		return results.stream().map(r -> translateFromMedic(translator, r)).collect(Collectors.toList());
	}

	private List<PersonWithMdat> translateFromMedic(ResultSetTranslatorFromMedic translator, QueryResult result)
	{
		return translator.translate(result.getOrganizationIdentifier(), result.getResultSet());
	}

	private ResultSet translateToMedicResultSet(ResultSet initialResultSet, ResultSetTranslatorToMedic translator,
			List<PseudonymizedPersonWithMdat> pseudonymizedPersons)
	{
		return translator.translate(initialResultSet.getMeta(), initialResultSet.getColumns(), pseudonymizedPersons);
	}

	private ResultSetTranslatorFromMedic createResultSetTranslatorFromMedic()
	{
		return new ResultSetTranslatorFromMedicImpl();
	}

	private ResultSetTranslatorToMedic createResultSetTranslatorToMedic()
	{
		return new ResultSetTranslatorToMedicImpl();
	}

	private FederatedMatcher<PersonWithMdat> createFederatedMatcher()
	{
		MatchedPersonFactory<PersonWithMdat> matchedPersonFactory = MatchedPersonImpl::new;
		return new FederatedMatcherImpl<>(matchedPersonFactory);
	}

	private PseudonymGeneratorImpl<PersonWithMdat, PseudonymizedPersonWithMdat> createPseudonymGenerator(
			String researchStudyIdentifier, SecretKey researchStudyKey) throws NoSuchAlgorithmException
	{
		PseudonymizedPersonFactory<PersonWithMdat, PseudonymizedPersonWithMdat> psnPersonFactory = PseudonymizedPersonImpl::new;
		return new PseudonymGeneratorImpl(researchStudyIdentifier, researchStudyKey, psnObjectMapper, psnPersonFactory);
	}
}
