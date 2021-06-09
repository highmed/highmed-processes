package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_MEDIC;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_TTP;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_MEDIC;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_TTP;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_HIGHMED_CONSORTIUM;
import static org.highmed.dsf.bpe.ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.KeyGenerator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.BloomFilterConfig;
import org.highmed.dsf.bpe.variables.BloomFilterConfigValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;

public class SelectRequestTargets extends AbstractServiceDelegate
{
	private static final Random random = new Random();

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

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(getMedicTargets(researchStudy)));
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET, TargetValues.create(getTtpTarget(researchStudy)));

		Boolean needsRecordLinkage = (Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);
		if (Boolean.TRUE.equals(needsRecordLinkage))
		{
			execution.setVariable(BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG,
					BloomFilterConfigValues.create(createBloomFilterConfig()));
		}
	}

	private BloomFilterConfig createBloomFilterConfig()
	{
		return new BloomFilterConfig(random.nextLong(), hmacSha2Generator.generateKey(),
				hmacSha3Generator.generateKey());
	}

	private Targets getMedicTargets(ResearchStudy researchStudy)
	{
		List<Target> targets = researchStudy.getExtensionsByUrl(EXTENSION_HIGHMED_PARTICIPATING_MEDIC).stream()
				.filter(Extension::hasValue).map(Extension::getValue).filter(v -> v instanceof Reference)
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
		return researchStudy.getExtensionsByUrl(EXTENSION_HIGHMED_PARTICIPATING_TTP).stream()
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
}
