package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Targets;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class SendTtpRequest extends AbstractTaskMessageSend
{
	public SendTtpRequest(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		Targets multiInstanceTargets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);
		Stream<Task.ParameterComponent> inputTargets = getTargetComponents(multiInstanceTargets);

		boolean needsRecordLinkage = (boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);
		Task.ParameterComponent inputNeedsRecordLinkage = getRecordLinkageComponent(needsRecordLinkage);

		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY);
		Task.ParameterComponent inputResearchStudy = getInputResearchStudyIdentifierComponent(researchStudy);

		return Stream.concat(inputTargets, Stream.of(inputNeedsRecordLinkage, inputResearchStudy));
	}

	private Stream<Task.ParameterComponent> getTargetComponents(Targets targets)
	{
		return targets.getEntries().stream().map(target -> getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY, target.getCorrelationKey()));
	}

	private Task.ParameterComponent getRecordLinkageComponent(boolean needsRecordLinkage)
	{
		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);
	}

	private Task.ParameterComponent getInputResearchStudyIdentifierComponent(ResearchStudy researchStudy)
	{
		Identifier identifier = researchStudy.getIdentifier().stream()
				.filter(i -> i.getSystem().equals(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Identifier is not set in research study with id='"
						+ researchStudy.getId() + "', this error should have been caught by resource validation"));

		Reference reference = new Reference().setIdentifier(identifier);

		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_IDENTIFIER, reference);
	}
}
