package org.highmed.dsf.bpe.message;

import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;

import ca.uhn.fhir.context.FhirContext;

public class SendMultiMedicErrors extends SendErrors
{
	public SendMultiMedicErrors(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected String getErrorMessage(String taskUrl)
	{
		return "An error occurred while calculating the multi medic data sharing result for "
				+ "all defined cohorts, see task with url='" + taskUrl + "'";
	}
}
