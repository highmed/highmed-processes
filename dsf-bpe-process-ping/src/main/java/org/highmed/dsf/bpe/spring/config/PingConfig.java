package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.message.SendPing;
import org.highmed.dsf.bpe.message.SendPong;
import org.highmed.dsf.bpe.service.LogPing;
import org.highmed.dsf.bpe.service.LogPong;
import org.highmed.dsf.bpe.service.SelectPingTargets;
import org.highmed.dsf.bpe.service.SelectPongTarget;
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
public class PingConfig
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
	public SendPing sendPing()
	{
		return new SendPing(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SendPong sendPong()
	{
		return new SendPong(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Bean
	public LogPing logPing()
	{
		return new LogPing(clientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public LogPong logPong()
	{
		return new LogPong(clientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public SelectPingTargets selectPingTargets()
	{
		return new SelectPingTargets(clientProvider, taskHelper, readAccessHelper, endpointProvider);
	}

	@Bean
	public SelectPongTarget selectPongTarget()
	{
		return new SelectPongTarget(clientProvider, taskHelper, readAccessHelper, endpointProvider);
	}
}
