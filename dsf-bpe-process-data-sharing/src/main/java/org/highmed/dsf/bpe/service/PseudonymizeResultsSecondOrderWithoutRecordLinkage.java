package org.highmed.dsf.bpe.service;

import org.highmed.dsf.bpe.crypto.KeyConsumer;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcher;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicNoRbf;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymizeResultsSecondOrderWithoutRecordLinkage extends AbstractPseudonymizeResultsSecondOrder
		implements InitializingBean
{
	public PseudonymizeResultsSecondOrderWithoutRecordLinkage(FhirWebserviceClientProvider clientProvider,
			TaskHelper taskHelper, ReadAccessHelper readAccessHelper, KeyConsumer keyConsumer,
			ResultSetTranslatorFromMedicNoRbf resultSetTranslatorFromMedicNoRbf,
			FederatedMatcher<PersonWithMdat> federatedMatcher, ObjectMapper psnObjectMapper)
	{
		super(clientProvider, taskHelper, readAccessHelper, keyConsumer, resultSetTranslatorFromMedicNoRbf,
				federatedMatcher, psnObjectMapper);
	}
}
