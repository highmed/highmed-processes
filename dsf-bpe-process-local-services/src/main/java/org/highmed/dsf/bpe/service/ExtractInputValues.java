package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_QUERY_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL;
import static org.highmed.dsf.bpe.ConstantsBase.CODE_TYPE_AQL_QUERY;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_QUERY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_COHORTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.BloomFilterConfig;
import org.highmed.dsf.bpe.variable.BloomFilterConfigValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourcesListValues;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class ExtractInputValues extends AbstractServiceDelegate implements InitializingBean
{
	public ExtractInputValues(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables();

		Stream<String> queries = getQueries(task);
		List<Group> cohortDefinitions = getCohortDefinitions(queries);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_COHORTS, FhirResourcesListValues.create(cohortDefinitions));

		boolean needsConsentCheck = getNeedsConsentCheck(task);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = getNeedsRecordLinkageCheck(task);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);

		if (needsRecordLinkage)
		{
			BloomFilterConfig bloomFilterConfig = getBloomFilterConfig(task);
			execution.setVariable(BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG,
					BloomFilterConfigValues.create(bloomFilterConfig));
		}
	}

	private Stream<String> getQueries(Task task)
	{
		return getTaskHelper().getInputParameterStringValues(task, CODESYSTEM_HIGHMED_QUERY_TYPE,
				CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL);
	}

	private List<Group> getCohortDefinitions(Stream<String> queries)
	{
		return queries.map(q ->
		{
			Group group = new Group();
			group.setType(GroupType.PERSON);
			group.setActual(false);
			group.addExtension().setUrl(EXTENSION_HIGHMED_QUERY)
					.setValue(new Expression().setLanguageElement(CODE_TYPE_AQL_QUERY).setExpression(q));
			getReadAccessHelper().addLocal(group);

			return getFhirWebserviceClientProvider().getLocalWebserviceClient().create(group);
		}).collect(Collectors.toList());
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK)
				.orElseThrow(() -> new IllegalArgumentException("NeedsConsentCheck boolean is not set in task with id='"
						+ task.getId() + "', this error should " + "have been caught by resource validation"));
	}

	private boolean getNeedsRecordLinkageCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE)
				.orElseThrow(
						() -> new IllegalArgumentException("NeedsRecordLinkage boolean is not set in task with id='"
								+ task.getId() + "', this error should " + "have been caught by resource validation"));
	}

	private BloomFilterConfig getBloomFilterConfig(Task task)
	{
		return BloomFilterConfig.fromBytes(getTaskHelper()
				.getFirstInputParameterByteValue(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_BLOOM_FILTER_CONFIG)
				.orElseThrow(() -> new IllegalArgumentException("BloomFilterConfig byte[] is not set in task with id='"
						+ task.getId() + "', this error should " + "have been caught by resource validation")));
	}
}
