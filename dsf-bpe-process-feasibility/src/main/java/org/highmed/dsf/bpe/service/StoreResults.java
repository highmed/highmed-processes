package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_REFERENCE;

import java.util.ArrayList;
import java.util.List;
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
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UnsignedIntType;
import org.springframework.beans.factory.InitializingBean;

public class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public StoreResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		boolean needsRecordLinkage = Boolean.TRUE
				.equals((Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE));

		Task task = getCurrentTaskFromExecutionVariables();

		List<QueryResult> extendedResults = new ArrayList<>();
		extendedResults.addAll(results.getResults());
		extendedResults.addAll(getResults(task, needsRecordLinkage));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				QueryResultsValues.create(new QueryResults(extendedResults)));
	}

	private List<QueryResult> getResults(Task task, boolean needsRecordLinkage)
	{
		TaskHelper taskHelper = getTaskHelper();
		Reference requester = task.getRequester();

		if (needsRecordLinkage)
		{
			return taskHelper.getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_DATA_SHARING,
					CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_REFERENCE, EXTENSION_HIGHMED_GROUP_ID)
					.map(input ->
					{
						String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
						String resultSetUrl = ((Reference) input.getValue()).getReference();

						return QueryResult.resultSet(requester.getIdentifier().getValue(), cohortId, resultSetUrl);
					}).collect(Collectors.toList());
		}
		else
		{
			return taskHelper
					.getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_DATA_SHARING,
							CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT, EXTENSION_HIGHMED_GROUP_ID)
					.map(input ->
					{
						String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
						int cohortSize = ((UnsignedIntType) input.getValue()).getValue();

						return QueryResult.count(requester.getIdentifier().getValue(), cohortId, cohortSize);
					}).collect(Collectors.toList());
		}
	}

}
