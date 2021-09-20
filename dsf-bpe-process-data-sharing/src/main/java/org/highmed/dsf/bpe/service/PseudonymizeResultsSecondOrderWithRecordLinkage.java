package org.highmed.dsf.bpe.service;

import org.highmed.dsf.bpe.crypto.KeyConsumer;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicWithRbfImpl;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymizeResultsSecondOrderWithRecordLinkage extends PseudonymizeResultsSecondOrder
		implements InitializingBean
{
	public PseudonymizeResultsSecondOrderWithRecordLinkage(FhirWebserviceClientProvider clientProvider,
			TaskHelper taskHelper, ReadAccessHelper readAccessHelper, KeyConsumer keyConsumer,
			ObjectMapper psnObjectMapper)
	{
		super(clientProvider, taskHelper, readAccessHelper, keyConsumer, psnObjectMapper);
	}

	@Override
	protected ResultSetTranslatorFromMedic createResultSetTranslatorFromMedic()
	{
		return new ResultSetTranslatorFromMedicWithRbfImpl();
	}
}
