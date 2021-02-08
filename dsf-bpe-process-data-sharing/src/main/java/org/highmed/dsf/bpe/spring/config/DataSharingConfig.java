package org.highmed.dsf.bpe.spring.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.dsf.bpe.message.SendMedicRequest;
import org.highmed.dsf.bpe.message.SendMultiMedicErrors;
import org.highmed.dsf.bpe.message.SendMultiMedicResults;
import org.highmed.dsf.bpe.message.SendSingleMedicResults;
import org.highmed.dsf.bpe.message.SendTtpRequest;
import org.highmed.dsf.bpe.service.CheckDataSharingResources;
import org.highmed.dsf.bpe.service.CheckMultiMedicResults;
import org.highmed.dsf.bpe.service.CheckSingleMedicResults;
import org.highmed.dsf.bpe.service.CheckTtpComputedMultiMedicResults;
import org.highmed.dsf.bpe.service.CreatePseudonym;
import org.highmed.dsf.bpe.service.DownloadDataSharingResources;
import org.highmed.dsf.bpe.service.DownloadResearchStudyResource;
import org.highmed.dsf.bpe.service.DownloadResultSets;
import org.highmed.dsf.bpe.service.EncryptMdat;
import org.highmed.dsf.bpe.service.ExecuteQueries;
import org.highmed.dsf.bpe.service.ExecuteRecordLink;
import org.highmed.dsf.bpe.service.ExtractQueries;
import org.highmed.dsf.bpe.service.FilterQueryResultsByConsent;
import org.highmed.dsf.bpe.service.GenerateBloomFilters;
import org.highmed.dsf.bpe.service.HandleErrorMultiMedicResults;
import org.highmed.dsf.bpe.service.ModifyQueries;
import org.highmed.dsf.bpe.service.ProvideLocalPseudonyms;
import org.highmed.dsf.bpe.service.SelectRequestTargets;
import org.highmed.dsf.bpe.service.SelectResponseTargetMedic;
import org.highmed.dsf.bpe.service.SelectResponseTargetTtp;
import org.highmed.dsf.bpe.service.StoreCorrelationKeys;
import org.highmed.dsf.bpe.service.StoreResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
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
public class DataSharingConfig
{

	@Autowired
	private FhirWebserviceClientProvider fhirClientProvider;

	@Autowired
	private OpenEhrClientFactory openEhrClientFactory;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private GroupHelper groupHelper;

	@Autowired
	private FhirContext fhirContext;

	@Value("${org.highmed.dsf.bpe.openehr.subject_external_id.path:/ehr_status/subject/external_ref/id/value}")
	private String ehrIdColumnPath;

	@Autowired
	private Environment environment;

	@Bean
	public DownloadResearchStudyResource downloadResearchStudyResourceDS()
	{
		return new DownloadResearchStudyResource(fhirClientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public SelectRequestTargets selectRequestTargets()
	{
		return new SelectRequestTargets(fhirClientProvider, taskHelper, organizationProvider, bouncyCastleProvider());
	}

	@Bean
	public SendTtpRequest sendTtpRequest()
	{
		return new SendTtpRequest(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SendMedicRequest sendMedicRequest()
	{
		return new SendMedicRequest(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public CheckMultiMedicResults checkMultiMedicResults()
	{
		return new CheckMultiMedicResults(fhirClientProvider, taskHelper);
	}

	//
	// process computeDataSharing implementations
	//

	@Bean
	public StoreCorrelationKeys storeCorrelationKeys()
	{
		return new StoreCorrelationKeys(fhirClientProvider, taskHelper);
	}

	@Bean
	public StoreResults storeResults()
	{
		return new StoreResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public DownloadResultSets downloadResultSets()
	{
		return new DownloadResultSets(fhirClientProvider, taskHelper);
	}

	@Bean
	public ExecuteRecordLink executeRecordLink()
	{
		return new ExecuteRecordLink(fhirClientProvider, taskHelper);
	}

	@Bean
	public CreatePseudonym createPseudonym()
	{
		return new CreatePseudonym(fhirClientProvider, taskHelper);
	}

	@Bean
	public SelectResponseTargetMedic selectResponseTargetMedic()
	{
		return new SelectResponseTargetMedic(fhirClientProvider, taskHelper);
	}

	@Bean
	public CheckTtpComputedMultiMedicResults checkTtpComputedMultiMedicResults()
	{
		return new CheckTtpComputedMultiMedicResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public SendMultiMedicResults sendMultiMedicResults()
	{
		return new SendMultiMedicResults(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SendMultiMedicErrors sendMultiMedicErrors()
	{
		return new SendMultiMedicErrors(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public HandleErrorMultiMedicResults handleErrorMultiMedicResults()
	{
		return new HandleErrorMultiMedicResults(fhirClientProvider, taskHelper);
	}

	//
	// process executeDataSharing implementations
	//

	@Bean
	public DownloadDataSharingResources downloadDataSharingResources()
	{
		return new DownloadDataSharingResources(fhirClientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public CheckDataSharingResources checkDataSharingResources()
	{
		return new CheckDataSharingResources(fhirClientProvider, taskHelper);
	}

	@Bean
	public ExtractQueries extractQueries()
	{
		return new ExtractQueries(fhirClientProvider, taskHelper, groupHelper);
	}

	@Bean
	public ModifyQueries modifyQueries()
	{
		return new ModifyQueries(fhirClientProvider, taskHelper, ehrIdColumnPath);
	}

	@Bean
	public OpenEhrClient openEhrClient()
	{
		return openEhrClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public ExecuteQueries executeQueries()
	{
		return new ExecuteQueries(fhirClientProvider, openEhrClient(), taskHelper, organizationProvider);
	}

	@Bean
	public ProvideLocalPseudonyms provideLocalPseudonyms()
	{
		return new ProvideLocalPseudonyms(fhirClientProvider, taskHelper);
	}

	@Bean
	public EncryptMdat encryptMdat()
	{
		return new EncryptMdat(fhirClientProvider, taskHelper);
	}

	@Bean
	public FilterQueryResultsByConsent filterQueryResultsByConsent()
	{
		return new FilterQueryResultsByConsent(fhirClientProvider, taskHelper);
	}

	@Bean
	public GenerateBloomFilters generateBloomFilters()
	{
		return new GenerateBloomFilters(fhirClientProvider, taskHelper);
	}

	@Bean
	public BouncyCastleProvider bouncyCastleProvider()
	{
		return new BouncyCastleProvider();
	}

	@Bean
	public CheckSingleMedicResults checkSingleMedicResults()
	{
		return new CheckSingleMedicResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public SelectResponseTargetTtp selectResponseTargetTtp()
	{
		return new SelectResponseTargetTtp(fhirClientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public SendSingleMedicResults sendSingleMedicResults()
	{
		return new SendSingleMedicResults(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}
}
