package org.highmed.dsf.bpe.message;

import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;

public class SendPong extends AbstractTaskMessageSend
{
	public SendPong(OrganizationProvider organizationProvider, FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(organizationProvider, clientProvider, taskHelper);
	}
}
