package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_QUERY_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsLocalServices.PROFILE_HIGHMED_TASK_LOCAL_SERVICES;
import static org.highmed.dsf.bpe.ConstantsLocalServices.PROFILE_HIGHMED_TASK_LOCAL_SERVICES_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsLocalServices.PROFILE_HIGHMED_TASK_LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.MEDIC_1_FHIR_BASE_URL;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1;

import java.util.Date;
import java.util.Random;

import javax.crypto.KeyGenerator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.dsf.bpe.variables.BloomFilterConfig;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

public class LocalServicesMedic1ExampleStarter
{
	private static boolean NEEDS_CONSENT_CHECK = true;
	private static boolean NEEDS_RECORD_LINKAGE = true;

	// Environment variable "DSF_CLIENT_CERTIFICATE_PATH" or args[0]: the path to the client-certificate
	//    highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12
	// Environment variable "DSF_CLIENT_CERTIFICATE_PASSWORD" or args[1]: the password of the client-certificate
	//    password
	public static void main(String[] args) throws Exception
	{
		Task task = createStartResource();
		ExampleStarter.forServer(args, MEDIC_1_FHIR_BASE_URL).startWith(task);
	}

	private static Task createStartResource()
	{
		Task task = new Task();

		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_LOCAL_SERVICES);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_LOCAL_SERVICES_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setAuthoredOn(new Date());

		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_LOCAL_SERVICES_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		task.addInput().setValue(new StringType("SELECT COUNT(e) FROM EHR e;")).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_QUERY_TYPE).setCode(CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL);
		task.addInput().setValue(new BooleanType(NEEDS_CONSENT_CHECK)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK);
		task.addInput().setValue(new BooleanType(NEEDS_RECORD_LINKAGE)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
				.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE);

		if (NEEDS_RECORD_LINKAGE)
		{
			try
			{
				BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
				BloomFilterConfig bloomFilterConfig = new BloomFilterConfig(new Random().nextLong(),
						KeyGenerator.getInstance("HmacSHA256", bouncyCastleProvider).generateKey(),
						KeyGenerator.getInstance("HmacSHA3-256", bouncyCastleProvider).generateKey());

				task.addInput().setValue(new Base64BinaryType(bloomFilterConfig.toBytes())).getType().addCoding()
						.setSystem(CODESYSTEM_HIGHMED_FEASIBILITY)
						.setCode(CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG);
			}
			catch (Exception exception)
			{
				throw new RuntimeException("Could not create BloomFilterConfig", exception);
			}
		}

		return task;
	}
}
