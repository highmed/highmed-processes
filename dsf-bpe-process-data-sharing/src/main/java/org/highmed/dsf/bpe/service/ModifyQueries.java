package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERIES;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ModifyQueries extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ModifyQueries.class);

	private final String ehrIdColumnPath;

	public ModifyQueries(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper, String ehrIdColumnPath)
	{
		super(clientProvider, taskHelper);
		this.ehrIdColumnPath = ehrIdColumnPath;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		Boolean needsConsentCheck = (Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK);
		Boolean needsRecordLinkage = (Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);

		if (Boolean.TRUE.equals(needsConsentCheck) || Boolean.TRUE.equals(needsRecordLinkage))
		{
			@SuppressWarnings("unchecked")
			// <groupId, query>
			Map<String, String> queries = (Map<String, String>) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERIES);
			Map<String, String> modifiedQueries = modifyQueries(queries);
			execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERIES, modifiedQueries);
		}
	}

	private Map<String, String> modifyQueries(Map<String, String> queries)
	{
		return queries.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> modify(entry.getValue())));
	}

	private String modify(String query)
	{
		// TODO: implement correct check for id query
		if (!query.startsWith("SELECT e" + ehrIdColumnPath + " as EHRID"))
		{
			query = query.replace("SELECT", "SELECT e" + ehrIdColumnPath + " as EHRID,");
			logger.warn("Query does not start with '{}', adapting SELECT statement",
					"SELECT e" + ehrIdColumnPath + " as EHRID");
		}

		return query;
	}
}
