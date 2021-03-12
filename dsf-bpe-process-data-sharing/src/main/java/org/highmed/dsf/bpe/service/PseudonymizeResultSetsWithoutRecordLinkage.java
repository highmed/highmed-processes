package org.highmed.dsf.bpe.service;

import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicNoRbfImpl;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymizeResultSetsWithoutRecordLinkage extends PseudonymizeResultSets implements InitializingBean
{
	public PseudonymizeResultSetsWithoutRecordLinkage(FhirWebserviceClientProvider clientProvider,
			TaskHelper taskHelper, ObjectMapper psnObjectMapper)
	{
		super(clientProvider, taskHelper, psnObjectMapper);
	}

	@Override
	protected ResultSetTranslatorFromMedic createResultSetTranslatorFromMedic()
	{
		return new ResultSetTranslatorFromMedicNoRbfImpl();
	}
}
