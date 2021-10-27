package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromTtp;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromTtpImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DecryptMultiMedicResults extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DecryptMultiMedicResults.class);

	private final ObjectMapper openEhrObjectMapper;

	public DecryptMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
		String researchStudyIdentifier = (String) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER);
		SecretKey mdatKey = getMdatKey(execution);
		ResultSetTranslatorFromTtp translator = createResultSetTranslator(researchStudyIdentifier, mdatKey);

		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		QueryResults decryptedResults = decryptResults(results, translator);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS, QueryResultsValues.create(decryptedResults));
	}

	private SecretKey getMdatKey(DelegateExecution execution)
	{
		byte[] encodedKey = (byte[]) execution.getVariable(BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY);
		return new SecretKeySpec(encodedKey, "AES");
	}

	private ResultSetTranslatorFromTtp createResultSetTranslator(String researchStudyIdentifier,
			SecretKey researchStudyKey)
	{
		return new ResultSetTranslatorFromTtpImpl(researchStudyIdentifier, researchStudyKey, openEhrObjectMapper);
	}

	private QueryResults decryptResults(QueryResults results, ResultSetTranslatorFromTtp translator)
	{
		List<QueryResult> translatedResults = results
				.getResults().stream().map(result -> QueryResult.idResult(result.getOrganizationIdentifier(),
						result.getCohortId(), translate(translator, result.getResultSet())))
				.collect(Collectors.toList());

		return new QueryResults(translatedResults);
	}

	private ResultSet translate(ResultSetTranslatorFromTtp translator, ResultSet toTranslate)
	{
		try
		{
			return translator.translate(toTranslate);
		}
		catch (Exception exception)
		{
			logger.warn("Error while translating ResultSet: " + exception.getMessage());
			throw exception;
		}
	}
}
