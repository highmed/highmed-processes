package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.client.PseudonymizationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class PseudonymizeResultsFirstOrder extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(PseudonymizeResultsFirstOrder.class);

	private final PseudonymizationClient pseudonymizationClient;

	public PseudonymizeResultsFirstOrder(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, PseudonymizationClient pseudonymizationClient)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.pseudonymizationClient = pseudonymizationClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(pseudonymizationClient, "pseudonymizationClient");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		List<QueryResult> pseudonymizedResults = pseudonymizeResults(results);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(pseudonymizedResults)));
	}

	private List<QueryResult> pseudonymizeResults(QueryResults results)
	{
		return results.getResults().stream().map(result -> pseudonymizeResult(result)).collect(Collectors.toList());
	}

	private QueryResult pseudonymizeResult(QueryResult result)
	{
		ResultSet pseudonymizedResultSet = pseudonymize(result.getResultSet());
		return QueryResult.idResult(result.getOrganizationIdentifier(), result.getCohortId(), pseudonymizedResultSet);
	}

	private ResultSet pseudonymize(ResultSet resultSet)
	{
		try
		{
			return pseudonymizationClient.pseudonymize(resultSet);
		}
		catch (Exception exception)
		{
			logger.warn("Error while encrypting ResultSet: " + exception.getMessage());
			throw exception;
		}
	}
}
