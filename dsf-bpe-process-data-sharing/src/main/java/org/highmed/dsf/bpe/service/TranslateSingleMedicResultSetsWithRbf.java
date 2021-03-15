package org.highmed.dsf.bpe.service;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtp;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToTtpWithRbfImpl;
import org.springframework.beans.factory.InitializingBean;

public class TranslateSingleMedicResultSetsWithRbf extends TranslateSingleMedicResultSets implements InitializingBean
{
	public TranslateSingleMedicResultSetsWithRbf(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, String ehrIdColumnPath,
			MasterPatientIndexClient masterPatientIndexClient, BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper, organizationProvider, ehrIdColumnPath, masterPatientIndexClient,
				bouncyCastleProvider);
	}

	@Override
	protected ResultSetTranslatorToTtp createResultSetTranslator(String organizationIdentifier,
			String researchStudyIdentifier, SecretKey mdatKey, String ehrIdColumnPath,
			MasterPatientIndexClient masterPatientIndexClient, RecordBloomFilterGenerator recordBloomFilterGenerator)
	{
		try
		{
			// TODO: implement and use pseudonym provider to get IDAT key
			SecretKey idatKey = AesGcmUtil.generateAES256Key();
			return new ResultSetTranslatorToTtpWithRbfImpl(organizationIdentifier, idatKey, researchStudyIdentifier,
					mdatKey, ehrIdColumnPath, recordBloomFilterGenerator, masterPatientIndexClient);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException("Could not get IDAT key", e);
		}
	}
}
