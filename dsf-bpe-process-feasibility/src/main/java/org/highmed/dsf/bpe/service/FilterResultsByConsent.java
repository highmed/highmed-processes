package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.consent.client.ConsentClient;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class FilterResultsByConsent extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(FilterResultsByConsent.class);

	private final ConsentClient consentClient;

	public FilterResultsByConsent(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, ConsentClient consentClient)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.consentClient = consentClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(consentClient, "consentClient");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> checkedResults = checkResults(results);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(checkedResults)));
	}

	private List<QueryResult> checkResults(QueryResults results)
	{
		return results.getResults().stream().map(result -> checkResult(result)).collect(Collectors.toList());
	}

	private QueryResult checkResult(QueryResult result)
	{
		ResultSet checkedResultSet = check(result.getResultSet());
		return QueryResult.idResult(result.getOrganizationIdentifier(), result.getCohortId(), checkedResultSet);
	}

	private ResultSet check(ResultSet resultSet)
	{
		try
		{
			return consentClient.check(resultSet);
		}
		catch (Exception exception)
		{
			logger.warn("Error while checking ResultSet: " + exception.getMessage(), exception);
			throw exception;
		}
	}
}
