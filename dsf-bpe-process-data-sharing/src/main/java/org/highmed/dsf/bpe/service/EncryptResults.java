package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.crypto.KeyProvider;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.bpe.variable.SecretKeyWrapper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtp;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpEncrypt;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpEncryptImpl;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class EncryptResults extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(EncryptResults.class);

	private final OrganizationProvider organizationProvider;
	private final KeyProvider keyProvider;
	private final String ehrIdColumnPath;

	public EncryptResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, KeyProvider keyProvider,
			String ehrIdColumnPath)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.organizationProvider = organizationProvider;
		this.keyProvider = keyProvider;
		this.ehrIdColumnPath = ehrIdColumnPath;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(keyProvider, "keyProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		String organizationIdentifier = organizationProvider.getLocalIdentifierValue();
		SecretKey idatKey = getIdatKey(organizationIdentifier);
		String researchStudyIdentifier = getResearchStudyIdentifier(execution);
		SecretKey mdatKey = getMdatKey(execution);

		ResultSetTranslatorToTtpEncrypt translator = createResultSetTranslator(organizationIdentifier, idatKey,
				researchStudyIdentifier, mdatKey, ehrIdColumnPath);

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
		SecretKeyWrapper secretKeyWrapper = (SecretKeyWrapper) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY);
		return secretKeyWrapper.getSecretKey();
	}

	private ResultSetTranslatorToTtpEncrypt createResultSetTranslator(String organizationIdentifier,
			SecretKey organizationKey, String researchStudyIdentifier, SecretKey researchStudyKey,
			String ehrIdColumnPath)
	{
		return new ResultSetTranslatorToTtpEncryptImpl(organizationIdentifier, organizationKey, researchStudyIdentifier,
				researchStudyKey, ehrIdColumnPath);
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
			logger.warn("Error while encrypting ResultSet: " + exception.getMessage());
			throw exception;
		}
	}
}
