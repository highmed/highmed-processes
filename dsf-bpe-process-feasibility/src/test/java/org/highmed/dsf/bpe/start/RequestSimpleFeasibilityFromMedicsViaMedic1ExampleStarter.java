package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.AQL_QUERY_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_QUERY_URI;
import static org.highmed.dsf.bpe.ConstantsBase.ORGANIZATION_IDENTIFIER_SYSTEM;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.CERTIFICATE_PASSWORD;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.CERTIFICATE_PATH;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.MEDIC_1_FHIR_BASE_URL;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ORGANIZATION_IDENTIFIER_VALUE_MEDIC_2;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ORGANIZATION_IDENTIFIER_VALUE_MEDIC_3;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.ORGANIZATION_IDENTIFIER_VALUE_TTP;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.EXTENSION_PARTICIPATING_MEDIC_URI;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.EXTENSION_PARTICIPATING_TTP_URI;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.FEASIBILITY_RESEARCH_STUDY_PROFILE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.GROUP_PROFILE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.REQUEST_FEASIBILITY_MESSAGE_NAME;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.REQUEST_FEASIBILITY_TASK_PROFILE;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.RESEARCH_STUDY_IDENTIFIER_SYSTEM;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.fhir.client.FhirWebserviceClientJersey;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

import ca.uhn.fhir.context.FhirContext;

import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class RequestSimpleFeasibilityFromMedicsViaMedic1ExampleStarter
{
	public static void main(String[] args)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		KeyStore keyStore = CertificateReader.fromPkcs12(Paths.get(CERTIFICATE_PATH), CERTIFICATE_PASSWORD);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext context = FhirContext.forR4();
		ReferenceCleaner referenceCleaner = new ReferenceCleanerImpl(new ReferenceExtractorImpl());
		FhirWebserviceClient client = new FhirWebserviceClientJersey(MEDIC_1_FHIR_BASE_URL, trustStore, keyStore,
				CERTIFICATE_PASSWORD, null, null, null, 0, 0, null, context, referenceCleaner);

		try
		{
			Group group1 = createGroup("Group 1");
			Group group2 = createGroup("Group 2");
			ResearchStudy researchStudy = createResearchStudy(group1, group2);
			Task task = createTask(researchStudy);

			Bundle bundle = new Bundle();
			bundle.setType(BundleType.TRANSACTION);
			bundle.addEntry().setResource(group1).setFullUrl(group1.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl(ResourceType.Group.name());
			bundle.addEntry().setResource(group2).setFullUrl(group2.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl(ResourceType.Group.name());
			bundle.addEntry().setResource(researchStudy).setFullUrl(researchStudy.getIdElement().getIdPart())
					.getRequest().setMethod(HTTPVerb.POST).setUrl(ResourceType.ResearchStudy.name());
			bundle.addEntry().setResource(task).setFullUrl(task.getIdElement().getIdPart()).getRequest()
					.setMethod(HTTPVerb.POST).setUrl(ResourceType.Task.name());

			client.withMinimalReturn().postBundle(bundle);
		}
		catch (WebApplicationException e)
		{
			if (e.getResponse() != null && e.getResponse().hasEntity())
			{
				OperationOutcome outcome = e.getResponse().readEntity(OperationOutcome.class);
				String xml = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(outcome);
				System.out.println(xml);
			}
			else
				e.printStackTrace();
		}
	}

	private static Group createGroup(String name)
	{
		Group group = new Group();
		group.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		group.getMeta().addProfile(GROUP_PROFILE);
		group.getText().getDiv().addText("This is the description");
		group.getText().setStatus(Narrative.NarrativeStatus.ADDITIONAL);
		group.setType(GroupType.PERSON);
		group.setActual(false);
		group.setActive(true);
		group.addExtension().setUrl(EXTENSION_QUERY_URI).setValue(
				new Expression().setLanguageElement(AQL_QUERY_TYPE).setExpression("SELECT COUNT(e) FROM EHR e"));
		group.setName(name);

		return group;
	}

	private static ResearchStudy createResearchStudy(Group group1, Group group2)
	{
		ResearchStudy researchStudy = new ResearchStudy();
		researchStudy.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		researchStudy.getMeta().addProfile(FEASIBILITY_RESEARCH_STUDY_PROFILE);
		researchStudy.addIdentifier().setSystem(RESEARCH_STUDY_IDENTIFIER_SYSTEM)
				.setValue(UUID.randomUUID().toString());
		researchStudy.setStatus(ResearchStudyStatus.ACTIVE);
		researchStudy.addEnrollment().setReference(group1.getIdElement().getIdPart());
		researchStudy.addEnrollment().setReference(group2.getIdElement().getIdPart());

		researchStudy.addExtension().setUrl(EXTENSION_PARTICIPATING_MEDIC_URI).setValue(
				new Reference().setType(ResourceType.Organization.name()).setIdentifier(
						new Identifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM)
								.setValue(ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1)));
		researchStudy.addExtension().setUrl(EXTENSION_PARTICIPATING_MEDIC_URI).setValue(
				new Reference().setType(ResourceType.Organization.name()).setIdentifier(
						new Identifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM)
								.setValue(ORGANIZATION_IDENTIFIER_VALUE_MEDIC_2)));
		researchStudy.addExtension().setUrl(EXTENSION_PARTICIPATING_MEDIC_URI).setValue(
				new Reference().setType(ResourceType.Organization.name()).setIdentifier(
						new Identifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM)
								.setValue(ORGANIZATION_IDENTIFIER_VALUE_MEDIC_3)));
		researchStudy.addExtension().setUrl(EXTENSION_PARTICIPATING_TTP_URI).setValue(
				new Reference().setType(ResourceType.Organization.name()).setIdentifier(
						new Identifier().setSystem(ORGANIZATION_IDENTIFIER_SYSTEM)
								.setValue(ORGANIZATION_IDENTIFIER_VALUE_TTP)));

		return researchStudy;
	}

	private static Task createTask(ResearchStudy researchStudy)
	{
		Task task = new Task();
		task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

		task.getMeta().addProfile(REQUEST_FEASIBILITY_TASK_PROFILE);
		task.setInstantiatesUri(REQUEST_FEASIBILITY_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue(ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ORGANIZATION_IDENTIFIER_SYSTEM).setValue(ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);

		task.addInput().setValue(new StringType(REQUEST_FEASIBILITY_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new Reference().setReference(researchStudy.getIdElement().getIdPart())
				.setType(ResourceType.ResearchStudy.name())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE);
		task.addInput().setValue(new BooleanType(true)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE);
		task.addInput().setValue(new BooleanType(true)).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK);

		return task;
	}
}
