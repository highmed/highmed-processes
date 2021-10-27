package org.highmed.dsf.bpe.service;

import org.highmed.dsf.bpe.crypto.KeyConsumer;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcher;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicWithRbf;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymizeResultsSecondOrderWithRecordLinkage extends AbstractPseudonymizeResultsSecondOrder
		implements InitializingBean
{
	public PseudonymizeResultsSecondOrderWithRecordLinkage(FhirWebserviceClientProvider clientProvider,
			TaskHelper taskHelper, ReadAccessHelper readAccessHelper, KeyConsumer keyConsumer,
			ResultSetTranslatorFromMedicWithRbf resultSetTranslatorFromMedicWithRbf,
			FederatedMatcher<PersonWithMdat> federatedMatcher, ObjectMapper psnObjectMapper)
	{
		super(clientProvider, taskHelper, readAccessHelper, keyConsumer, resultSetTranslatorFromMedicWithRbf,
				federatedMatcher, psnObjectMapper);
	}
}
