package org.highmed.dsf.bpe.spring.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.consent.client.ConsentClient;
import org.highmed.consent.client.ConsentClientFactory;
import org.highmed.dsf.bpe.message.SendMedicRequest;
import org.highmed.dsf.bpe.message.SendMultiMedicErrors;
import org.highmed.dsf.bpe.message.SendMultiMedicResults;
import org.highmed.dsf.bpe.message.SendSingleMedicResults;
import org.highmed.dsf.bpe.message.SendTtpRequest;
import org.highmed.dsf.bpe.service.CalculateMultiMedicResults;
import org.highmed.dsf.bpe.service.CheckFeasibilityResources;
import org.highmed.dsf.bpe.service.CheckMultiMedicResults;
import org.highmed.dsf.bpe.service.CheckQueries;
import org.highmed.dsf.bpe.service.CheckSingleMedicResults;
import org.highmed.dsf.bpe.service.CheckTtpComputedMultiMedicResults;
import org.highmed.dsf.bpe.service.DownloadFeasibilityResources;
import org.highmed.dsf.bpe.service.DownloadResearchStudyResource;
import org.highmed.dsf.bpe.service.DownloadResults;
import org.highmed.dsf.bpe.service.ExecuteQueries;
import org.highmed.dsf.bpe.service.ExecuteRecordLink;
import org.highmed.dsf.bpe.service.FilterResultsByConsent;
import org.highmed.dsf.bpe.service.GenerateBloomFilters;
import org.highmed.dsf.bpe.service.GenerateCountFromIds;
import org.highmed.dsf.bpe.service.HandleErrorMultiMedicResults;
import org.highmed.dsf.bpe.service.ModifyQueries;
import org.highmed.dsf.bpe.service.SelectRequestTargets;
import org.highmed.dsf.bpe.service.SelectResponseTargetMedic;
import org.highmed.dsf.bpe.service.SelectResponseTargetTtp;
import org.highmed.dsf.bpe.service.StoreCorrelationKeys;
import org.highmed.dsf.bpe.service.StoreResults;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicRbfOnly;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicRbfOnlyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class FeasibilityConfig
{
	@Autowired
	private FhirWebserviceClientProvider fhirClientProvider;

	@Autowired
	private ConsentClientFactory consentClientFactory;

	@Autowired
	private MasterPatientIndexClientFactory masterPatientIndexClientFactory;

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
	private ObjectMapper objectMapper;

	@Autowired
	private Environment environment;

	@Value("${org.highmed.dsf.bpe.openehr.subject.external.id.path:/ehr_status/subject/external_ref/id/value}")
	private String ehrIdColumnPath;

	//
	// process requestFeasibility implementations
	//

	@Bean
	public DownloadResearchStudyResource downloadResearchStudyResource()
	{
		return new DownloadResearchStudyResource(fhirClientProvider, taskHelper, readAccessHelper,
				organizationProvider);
	}

	@Bean
	public SelectRequestTargets selectRequestTargets()
	{
		return new SelectRequestTargets(fhirClientProvider, taskHelper, readAccessHelper, endpointProvider,
				bouncyCastleProvider());
	}

	@Bean
	public SendTtpRequest sendTtpRequest()
	{
		return new SendTtpRequest(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SendMedicRequest sendMedicRequest()
	{
		return new SendMedicRequest(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}

	@Bean
	public CheckMultiMedicResults checkMultiMedicResults()
	{
		return new CheckMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public HandleErrorMultiMedicResults handleErrorMultiMedicResults()
	{
		return new HandleErrorMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	//
	// process executeFeasibility implementations
	//

	@Bean
	public DownloadFeasibilityResources downloadFeasibilityResources()
	{
		return new DownloadFeasibilityResources(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider);
	}

	@Bean
	public CheckFeasibilityResources checkFeasibilityResources()
	{
		return new CheckFeasibilityResources(fhirClientProvider, taskHelper, readAccessHelper);
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
	public MasterPatientIndexClient masterPatientIndexClient()
	{
		return masterPatientIndexClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public OpenEhrClient openEhrClient()
	{
		return openEhrClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public GenerateBloomFilters generateBloomFilters()
	{
		return new GenerateBloomFilters(fhirClientProvider, taskHelper, readAccessHelper, ehrIdColumnPath,
				masterPatientIndexClient(), objectMapper, bouncyCastleProvider());
	}

	@Bean
	public BouncyCastleProvider bouncyCastleProvider()
	{
		return new BouncyCastleProvider();
	}

	@Bean
	public CheckSingleMedicResults checkSingleMedicResults()
	{
		return new CheckSingleMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public SelectResponseTargetTtp selectResponseTargetTtp()
	{
		return new SelectResponseTargetTtp(fhirClientProvider, taskHelper, readAccessHelper, endpointProvider);
	}

	@Bean
	public SendSingleMedicResults sendSingleMedicResults()
	{
		return new SendSingleMedicResults(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}

	//
	// process computeFeasibility implementations
	//

	@Bean
	public StoreCorrelationKeys storeCorrelationKeys()
	{
		return new StoreCorrelationKeys(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public StoreResults storeResults()
	{
		return new StoreResults(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider);
	}

	@Bean
	public DownloadResults downloadResults()
	{
		return new DownloadResults(fhirClientProvider, taskHelper, readAccessHelper, objectMapper);
	}

	@Bean
	public ResultSetTranslatorFromMedicRbfOnly resultSetTranslatorFromMedicRbfOnly()
	{
		return new ResultSetTranslatorFromMedicRbfOnlyImpl();
	}

	@Bean
	public ExecuteRecordLink executeRecordLink()
	{
		return new ExecuteRecordLink(fhirClientProvider, taskHelper, readAccessHelper,
				resultSetTranslatorFromMedicRbfOnly());
	}

	@Bean
	public CalculateMultiMedicResults calculateMultiMedicResults()
	{
		return new CalculateMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public CheckTtpComputedMultiMedicResults checkTtpComputedMultiMedicResults()
	{
		return new CheckTtpComputedMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public SelectResponseTargetMedic selectResponseTargetMedic()
	{
		return new SelectResponseTargetMedic(fhirClientProvider, taskHelper, readAccessHelper, endpointProvider);
	}

	@Bean
	public SendMultiMedicResults sendMultiMedicResults()
	{
		return new SendMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}

	@Bean
	public SendMultiMedicErrors sendMultiMedicErrors()
	{
		return new SendMultiMedicErrors(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}
}
