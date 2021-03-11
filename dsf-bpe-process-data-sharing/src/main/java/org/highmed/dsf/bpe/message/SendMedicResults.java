package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public abstract class SendMedicResults extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(SendMedicResults.class);

	public SendMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		QueryResults results = (QueryResults) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);
		return results.getResults().stream().map(this::toInput);
	}

	/**
	 * @return the code system value that classifies the result set reference Task input
	 */
	protected abstract String getResultSetReferenceCodeSystemValue();

	private Task.ParameterComponent toInput(QueryResult result)
	{
		if (result.isIdResultSetUrlResult())
		{
			String resultSetReferenceCodeSystemValue = getResultSetReferenceCodeSystemValue();
			ParameterComponent input = getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
					resultSetReferenceCodeSystemValue, new Reference(result.getResultSetUrl()));
			input.addExtension(createCohortIdExtension(result.getCohortId()));
			return input;
		}
		else
		{
			logger.warn("Unexpected result (not a cohort-size or ResultSet URL result) for cohort with ID "
					+ result.getCohortId());
			throw new RuntimeException(
					"Unexpected result (not a cohort-size or ResultSet URL result) for cohort with ID "
							+ result.getCohortId());
		}
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(EXTENSION_HIGHMED_GROUP_ID, new Reference(cohortId));
	}
}
