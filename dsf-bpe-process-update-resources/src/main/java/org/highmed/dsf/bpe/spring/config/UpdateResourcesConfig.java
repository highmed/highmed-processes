package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.message.SendRequest;
import org.highmed.dsf.bpe.service.CheckRequest;
import org.highmed.dsf.bpe.service.DownloadBundle;
import org.highmed.dsf.bpe.service.SelectResourceAndTargets;
import org.highmed.dsf.bpe.service.UpdateResources;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class UpdateResourcesConfig
{
	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private ReadAccessHelper readAccessHelper;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private EndpointProvider endpointProvider;

	@Autowired
	private FhirContext fhirContext;

	@Bean
	public SendRequest sendRequest()
	{
		return new SendRequest(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SelectResourceAndTargets selectUpdateResourcesTargets()
	{
		return new SelectResourceAndTargets(clientProvider, taskHelper, readAccessHelper, organizationProvider,
				endpointProvider);
	}

	@Bean
	public DownloadBundle downloadBundle()
	{
		return new DownloadBundle(clientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public CheckRequest checkRequest()
	{
		return new CheckRequest(clientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public UpdateResources updateResources()
	{
		return new UpdateResources(clientProvider, taskHelper, readAccessHelper, fhirContext);
	}
}
