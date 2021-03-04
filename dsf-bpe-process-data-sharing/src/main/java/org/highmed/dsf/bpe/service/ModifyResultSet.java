package org.highmed.dsf.bpe.service;

import java.util.Objects;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModifyResultSet extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ModifyResultSet.class);

	private static final int RBF_LENGTH = 3000;
	private static final RecordBloomFilterGeneratorImpl.FieldWeights FBF_WEIGHTS = new RecordBloomFilterGeneratorImpl.FieldWeights(
			0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
	private static final RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths FBF_LENGTHS = new RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths(
			500, 500, 250, 50, 500, 250, 500, 500, 500);

	private final String ehrIdColumnPath;
	private final MasterPatientIndexClient masterPatientIndexClient;
	private final ObjectMapper openEhrObjectMapper;
	private final BouncyCastleProvider bouncyCastleProvider;

	public ModifyResultSet(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper, String ehrIdColumnPath,
			MasterPatientIndexClient masterPatientIndexClient, ObjectMapper openEhrObjectMapper,
			BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper);

		this.ehrIdColumnPath = ehrIdColumnPath;
		this.masterPatientIndexClient = masterPatientIndexClient;
		this.openEhrObjectMapper = openEhrObjectMapper;
		this.bouncyCastleProvider = bouncyCastleProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");
		Objects.requireNonNull(masterPatientIndexClient, "masterPatientIndexClient");
		Objects.requireNonNull(openEhrObjectMapper, "openEhrObjectMapper");
		Objects.requireNonNull(bouncyCastleProvider, "bouncyCastleProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		logger.info(this.getClass().getName() + " doExecute called");
	}
}
