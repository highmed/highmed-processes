package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.service.DownloadAllowList;
import org.highmed.dsf.bpe.service.UpdateAllowList;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class UpdateAllowListConfig
{
	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private ReadAccessHelper readAccessHelper;

	@Autowired
	private FhirContext fhirContext;

	@Bean
	public UpdateAllowList updateAllowList()
	{
		return new UpdateAllowList(clientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public DownloadAllowList downloadAllowList()
	{
		return new DownloadAllowList(clientProvider, taskHelper, readAccessHelper, fhirContext);
	}
}
