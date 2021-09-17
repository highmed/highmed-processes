package org.highmed.dsf.bpe.service;

import javax.crypto.SecretKey;

import org.highmed.dsf.bpe.crypto.KeyProvider;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtp;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpNoRbfImpl;
import org.springframework.beans.factory.InitializingBean;

public class TranslateSingleMedicResultSetsWithoutRbf extends TranslateSingleMedicResultSets implements InitializingBean
{
	public TranslateSingleMedicResultSetsWithoutRbf(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, KeyProvider keyProvider,
			String ehrIdColumnPath)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, keyProvider, ehrIdColumnPath, null,
				null);
	}

	@Override
	protected ResultSetTranslatorToTtp createResultSetTranslator(String organizationIdentifier, SecretKey idatKey,
			String researchStudyIdentifier, SecretKey mdatKey, String ehrIdColumnPath,
			MasterPatientIndexClient masterPatientIndexClient, RecordBloomFilterGenerator recordBloomFilterGenerator)
	{
		return new ResultSetTranslatorToTtpNoRbfImpl(organizationIdentifier, idatKey, researchStudyIdentifier, mdatKey,
				ehrIdColumnPath);
	}
}
