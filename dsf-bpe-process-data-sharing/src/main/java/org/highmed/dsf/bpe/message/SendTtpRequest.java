package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Targets;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class SendTtpRequest extends AbstractTaskMessageSend
{
	public SendTtpRequest(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		Targets medicTargets = (Targets) execution.getVariable(BPMN_EXECUTION_VARIABLE_TARGETS);
		Stream<Task.ParameterComponent> inputMedicTargets = createTargetComponents(medicTargets);

		boolean needsRecordLinkage = (boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);
		Task.ParameterComponent inputNeedsRecordLinkage = createRecordLinkageComponent(needsRecordLinkage);

		String researchStudyIdentifier = (String) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER);
		Task.ParameterComponent inputResearchStudyIdentifier = createInputResearchStudyIdentifierComponent(
				researchStudyIdentifier);

		return Stream.concat(inputMedicTargets, Stream.of(inputNeedsRecordLinkage, inputResearchStudyIdentifier));
	}

	private Stream<Task.ParameterComponent> createTargetComponents(Targets targets)
	{
		return targets.getEntries().stream().map(target -> getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY, target.getCorrelationKey()));
	}

	private Task.ParameterComponent createRecordLinkageComponent(boolean needsRecordLinkage)
	{
		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);
	}

	private Task.ParameterComponent createInputResearchStudyIdentifierComponent(String researchStudyIdentifierValue)
	{
		Reference reference = new Reference().setType(ResourceType.ResearchStudy.name()).setIdentifier(new Identifier()
				.setValue(researchStudyIdentifierValue).setSystem(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER));

		return getTaskHelper().createInput(CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_IDENTIFIER, reference);
	}
}
