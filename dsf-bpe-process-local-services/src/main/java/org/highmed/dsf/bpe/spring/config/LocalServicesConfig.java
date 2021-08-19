package org.highmed.dsf.bpe.spring.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.dsf.bpe.service.CheckQueries;
import org.highmed.dsf.bpe.service.CheckSingleMedicResults;
import org.highmed.dsf.bpe.service.ExecuteQueries;
import org.highmed.dsf.bpe.service.ExtractInputValues;
import org.highmed.dsf.bpe.service.FilterQueryResultsByConsent;
import org.highmed.dsf.bpe.service.GenerateBloomFilters;
import org.highmed.dsf.bpe.service.GenerateCountFromIds;
import org.highmed.dsf.bpe.service.ModifyQueries;
import org.highmed.dsf.bpe.service.StoreResult;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
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

@Configuration
public class LocalServicesConfig
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
	private ReadAccessHelper readAccessHelper;

	@Autowired
	private GroupHelper groupHelper;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Environment environment;

	@Value("${org.highmed.dsf.bpe.openehr.subject_external_id.path:/ehr_status/subject/external_ref/id/value}")
	private String ehrIdColumnPath;

	@Bean
	public ExtractInputValues extractInputValues()
	{
		return new ExtractInputValues(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public StoreResult storeResult()
	{
		return new StoreResult(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public CheckSingleMedicResults checkSingleMedicResults()
	{
		return new CheckSingleMedicResults(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public ModifyQueries modifyQueries()
	{
		return new ModifyQueries(fhirClientProvider, taskHelper, readAccessHelper, ehrIdColumnPath);
	}

	@Bean
	public FilterQueryResultsByConsent filterQueryResultsByConsent()
	{
		return new FilterQueryResultsByConsent(fhirClientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public GenerateBloomFilters generateBloomFilters()
	{
		return new GenerateBloomFilters(fhirClientProvider, taskHelper, readAccessHelper, ehrIdColumnPath,
				masterPatientIndexClient(), objectMapper, bouncyCastleProvider());
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
	public GenerateCountFromIds generateCountFromIds()
	{
		return new GenerateCountFromIds(fhirClientProvider, taskHelper, readAccessHelper);
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
	public CheckQueries checkQueries()
	{
		return new CheckQueries(fhirClientProvider, taskHelper, readAccessHelper, groupHelper);
	}
}
