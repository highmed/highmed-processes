package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERIES;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.bpe.variable.QueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.model.structure.ResultSet;
import org.springframework.beans.factory.InitializingBean;

public class ExecuteQueries extends AbstractServiceDelegate implements InitializingBean
{
	private final OpenEhrClient openehrClient;
	private final OrganizationProvider organizationProvider;

	public ExecuteQueries(FhirWebserviceClientProvider clientProvider, OpenEhrClient openehrClient,
			TaskHelper taskHelper, OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.openehrClient = openehrClient;
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(openehrClient, "openehrClient");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		// <groupId, query>
		@SuppressWarnings("unchecked")
		Map<String, String> queries = (Map<String, String>) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERIES);

		Boolean needsConsentCheck = (Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK);
		Boolean needsRecordLinkage = (Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);
		boolean idQuery = Boolean.TRUE.equals(needsConsentCheck) || Boolean.TRUE.equals(needsRecordLinkage);

		List<QueryResult> results = queries.entrySet().stream()
				.map(entry -> executeQuery(entry.getKey(), entry.getValue(), idQuery)).collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(results)));
	}

	private QueryResult executeQuery(String cohortId, String cohortQuery, boolean idQuery)
	{
		// TODO We might want to introduce a more complex result type to represent a count,
		// errors and possible meta-data.

		ResultSet resultSet = openehrClient.query(cohortQuery, null);

		if (idQuery)
		{
			return QueryResult.idResult(organizationProvider.getLocalIdentifierValue(), cohortId, resultSet);
		}
		else
		{
			int count = Integer.parseInt(resultSet.getRow(0).get(0).getValueAsString());
			return QueryResult.countResult(organizationProvider.getLocalIdentifierValue(), cohortId, count);
		}
	}
}
