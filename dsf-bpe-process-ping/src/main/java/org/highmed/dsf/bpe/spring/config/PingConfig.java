package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.logging.ErrorLogger;
import org.highmed.dsf.bpe.message.SendPing;
import org.highmed.dsf.bpe.message.SendPong;
import org.highmed.dsf.bpe.message.SendStartPing;
import org.highmed.dsf.bpe.service.LogNoResponse;
import org.highmed.dsf.bpe.service.LogPing;
import org.highmed.dsf.bpe.service.LogPong;
import org.highmed.dsf.bpe.service.SelectPingTargets;
import org.highmed.dsf.bpe.service.SelectPongTarget;
import org.highmed.dsf.bpe.service.StartTimer;
import org.highmed.dsf.bpe.service.StopTimer;
import org.highmed.dsf.bpe.util.PingStatusGenerator;
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
	public StartTimer startTimer()
	{
		return new StartTimer(clientProvider, taskHelper, readAccessHelper, organizationProvider, endpointProvider);
	}

	@Bean
	public SendStartPing sendStartPing()
	{
		return new SendStartPing(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Bean
	public StopTimer stopTimer()
	{
		return new StopTimer(clientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public PingStatusGenerator responseGenerator()
	{
		return new PingStatusGenerator();
	}

	@Bean
	public ErrorLogger errorLogger()
	{
		return new ErrorLogger();
	}

	@Bean
	public SendPing sendPing()
	{
		return new SendPing(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext,
				responseGenerator(), errorLogger());
	}

	@Bean
	public SendPong sendPong()
	{
		return new SendPong(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext,
				responseGenerator(), errorLogger());
	}

	@Bean
	public LogPing logPing()
	{
		return new LogPing(clientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public LogPong logPong()
	{
		return new LogPong(clientProvider, taskHelper, readAccessHelper, responseGenerator());
	}

	@Bean
	public LogNoResponse logNoResponse()
	{
		return new LogNoResponse(clientProvider, taskHelper, readAccessHelper, responseGenerator(), errorLogger());
	}

	@Bean
	public SelectPingTargets selectPingTargets()
	{
		return new SelectPingTargets(clientProvider, taskHelper, readAccessHelper, organizationProvider);
	}

	@Bean
	public SelectPongTarget selectPongTarget()
	{
		return new SelectPongTarget(clientProvider, taskHelper, readAccessHelper, endpointProvider);
	}
}
