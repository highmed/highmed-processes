package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_COHORTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERIES;
import static org.highmed.dsf.bpe.ConstantsFeasibilityMpc.FEASIBILITY_MPC_QUERY_PREFIX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourcesList;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class CheckQueries extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(CheckQueries.class);

	private final GroupHelper groupHelper;

	public CheckQueries(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, GroupHelper groupHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);

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

		Map<String, String> queries = new HashMap<>();

		Task leadingTask = getLeadingTaskFromExecutionVariables(execution);
		cohorts.forEach(group ->
		{
			String aqlQuery = groupHelper.extractAqlQuery(group);

			String groupId = group.getId();
			if (!aqlQuery.startsWith(FEASIBILITY_MPC_QUERY_PREFIX))
			{
				String errorMessage = "Initial single medic FeasibilityMpc query check failed, wrong format for query of group with id '"
						+ groupId + "', expected query to start with '" + FEASIBILITY_MPC_QUERY_PREFIX + "' but got '"
						+ aqlQuery + "'";

				logger.info(errorMessage);
				leadingTask.getOutput().add(getTaskHelper().createOutput(CODESYSTEM_HIGHMED_BPMN,
						CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, errorMessage));
			}
			else
			{
				queries.put(groupId, aqlQuery);
			}
		});

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERIES, queries);
	}
}
