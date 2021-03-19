package org.highmed.dsf.fhir.profile;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MDAT_AES_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SET_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SET_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_COMPUTE_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_COMPUTE_DATA_SHARING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_COMPUTE_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_ERROR_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_ERROR_DATA_SHARING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_EXECUTE_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_EXECUTE_DATA_SHARING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_EXECUTE_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_DATA_SHARING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_DATA_SHARING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.DataSharingProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class TaskProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(TaskProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(VERSION,
			Arrays.asList("highmed-task-base-0.5.0.xml", "highmed-group-0.5.0.xml",
					"highmed-extension-group-id-0.5.0.xml", "highmed-research-study-0.5.0.xml",
					"highmed-task-request-data-sharing.xml", "highmed-task-execute-data-sharing.xml",
					"highmed-task-single-medic-result-data-sharing.xml", "highmed-task-compute-data-sharing.xml",
					"highmed-task-multi-medic-result-data-sharing.xml",
					"highmed-task-multi-medic-error-data-sharing.xml"),
			Arrays.asList("highmed-authorization-role-0.5.0.xml", "highmed-bpmn-message-0.5.0.xml",
					"highmed-data-sharing.xml"),
			Arrays.asList("highmed-authorization-role-0.5.0.xml", "highmed-bpmn-message-0.5.0.xml",
					"highmed-data-sharing.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskRequestDataSharingValid() throws Exception
	{
		Task task = createValidTaskRequestDataSharing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskRequestDataSharingValidWithOutput() throws Exception
	{
		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		Task task = createValidTaskRequestDataSharing();

		TaskOutputComponent outMultiMedicResult1 = task.addOutput();
		outMultiMedicResult1.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SET_REFERENCE);
		outMultiMedicResult1.addExtension("http://highmed.org/fhir/StructureDefinition/extension-group-id",
				new Reference(groupId1));

		TaskOutputComponent outMultiMedicResult2 = task.addOutput();
		outMultiMedicResult2.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_COUNT_RESULT);
		outMultiMedicResult2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskRequestDataSharing()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new Reference("ResearchStudy/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE);
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE);
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK);

		return task;
	}

	@Test
	public void testTaskExecuteDataSharingValid() throws Exception
	{
		Task task = createValidTaskExecuteDataSharing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskExecuteDataSharingValidWithBloomFilterConfig() throws Exception
	{
		Task task = createValidTaskExecuteDataSharing();
		task.addInput().setValue(new Base64BinaryType("TEST".getBytes(StandardCharsets.UTF_8))).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_BLOOM_FILTER_CONFIG);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskExecuteDataSharing() throws NoSuchAlgorithmException
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_EXECUTE_DATA_SHARING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_EXECUTE_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 2");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_EXECUTE_DATA_SHARING_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY);

		task.addInput().setValue(new Reference("ResearchStudy/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_REFERENCE);
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE);
		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_CONSENT_CHECK);
		task.addInput().setValue(new Base64BinaryType(AesGcmUtil.generateAES256Key().getEncoded())).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MDAT_AES_KEY);

		return task;
	}

	@Test
	public void testTaskSingleMedicResultDataSharingReferenceResultValid() throws Exception
	{
		Task task = createValidTaskSingleMedicResultDataSharingReferenceResult();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskSingleMedicResultDataSharing()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_DATA_SHARING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_COMPUTE_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 2");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_DATA_SHARING_MESSAGE_NAME))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN)
				.setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY);

		return task;
	}

	private Task createValidTaskSingleMedicResultDataSharingReferenceResult()
	{
		Task task = createValidTaskSingleMedicResultDataSharing();

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inSingleMedicResult1 = task.addInput();
		inSingleMedicResult1.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SET_REFERENCE);
		inSingleMedicResult1.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId1));
		ParameterComponent inSingleMedicResult2 = task.addInput();
		inSingleMedicResult2.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_SINGLE_MEDIC_RESULT_SET_REFERENCE);
		inSingleMedicResult2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskComputeDataSharingValid() throws Exception
	{
		Task task = createValidTaskComputeDataSharing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskComputeDataSharing()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_COMPUTE_DATA_SHARING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_COMPUTE_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_COMPUTE_DATA_SHARING_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		Reference rsRef = new Reference().setIdentifier(new Identifier().setValue(UUID.randomUUID().toString())
				.setSystem(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER));
		task.addInput().setValue(rsRef).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_RESEARCH_STUDY_IDENTIFIER);

		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY);

		task.addInput().setValue(new BooleanType(false)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_NEEDS_RECORD_LINKAGE);

		return task;
	}

	@Test
	public void testTaskMultiMedicResultDataSharingValid() throws Exception
	{
		Task task = createValidTaskMultiMedicResultDataSharing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskMultiMedicResultDataSharing()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_DATA_SHARING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_DATA_SHARING_MESSAGE_NAME))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN)
				.setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		String groupId1 = "Group/" + UUID.randomUUID().toString();
		String groupId2 = "Group/" + UUID.randomUUID().toString();

		ParameterComponent inMultiMedicResult1 = task.addInput();
		inMultiMedicResult1.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SET_REFERENCE);
		inMultiMedicResult1.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId1));

		ParameterComponent inMultiMedicResult2 = task.addInput();
		inMultiMedicResult2.setValue(new Reference("Binary/" + UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_DATA_SHARING)
				.setCode(CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SET_REFERENCE);
		inMultiMedicResult2.addExtension(EXTENSION_HIGHMED_GROUP_ID, new Reference(groupId2));

		return task;
	}

	@Test
	public void testTaskErrorDataSharingValid() throws Exception
	{
		Task task = createValidTaskErrorDataSharing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskErrorDataSharing()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_ERROR_DATA_SHARING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_REQUEST_DATA_SHARING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_ERROR_DATA_SHARING_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		ParameterComponent error = task.addInput();
		error.setValue(new StringType("Could not calculate multi medic results sets for all defined cohorts")).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR);

		return task;
	}
}
