package org.highmed.dsf.bpe.spring.config;

import org.highmed.consent.client.ConsentClient;
import org.highmed.consent.client.ConsentClientFactory;
import org.highmed.dsf.bpe.message.SendMedicRequestMultiShare;
import org.highmed.dsf.bpe.message.SendMedicRequestSingleShare;
import org.highmed.dsf.bpe.message.SendMultiMedicResultShare;
import org.highmed.dsf.bpe.message.SendSingleMedicResultShare;
import org.highmed.dsf.bpe.service.CalculateMultiMedicResult;
import org.highmed.dsf.bpe.service.CalculateMultiMedicResultShares;
import org.highmed.dsf.bpe.service.CalculateSingleMedicResultShares;
import org.highmed.dsf.bpe.service.CheckFeasibilityMpcResources;
import org.highmed.dsf.bpe.service.CheckMultiMedicResults;
import org.highmed.dsf.bpe.service.CheckQueries;
import org.highmed.dsf.bpe.service.CheckSingleMedicResultShares;
import org.highmed.dsf.bpe.service.DownloadFeasibilityMpcResources;
import org.highmed.dsf.bpe.service.DownloadResearchStudyResource;
import org.highmed.dsf.bpe.service.ExecuteQueries;
import org.highmed.dsf.bpe.service.FilterResultsByConsent;
import org.highmed.dsf.bpe.service.GenerateCountFromIds;
import org.highmed.dsf.bpe.service.ModifyQueries;
import org.highmed.dsf.bpe.service.SelectMultiMedicResultShareTarget;
import org.highmed.dsf.bpe.service.SelectMultiMedicTargets;
import org.highmed.dsf.bpe.service.SelectSingleMedicResultShareTargets;
import org.highmed.dsf.bpe.service.StoreResultsMultiMedicShare;
import org.highmed.dsf.bpe.service.StoreResultsSingleMedicShare;
import org.highmed.dsf.bpe.service.StoreSingleMedicResultShareCorrelationKeys;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class FeasibilityMpcConfig
{
	@Autowired
	private FhirWebserviceClientProvider fhirClientProvider;

	@Autowired
	private ConsentClientFactory consentClientFactory;

	@Autowired
	private OpenEhrClientFactory openEhrClientFactory;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private EndpointProvider endpointProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private ReadAccessHelper readAccessHelper;

	@Autowired
	private GroupHelper groupHelper;

	@Autowired
	private FhirContext fhirContext;

	@Autowired
	private Environment environment;

	@Value("${org.highmed.dsf.bpe.openehr.subject.external.id.path:/ehr_status/subject/external_ref/id/value}")
	private String ehrIdColumnPath;

	//
	// process requestFeasibilityMpc implementations
	//

	@Bean
	public DownloadResearchStudyResource downloadResearchStudyResource()
	{
		return new DownloadResearchStudyResource(fhirClientProvider, taskHelper, readAccessHelper,
				organizationProvider);
	}

	@Bean
	public SelectMultiMedicTargets selectMultiMedicTargets()
	{
		return new SelectMultiMedicTargets(fhirClientProvider, taskHelper, readAccessHelper, endpointProvider);
	}

	@Bean
	public SendMedicRequestMultiShare sendMedicRequestMultiShare()
	{
		return new SendMedicRequestMultiShare(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}

	@Bean
	public SendMedicRequestSingleShare sendMedicRequestSingleShare()
	{
		return new SendMedicRequestSingleShare(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}

	@Bean
	public StoreResultsMultiMedicShare storeResultsMultiMedicShare()
	{
		return new StoreResultsMultiMedicShare(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public CalculateMultiMedicResultShares calculateMulitMedicResultShares()
	{
		return new CalculateMultiMedicResultShares(fhirClientProvider, taskHelper, readAccessHelper,
				organizationProvider);
	}

	@Bean
	public CheckMultiMedicResults checkMultiMedicResults()
	{
		return new CheckMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	//
	// process executeFeasibilityMpcSingleShare implementations
	//

	//
	// process executeFeasibilityMpcMultiShare implementations
	//

	@Bean
	public DownloadFeasibilityMpcResources downloadFeasibilityMpcResources()
	{
		return new DownloadFeasibilityMpcResources(fhirClientProvider, taskHelper, readAccessHelper,
				organizationProvider);
	}

	@Bean
	public CheckFeasibilityMpcResources checkFeasibilityMpcResources()
	{
		return new CheckFeasibilityMpcResources(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public StoreSingleMedicResultShareCorrelationKeys storeSingleMedicResultShareCorrelationKeys()
	{
		return new StoreSingleMedicResultShareCorrelationKeys(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public CheckQueries checkQueries()
	{
		return new CheckQueries(fhirClientProvider, taskHelper, readAccessHelper, groupHelper);
	}

	@Bean
	public ModifyQueries modifyQueries()
	{
		return new ModifyQueries(fhirClientProvider, taskHelper, readAccessHelper, ehrIdColumnPath);
	}

	@Bean
	public OpenEhrClient openEhrClient()
	{
		return openEhrClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public ExecuteQueries executeQueries()
	{
		return new ExecuteQueries(fhirClientProvider, openEhrClient(), taskHelper, readAccessHelper,
				organizationProvider);
	}

	@Bean
	public ConsentClient consentClient()
	{
		return consentClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public FilterResultsByConsent filterResultsByConsent()
	{
		return new FilterResultsByConsent(fhirClientProvider, taskHelper, readAccessHelper, consentClient());
	}

	@Bean
	public GenerateCountFromIds generateCountFromIds()
	{
		return new GenerateCountFromIds(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public CalculateSingleMedicResultShares calculateSingleMedicResultShares()
	{
		return new CalculateSingleMedicResultShares(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public CheckSingleMedicResultShares checkSingleMedicResultShares()
	{
		return new CheckSingleMedicResultShares(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public SelectSingleMedicResultShareTargets selectSingleMedicResultShareTargets()
	{
		return new SelectSingleMedicResultShareTargets(fhirClientProvider, taskHelper, readAccessHelper,
				endpointProvider);
	}

	@Bean
	public SendSingleMedicResultShare sendSingleMedicResultShare()
	{
		return new SendSingleMedicResultShare(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}

	@Bean
	public StoreResultsSingleMedicShare storeResultsSingleMedicShare()
	{
		return new StoreResultsSingleMedicShare(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public CalculateMultiMedicResult calculateMultiMedicResult()
	{
		return new CalculateMultiMedicResult(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public SelectMultiMedicResultShareTarget selectMultiMedicResultShareTarget()
	{
		return new SelectMultiMedicResultShareTarget(fhirClientProvider, taskHelper, readAccessHelper,
				endpointProvider);
	}

	@Bean
	public SendMultiMedicResultShare sendMultiMedicResultShare()
	{
		return new SendMultiMedicResultShare(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}
}
