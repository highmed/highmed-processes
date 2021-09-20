package org.highmed.dsf.bpe.spring.config;

import java.nio.file.Paths;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.consent.client.ConsentClient;
import org.highmed.consent.client.ConsentClientFactory;
import org.highmed.dsf.bpe.crypto.KeyConsumer;
import org.highmed.dsf.bpe.crypto.KeyProvider;
import org.highmed.dsf.bpe.crypto.SecretKeyConsumerImpl;
import org.highmed.dsf.bpe.crypto.SecretKeyProviderImpl;
import org.highmed.dsf.bpe.message.SendMedicRequest;
import org.highmed.dsf.bpe.message.SendMultiMedicErrors;
import org.highmed.dsf.bpe.message.SendMultiMedicResults;
import org.highmed.dsf.bpe.message.SendSingleMedicResults;
import org.highmed.dsf.bpe.message.SendTtpRequest;
import org.highmed.dsf.bpe.service.CheckDataSharingResources;
import org.highmed.dsf.bpe.service.CheckMedicMultiMedicResults;
import org.highmed.dsf.bpe.service.CheckMedicSingleMedicResults;
import org.highmed.dsf.bpe.service.CheckTtpMultiMedicResults;
import org.highmed.dsf.bpe.service.CheckTtpSingleMedicResults;
import org.highmed.dsf.bpe.service.DecryptMultiMedicResults;
import org.highmed.dsf.bpe.service.DownloadDataSharingResources;
import org.highmed.dsf.bpe.service.DownloadMultiMedicResults;
import org.highmed.dsf.bpe.service.DownloadResearchStudyResource;
import org.highmed.dsf.bpe.service.DownloadSingleMedicResults;
import org.highmed.dsf.bpe.service.EncryptResults;
import org.highmed.dsf.bpe.service.ExecuteQueries;
import org.highmed.dsf.bpe.service.ExtractQueries;
import org.highmed.dsf.bpe.service.FilterResultsByConsent;
import org.highmed.dsf.bpe.service.GenerateBloomFilters;
import org.highmed.dsf.bpe.service.HandleErrorMultiMedicResults;
import org.highmed.dsf.bpe.service.ModifyQueries;
import org.highmed.dsf.bpe.service.PseudonymizeResultsFirstOrder;
import org.highmed.dsf.bpe.service.PseudonymizeResultsSecondOrderWithRecordLinkage;
import org.highmed.dsf.bpe.service.PseudonymizeResultsSecondOrderWithoutRecordLinkage;
import org.highmed.dsf.bpe.service.SelectRequestTargets;
import org.highmed.dsf.bpe.service.SelectResponseTargetMedic;
import org.highmed.dsf.bpe.service.SelectResponseTargetTtp;
import org.highmed.dsf.bpe.service.StoreCorrelationKeys;
import org.highmed.dsf.bpe.service.StoreMultiMedicResultsForLeadingMedic;
import org.highmed.dsf.bpe.service.StoreMultiMedicResultsForResearcher;
import org.highmed.dsf.bpe.service.StoreSingleMedicResultLinks;
import org.highmed.dsf.bpe.service.StoreSingleMedicResults;
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
import org.highmed.pseudonymization.client.PseudonymizationClient;
import org.highmed.pseudonymization.client.PseudonymizationClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class DataSharingConfig
{
	@Autowired
	private FhirWebserviceClientProvider fhirClientProvider;

	@Autowired
	private ConsentClientFactory consentClientFactory;

	@Autowired
	private PseudonymizationClientFactory pseudonymizationClientFactory;

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

	@Value("${org.highmed.dsf.bpe.psn.organization.keystore:psn/organization-keystore.p12}")
	private String organizationKeystoreFile;

	@Value("${org.highmed.dsf.bpe.psn.organization.keystore.password:password}")
	private String organizationKeystorePassword;

	@Value("${org.highmed.dsf.bpe.psn.research.study.keystore:psn/research-study-keystore.p12}")
	private String researchStudyKeystoreFile;

	@Value("${org.highmed.dsf.bpe.psn.research.study.keystore.password:password}")
	private String getResearchStudyKeystorePassword;

	@Value("${org.highmed.dsf.bpe.openehr.subject_external_id.path:/ehr_status/subject/external_ref/id/value}")
	private String ehrIdColumnPath;

	//
	// process requestDataSharing implementations
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
	public DownloadMultiMedicResults downloadMultiMedicResults()
	{
		return new DownloadMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper, objectMapper);
	}

	@Bean
	public DecryptMultiMedicResults decryptMultiMedicResults()
	{
		return new DecryptMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper, objectMapper);
	}

	@Bean
	public CheckMedicMultiMedicResults checkMedicMultiMedicResults()
	{
		return new CheckMedicMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public StoreMultiMedicResultsForLeadingMedic storeMultiMedicResultsForLeadingMedic()
	{
		return new StoreMultiMedicResultsForLeadingMedic(fhirClientProvider, taskHelper, readAccessHelper,
				objectMapper);
	}

	@Bean
	public StoreMultiMedicResultsForResearcher storeMultiMedicResultsForResearcher()
	{
		return new StoreMultiMedicResultsForResearcher(fhirClientProvider, taskHelper, readAccessHelper, objectMapper);
	}

	@Bean
	public HandleErrorMultiMedicResults handleErrorMultiMedicResults()
	{
		return new HandleErrorMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	//
	// process computeDataSharing implementations
	//

	@Bean
	public StoreCorrelationKeys storeCorrelationKeys()
	{
		return new StoreCorrelationKeys(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public StoreSingleMedicResultLinks storeSingleMedicResultLinks()
	{
		return new StoreSingleMedicResultLinks(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public DownloadSingleMedicResults downloadSingleMedicResults()
	{
		return new DownloadSingleMedicResults(fhirClientProvider, taskHelper, readAccessHelper, objectMapper);
	}

	@Bean
	public CheckTtpSingleMedicResults checkTtpSingleMedicResults()
	{
		return new CheckTtpSingleMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public KeyConsumer keyConsumer()
	{
		return new SecretKeyConsumerImpl(Paths.get(researchStudyKeystoreFile),
				getResearchStudyKeystorePassword.toCharArray());
	}

	@Bean
	public PseudonymizeResultsSecondOrderWithRecordLinkage pseudonymizeQueryResultsSecondOrderWithRecordLinkage()
	{
		return new PseudonymizeResultsSecondOrderWithRecordLinkage(fhirClientProvider, taskHelper, readAccessHelper,
				keyConsumer(), objectMapper);
	}

	@Bean
	public PseudonymizeResultsSecondOrderWithoutRecordLinkage pseudonymizeQueryResultsSecondOrderWithoutRecordLinkage()
	{
		return new PseudonymizeResultsSecondOrderWithoutRecordLinkage(fhirClientProvider, taskHelper, readAccessHelper,
				keyConsumer(), objectMapper);
	}

	@Bean
	public SelectResponseTargetMedic selectResponseTargetMedic()
	{
		return new SelectResponseTargetMedic(fhirClientProvider, taskHelper, readAccessHelper, endpointProvider);
	}

	@Bean
	public CheckTtpMultiMedicResults checkTtpMultiMedicResults()
	{
		return new CheckTtpMultiMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
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

	//
	// process executeDataSharing implementations
	//

	@Bean
	public DownloadDataSharingResources downloadDataSharingResources()
	{
		return new DownloadDataSharingResources(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider);
	}

	@Bean
	public CheckDataSharingResources checkDataSharingResources()
	{
		return new CheckDataSharingResources(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public ExtractQueries extractQueries()
	{
		return new ExtractQueries(fhirClientProvider, taskHelper, readAccessHelper, groupHelper);
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
	public OpenEhrClient openEhrClient()
	{
		return openEhrClientFactory.createClient(environment::getProperty);
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
	public MasterPatientIndexClient masterPatientIndexClient()
	{
		return masterPatientIndexClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public BouncyCastleProvider bouncyCastleProvider()
	{
		return new BouncyCastleProvider();
	}

	@Bean
	public GenerateBloomFilters generateBloomFilters()
	{
		return new GenerateBloomFilters(fhirClientProvider, taskHelper, readAccessHelper, masterPatientIndexClient(),
				ehrIdColumnPath, bouncyCastleProvider());
	}

	@Bean
	public PseudonymizationClient pseudonymizationClient()
	{
		return pseudonymizationClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public PseudonymizeResultsFirstOrder pseudonymizeResultsFirstOrder()
	{
		return new PseudonymizeResultsFirstOrder(fhirClientProvider, taskHelper, readAccessHelper,
				pseudonymizationClient());
	}

	@Bean
	public KeyProvider keyProvider()
	{
		return new SecretKeyProviderImpl(organizationProvider, Paths.get(organizationKeystoreFile),
				organizationKeystorePassword.toCharArray());
	}

	@Bean
	public EncryptResults encryptResults()
	{
		return new EncryptResults(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider, keyProvider(),
				ehrIdColumnPath);
	}

	@Bean
	public SelectResponseTargetTtp selectResponseTargetTtp()
	{
		return new SelectResponseTargetTtp(fhirClientProvider, taskHelper, readAccessHelper, endpointProvider);
	}

	@Bean
	public CheckMedicSingleMedicResults checkMedicSingleMedicResults()
	{
		return new CheckMedicSingleMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public StoreSingleMedicResults storeSingleMedicResults()
	{
		return new StoreSingleMedicResults(fhirClientProvider, taskHelper, readAccessHelper, objectMapper);
	}

	@Bean
	public SendSingleMedicResults sendSingleMedicResults()
	{
		return new SendSingleMedicResults(fhirClientProvider, taskHelper, readAccessHelper, organizationProvider,
				fhirContext);
	}
}
