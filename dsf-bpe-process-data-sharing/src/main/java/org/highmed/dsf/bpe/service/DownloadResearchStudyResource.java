package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_MEDIC_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DownloadResearchStudyResource extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResearchStudyResource.class);

	private final OrganizationProvider organizationProvider;

	public DownloadResearchStudyResource(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		Task task = getLeadingTaskFromExecutionVariables();
		FhirWebserviceClient client = getFhirWebserviceClientProvider().getLocalWebserviceClient();

		IdType researchStudyId = getResearchStudyId(task);
		ResearchStudy researchStudy = getResearchStudy(researchStudyId, client);
		String researchStudyIdentifier = getResearchStudyIdentifier(researchStudy);
		boolean needsConsentCheck = getNeedsConsentCheck(task);
		boolean needsRecordLinkage = getNeedsRecordLinkageCheck(task);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY, researchStudy);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY_IDENTIFIER, researchStudyIdentifier);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_LEADING_MEDIC_IDENTIFIER,
				organizationProvider.getLocalIdentifierValue());
	}

	private IdType getResearchStudyId(Task task)
	{
		Reference researchStudyReference = getTaskHelper()
				.getInputParameterReferenceValues(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("ResearchStudy reference is not set in task with id='"
						+ task.getId() + "', this error should have been caught by resource validation"));

		return new IdType(researchStudyReference.getReference());
	}

	private ResearchStudy getResearchStudy(IdType researchStudyid, FhirWebserviceClient client)
	{
		try
		{
			return client.read(ResearchStudy.class, researchStudyid.getIdPart());
		}
		catch (Exception e)
		{
			logger.warn("Error while reading ResearchStudy with id {} from {}", researchStudyid.getIdPart(),
					client.getBaseUrl());
			throw e;
		}
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK)
				.orElseThrow(() -> new IllegalArgumentException("NeedsConsentCheck boolean is not set in task with id='"
						+ task.getId() + "', this error should " + "have been caught by resource validation"));
	}

	private boolean getNeedsRecordLinkageCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, CODESYSTEM_HIGHMED_DATA_SHARING,
						CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE)
				.orElseThrow(
						() -> new IllegalArgumentException("NeedsRecordLinkage boolean is not set in task with id='"
								+ task.getId() + "', this error should " + "have been caught by resource validation"));
	}

	private String getResearchStudyIdentifier(ResearchStudy researchStudy)
	{
		Identifier identifier = researchStudy.getIdentifier().stream()
				.filter(i -> i.getSystem().equals(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Identifier is not set in research study with id='"
						+ researchStudy.getId() + "', this error should have been caught by resource validation"));

		return identifier.getValue();
	}
}
