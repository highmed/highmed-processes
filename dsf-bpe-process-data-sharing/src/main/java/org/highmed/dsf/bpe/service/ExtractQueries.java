package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_COHORTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERIES;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourcesList;
import org.hl7.fhir.r4.model.Group;
import org.springframework.beans.factory.InitializingBean;

public class ExtractQueries extends AbstractServiceDelegate implements InitializingBean
{
	private final GroupHelper groupHelper;

	public ExtractQueries(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper, GroupHelper groupHelper)
	{
		super(clientProvider, taskHelper);
		this.groupHelper = groupHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(groupHelper, "groupHelper");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		List<Group> cohorts = ((FhirResourcesList) execution.getVariable(BPMN_EXECUTION_VARIABLE_COHORTS))
				.getResourcesAndCast();

		// <groupId, query>
		Map<String, String> queries = new HashMap<>();

		cohorts.forEach(group ->
		{
			String aqlQuery = groupHelper.extractAqlQuery(group);
			String groupId = group.getId();

			queries.put(groupId, aqlQuery);
		});

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERIES, queries);
	}
}
