package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.CODE_TYPE_AQL_QUERY;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.PROFILE_HIGHEMD_RESEARCH_STUDY;
import static org.highmed.dsf.bpe.ConstantsBase.PROFILE_HIGHMED_GROUP;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_CONTRACT_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_FEASIBILITY_QUERY_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_REQUEST_FORM_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.EXTENSION_HIGHMED_QUERY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_AND_VERSION;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_2;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_3;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelperImpl;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public abstract class AbstractRequestDataSharingFromMedicsViaMedic1ExampleStarter
{
	private final boolean needsConsentCheck = true;
	private final boolean needsRecordLinkage = true;

	private final String requestFormReference = "https://foo/fhir/Binary/9f747003-5d80-4313-b77f-d6dbe2ef4c55";
	private final String contractReference = "https://foo/fhir/Binary/9f747003-5d80-4313-b77f-d6dbe2ef4c55";
	private final String feasibilityReference = "https://foo/fhir/Task/9f747003-5d80-4313-b77f-d6dbe2ef4c55";

	private final String query = "SELECT e/ehr_status/subject/external_ref/id/value as EHRID, "
			+ "v/items[at0024,'Bezeichnung']/value, v/items [at0001,'Messwert'], "
			+ "v/items[at0006,'Dokumentationsdatum Untersuchung']/value FROM EHR e CONTAINS COMPOSITION c "
			+ "CONTAINS CLUSTER v[openEHR-EHR-CLUSTER.laboratory_test_analyte.v1] "
			+ "WHERE v/items[at0024,'Bezeichnung']/value/value = 'Natrium' OFFSET 0 LIMIT 15;";

	private final String[] medicIdentifier = new String[] { NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1,
			NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_2,
			NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_3 };
	private final String ttpIdentifier = NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP;

	private final ReadAccessHelper readAccessHelper = new ReadAccessHelperImpl();

	protected void main(String[] args, String baseUrl) throws Exception
	{
		Bundle bundle = createStartResource();
		ExampleStarter.forServer(args, baseUrl).startWith(bundle);
	}

	private Bundle createStartResource()
	{
		Group group1 = createGroup("Group 1");
		Group group2 = createGroup("Group 2");
		ResearchStudy researchStudy = createResearchStudy(group1, group2);
		Task task = createTask(researchStudy);

		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.TRANSACTION);
		bundle.addEntry().setResource(group1).setFullUrl(group1.getIdElement().getIdPart()).getRequest()
				.setMethod(Bundle.HTTPVerb.POST).setUrl(ResourceType.Group.name());
		bundle.addEntry().setResource(group2).setFullUrl(group2.getIdElement().getIdPart()).getRequest()
				.setMethod(Bundle.HTTPVerb.POST).setUrl(ResourceType.Group.name());
		bundle.addEntry().setResource(researchStudy).setFullUrl(researchStudy.getIdElement().getIdPart()).getRequest()
				.setMethod(Bundle.HTTPVerb.POST).setUrl(ResourceType.ResearchStudy.name());
		bundle.addEntry().setResource(task).setFullUrl(task.getIdElement().getIdPart()).getRequest()
				.setMethod(Bundle.HTTPVerb.POST).setUrl(ResourceType.Task.name());

		return bundle;
	}

	private Group createGroup(String name)
	{
		Group group = new Group();
		group.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		group.getMeta().addProfile(PROFILE_HIGHMED_GROUP);
		group.getText().getDiv().addText("This is the description");
		group.getText().setStatus(Narrative.NarrativeStatus.ADDITIONAL);
		group.setType(Group.GroupType.PERSON);
		group.setActual(false);
		group.setActive(true);
		group.addExtension().setUrl(EXTENSION_HIGHMED_QUERY)
				.setValue(new Expression().setLanguageElement(CODE_TYPE_AQL_QUERY).setExpression(query));
		group.setName(name);

		Arrays.stream(medicIdentifier).forEach(i -> readAccessHelper.addOrganization(group, i));
		readAccessHelper.addOrganization(group, ttpIdentifier);

		return group;
	}

	private ResearchStudy createResearchStudy(Group group1, Group group2)
	{
		ResearchStudy researchStudy = new ResearchStudy();
		researchStudy.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		researchStudy.getMeta().addProfile(PROFILE_HIGHEMD_RESEARCH_STUDY);
		researchStudy.addIdentifier().setSystem(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER)
				.setValue(UUID.randomUUID().toString());
		researchStudy.setStatus(ResearchStudy.ResearchStudyStatus.ACTIVE);

		researchStudy.addEnrollment().setReference(group1.getIdElement().getIdPart());
		researchStudy.addEnrollment().setReference(group2.getIdElement().getIdPart());

		researchStudy.addRelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.DOCUMENTATION)
				.setLabel(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_REQUEST_FORM_REFERENCE).setUrl(requestFormReference);
		researchStudy.addRelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.JUSTIFICATION)
				.setLabel(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_CONTRACT_REFERENCE).setUrl(contractReference);
		researchStudy.addRelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.JUSTIFICATION)
				.setLabel(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_FEASIBILITY_QUERY_REFERENCE)
				.setUrl(feasibilityReference);

		Arrays.stream(medicIdentifier)
				.forEach(i -> researchStudy.addExtension().setUrl(ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_MEDIC)
						.setValue(new Reference().setType(ResourceType.Organization.name()).setIdentifier(
								new Identifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue(i))));

		researchStudy.addExtension().setUrl(ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_TTP)
				.setValue(new Reference().setType(ResourceType.Organization.name()).setIdentifier(new Identifier()
						.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue(ttpIdentifier)));

		Arrays.stream(medicIdentifier).forEach(i -> readAccessHelper.addOrganization(researchStudy, i));
		readAccessHelper.addOrganization(researchStudy, ttpIdentifier);

		return researchStudy;
	}

	private Task createTask(ResearchStudy researchStudy)
	{
		Task task = new Task();
		task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_AND_VERSION);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());

		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput()
				.setValue(new Reference().setReference(researchStudy.getIdElement().getIdPart())
						.setType(ResourceType.ResearchStudy.name()))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE);
		task.addInput().setValue(new BooleanType(needsRecordLinkage)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE);
		task.addInput().setValue(new BooleanType(needsConsentCheck)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK);

		return task;
	}
}
