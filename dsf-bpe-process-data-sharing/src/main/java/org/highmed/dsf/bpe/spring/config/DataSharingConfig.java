package org.highmed.dsf.bpe.spring.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.dsf.bpe.message.SendMedicRequest;
import org.highmed.dsf.bpe.message.SendMultiMedicErrors;
import org.highmed.dsf.bpe.message.SendMultiMedicResults;
import org.highmed.dsf.bpe.message.SendSingleMedicResults;
import org.highmed.dsf.bpe.message.SendTtpRequest;
import org.highmed.dsf.bpe.service.CheckDataSharingResources;
import org.highmed.dsf.bpe.service.CheckMedicMultiMedicResultSets;
import org.highmed.dsf.bpe.service.CheckSingleMedicResultSets;
import org.highmed.dsf.bpe.service.CheckTtpMultiMedicResultSets;
import org.highmed.dsf.bpe.service.DownloadDataSharingResources;
import org.highmed.dsf.bpe.service.DownloadMultiMedicResultSets;
import org.highmed.dsf.bpe.service.DownloadResearchStudyResource;
import org.highmed.dsf.bpe.service.DownloadSingleMedicResultSets;
import org.highmed.dsf.bpe.service.ExecuteQueries;
import org.highmed.dsf.bpe.service.ExtractQueries;
import org.highmed.dsf.bpe.service.FilterQueryResultsByConsent;
import org.highmed.dsf.bpe.service.HandleErrorMultiMedicResults;
import org.highmed.dsf.bpe.service.ModifyQueries;
import org.highmed.dsf.bpe.service.PseudonymizeResultSetsWithRecordLinkage;
import org.highmed.dsf.bpe.service.SelectRequestTargets;
import org.highmed.dsf.bpe.service.SelectResponseTargetMedic;
import org.highmed.dsf.bpe.service.SelectResponseTargetTtp;
import org.highmed.dsf.bpe.service.StoreCorrelationKeys;
import org.highmed.dsf.bpe.service.StoreMultiMedicResultSetsForLeadingMedic;
import org.highmed.dsf.bpe.service.StoreMultiMedicResultSetsForResearcher;
import org.highmed.dsf.bpe.service.StoreSingleMedicResultSetLinks;
import org.highmed.dsf.bpe.service.StoreSingleMedicResultSets;
import org.highmed.dsf.bpe.service.TranslateMultiMedicResultSets;
import org.highmed.dsf.bpe.service.TranslateSingleMedicResultSetsWithRbf;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;
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
	private MasterPatientIndexClientFactory masterPatientIndexClientFactory;

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
	private ObjectMapper objectMapper;

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
	public DownloadMultiMedicResultSets downloadMultiMedicResultSets()
	{
		return new DownloadMultiMedicResultSets(fhirClientProvider, taskHelper, objectMapper);
	}

	@Bean
	public TranslateMultiMedicResultSets translateMultiMedicResultSets()
	{
		return new TranslateMultiMedicResultSets(fhirClientProvider, taskHelper, objectMapper);
	}

	@Bean
	public CheckMedicMultiMedicResultSets checkMedicMultiMedicResultSets()
	{
		return new CheckMedicMultiMedicResultSets(fhirClientProvider, taskHelper);
	}

	@Bean
	public StoreMultiMedicResultSetsForLeadingMedic storeMultiMedicResultSetsForLeadingMedic()
	{
		return new StoreMultiMedicResultSetsForLeadingMedic(fhirClientProvider, taskHelper, objectMapper);
	}

	@Bean
	public StoreMultiMedicResultSetsForResearcher storeMultiMedicResultSetsForResearcher()
	{
		return new StoreMultiMedicResultSetsForResearcher(fhirClientProvider, taskHelper, objectMapper);
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
	public StoreSingleMedicResultSetLinks storeSingleMedicResultSetLinks()
	{
		return new StoreSingleMedicResultSetLinks(fhirClientProvider, taskHelper);
	}

	@Bean
	public DownloadSingleMedicResultSets downloadSingleMedicResultSets()
	{
		return new DownloadSingleMedicResultSets(fhirClientProvider, taskHelper, objectMapper);
	}

	@Bean
	public PseudonymizeResultSetsWithRecordLinkage pseudonymizeResultSetsWithRecordLinkage()
	{
		return new PseudonymizeResultSetsWithRecordLinkage(fhirClientProvider, taskHelper, objectMapper);
	}

	@Bean
	public SelectResponseTargetMedic selectResponseTargetMedic()
	{
		return new SelectResponseTargetMedic(fhirClientProvider, taskHelper);
	}

	@Bean
	public CheckTtpMultiMedicResultSets checkTtpMultiMedicResultSets()
	{
		return new CheckTtpMultiMedicResultSets(fhirClientProvider, taskHelper);
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
	public ExecuteQueries executeQueries()
	{
		return new ExecuteQueries(fhirClientProvider, openEhrClient(), taskHelper, organizationProvider);
	}

	@Bean
	public OpenEhrClient openEhrClient()
	{
		return openEhrClientFactory.createClient(environment::getProperty);
	}

	@Bean
	public FilterQueryResultsByConsent filterQueryResultsByConsent()
	{
		return new FilterQueryResultsByConsent(fhirClientProvider, taskHelper);
	}

	@Bean
	public TranslateSingleMedicResultSetsWithRbf translateSingleMedicResultSetsWithRbf()
	{
		return new TranslateSingleMedicResultSetsWithRbf(fhirClientProvider, taskHelper, organizationProvider,
				ehrIdColumnPath, masterPatientIndexClient(), bouncyCastleProvider());
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
	public StoreSingleMedicResultSets storeSingleMedicResultSets()
	{
		return new StoreSingleMedicResultSets(fhirClientProvider, taskHelper, objectMapper);
	}

	@Bean
	public CheckSingleMedicResultSets checkSingleMedicResultSets()
	{
		return new CheckSingleMedicResultSets(fhirClientProvider, taskHelper);
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
