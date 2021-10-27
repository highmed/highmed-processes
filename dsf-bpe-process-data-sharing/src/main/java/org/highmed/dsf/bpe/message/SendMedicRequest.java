package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MDAT_AES_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.variable.BloomFilterConfig;
import org.highmed.dsf.bpe.variable.SecretKeyWrapper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;

public class SendMedicRequest extends AbstractTaskMessageSend
{
	public SendMedicRequest(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY);
		ParameterComponent inputResearchStudyReference = createResearchStudyComponent(researchStudy);

		boolean needsConsentCheck = (boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK);
		ParameterComponent inputNeedsConsentCheck = createNeedsConsentCheckComponent(needsConsentCheck);

		boolean needsRecordLinkage = (boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);
		ParameterComponent inputNeedsRecordLinkage = createNeedsRecordLinkageComponent(needsRecordLinkage);

		SecretKeyWrapper secretKeyWrapper = (SecretKeyWrapper) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_MDAT_AES_KEY);
		ParameterComponent inputMdatKey = createMdatKeyComponent(secretKeyWrapper);

		if (needsRecordLinkage)
		{
			BloomFilterConfig bloomFilterConfig = (BloomFilterConfig) execution
					.getVariable(BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG);
			ParameterComponent inputBloomFilterConfig = getBloomFilterConfigComponent(bloomFilterConfig);

			return Stream.of(inputResearchStudyReference, inputNeedsConsentCheck, inputNeedsRecordLinkage,
					inputBloomFilterConfig, inputMdatKey);
		}
		else
		{
			return Stream.of(inputResearchStudyReference, inputNeedsConsentCheck, inputNeedsRecordLinkage,
					inputMdatKey);
		}
	}

	private Task.ParameterComponent createResearchStudyComponent(ResearchStudy researchStudy)
	{
		IdType researchStudyId = new IdType(
				getFhirWebserviceClientProvider().getLocalBaseUrl() + "/" + researchStudy.getId());
		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE,
				new Reference().setReference(researchStudyId.toVersionless().getValueAsString()));
	}

	private Task.ParameterComponent createNeedsConsentCheckComponent(boolean needsConsentCheck)
	{
		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK, needsConsentCheck);
	}

	private Task.ParameterComponent createNeedsRecordLinkageComponent(boolean needsRecordLinkage)
	{
		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);
	}

	private Task.ParameterComponent createMdatKeyComponent(SecretKeyWrapper secretKeyWrapper)
	{
		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MDAT_AES_KEY, secretKeyWrapper.getBytes());
	}

	private Task.ParameterComponent getBloomFilterConfigComponent(BloomFilterConfig bloomFilterConfig)
	{
		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_BLOOM_FILTER_CONFIG, bloomFilterConfig.toBytes());
	}
}
