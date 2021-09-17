package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_MEDIC;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_TTP;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_HIGHMED_CONSORTIUM;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.KeyGenerator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.BloomFilterConfig;
import org.highmed.dsf.bpe.variable.BloomFilterConfigValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.springframework.beans.factory.InitializingBean;

public class SelectRequestTargets extends AbstractServiceDelegate implements InitializingBean
{
	private final Random random = new Random();

	private final EndpointProvider endpointProvider;
	private final KeyGenerator hmacSha2Generator;
	private final KeyGenerator hmacSha3Generator;

	public SelectRequestTargets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, EndpointProvider endpointProvider,
			BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.endpointProvider = endpointProvider;

		try
		{
			Objects.requireNonNull(bouncyCastleProvider, "bouncyCastleProvider");

			hmacSha2Generator = KeyGenerator.getInstance("HmacSHA256", bouncyCastleProvider);
			hmacSha3Generator = KeyGenerator.getInstance("HmacSHA3-256", bouncyCastleProvider);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(endpointProvider, "endpointProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY);

		Targets medicTargets = getMedicTargets(researchStudy);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(medicTargets));

		Target ttpTarget = getTtpTarget(researchStudy);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET, TargetValues.create(ttpTarget));

		byte[] mdatKey = AesGcmUtil.generateAES256Key().getEncoded();
		execution.setVariable(BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY, mdatKey);

		Boolean needsRecordLinkage = (Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);
		if (Boolean.TRUE.equals(needsRecordLinkage))
		{
			BloomFilterConfig bloomFilterConfig = createBloomFilterConfig();
			execution.setVariable(BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG,
					BloomFilterConfigValues.create(bloomFilterConfig));
		}
	}

	private Targets getMedicTargets(ResearchStudy researchStudy)
	{
		List<Target> targets = researchStudy.getExtensionsByUrl(ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_MEDIC)
				.stream().filter(Extension::hasValue).map(Extension::getValue).filter(v -> v instanceof Reference)
				.map(v -> (Reference) v).filter(Reference::hasIdentifier).map(Reference::getIdentifier)
				.filter(Identifier::hasValue).map(Identifier::getValue)
				.map(medicIdentifier -> Target.createBiDirectionalTarget(medicIdentifier,
						getAddress(CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_MEDIC, medicIdentifier),
						UUID.randomUUID().toString()))
				.collect(Collectors.toList());

		return new Targets(targets);
	}

	private Target getTtpTarget(ResearchStudy researchStudy)
	{
		return researchStudy.getExtensionsByUrl(ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_TTP).stream()
				.filter(Extension::hasValue).map(Extension::getValue).filter(v -> v instanceof Reference)
				.map(v -> (Reference) v).filter(Reference::hasIdentifier).map(Reference::getIdentifier)
				.filter(Identifier::hasValue).map(Identifier::getValue)
				.map(ttpIdentifier -> Target.createUniDirectionalTarget(ttpIdentifier,
						getAddress(CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_TTP, ttpIdentifier)))
				.findFirst().get();
	}

	private String getAddress(String role, String identifier)
	{
		return endpointProvider
				.getFirstConsortiumEndpointAdress(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_HIGHMED_CONSORTIUM,
						CODESYSTEM_HIGHMED_ORGANIZATION_TYPE, role, identifier)
				.get();
	}

	private BloomFilterConfig createBloomFilterConfig()
	{
		return new BloomFilterConfig(random.nextLong(), hmacSha2Generator.generateKey(),
				hmacSha3Generator.generateKey());
	}
}
