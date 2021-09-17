package org.highmed.dsf.bpe.service;

import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.dsf.bpe.crypto.KeyProvider;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtp;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpWithRbfImpl;
import org.springframework.beans.factory.InitializingBean;

public class TranslateSingleMedicResultSetsWithRbf extends TranslateSingleMedicResultSets implements InitializingBean
{
	public TranslateSingleMedicResultSetsWithRbf(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, KeyProvider keyProvider,
			String ehrIdColumnPath, MasterPatientIndexClient masterPatientIndexClient,
			BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, keyProvider, ehrIdColumnPath,
				masterPatientIndexClient, bouncyCastleProvider);
	}

	@Override
	protected ResultSetTranslatorToTtp createResultSetTranslator(String organizationIdentifier, SecretKey idatKey,
			String researchStudyIdentifier, SecretKey mdatKey, String ehrIdColumnPath,
			MasterPatientIndexClient masterPatientIndexClient, RecordBloomFilterGenerator recordBloomFilterGenerator)
	{
		return new ResultSetTranslatorToTtpWithRbfImpl(organizationIdentifier, idatKey, researchStudyIdentifier,
				mdatKey, ehrIdColumnPath, recordBloomFilterGenerator, masterPatientIndexClient);
	}
}
