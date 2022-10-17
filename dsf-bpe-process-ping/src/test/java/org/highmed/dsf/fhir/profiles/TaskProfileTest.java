package org.highmed.dsf.fhir.profiles;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_REACHABLE;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_STATUS_VALUE_PONG_SEND;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS;
import static org.highmed.dsf.bpe.ConstantsPing.CODESYSTEM_HIGHMED_PING_VALUE_TIMER_INTERVAL;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_PING;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_PING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_PONG_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_PONG_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_PONG_TASK;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING_AUTOSTART;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING_AUTOSTART_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING_AUTOSTART_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_STOP_PING_AUTOSTART;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_STOP_PING_AUTOSTART_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_STOP_PING_AUTOSTART_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.PingProcessPluginDefinition.RELEASE_DATE;
import static org.highmed.dsf.bpe.PingProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.highmed.dsf.bpe.util.PingStatusGenerator;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.highmed.dsf.fhir.variables.Target;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
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
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(VERSION, RELEASE_DATE,
			Arrays.asList("highmed-task-base-0.5.0.xml", "highmed-extension-ping-status.xml", "highmed-task-ping.xml",
					"highmed-task-pong.xml", "highmed-task-start-ping.xml", "highmed-task-start-ping-autostart.xml",
					"highmed-task-stop-ping-autostart.xml"),
			Arrays.asList("highmed-read-access-tag-0.5.0.xml", "highmed-bpmn-message-0.5.0.xml", "highmed-ping.xml",
					"highmed-ping-status.xml"),
			Arrays.asList("highmed-read-access-tag-0.5.0.xml", "highmed-bpmn-message-0.5.0.xml", "highmed-ping.xml",
					"highmed-ping-status.xml", "highmed-pong-status.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskStartAutostartProcessProfileValid() throws Exception
	{
		Task task = createValidTaskStartAutostartProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileValidWithTargetEndpoints() throws Exception
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput()
				.setValue(new StringType(
						"Endpoint?identifier=http://highmed.org/sid/endpoint-identifier|endpoint.target.org"))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_PING)
				.setCode(CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileValidTimerInterval() throws Exception
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput().setValue(new StringType("PT24H")).getType().addCoding().setSystem(CODESYSTEM_HIGHMED_PING)
				.setCode(CODESYSTEM_HIGHMED_PING_VALUE_TIMER_INTERVAL);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileNotValidTimerInterval() throws Exception
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput().setValue(new StringType("invalid_duration")).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_PING).setCode(CODESYSTEM_HIGHMED_PING_VALUE_TIMER_INTERVAL);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskStartAutostartProcess()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_START_PING_AUTOSTART);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_START_PING_AUTOSTART_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_START_PING_AUTOSTART_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		return task;
	}

	@Test
	public void testTaskStopAutostartProcessProfileValid() throws Exception
	{
		Task task = createValidTaskStopAutostartProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskStopAutostartProcess()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_STOP_PING_AUTOSTART);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_STOP_PING_AUTOSTART_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_STOP_PING_AUTOSTART_MESSAGE_NAME)).getType()
				.addCoding().setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		return task;
	}

	@Test
	public void testTaskStartPingProcessProfileValid() throws Exception
	{
		Task task = createValidTaskStartPingProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithTargetEndpoints() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.addInput()
				.setValue(new StringType(
						"Endpoint?identifier=http://highmed.org/sid/endpoint-identifier|endpoint.target.org"))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_PING)
				.setCode(CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithBuisnessKeyOutput() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.addOutput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithBusinessKeyAndPingStatusOutput() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.addOutput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);

		Target target = Target.createBiDirectionalTarget("target.org", "endpoint.target.org",
				"https://endpoint.target.org/fhir", UUID.randomUUID().toString());
		task.addOutput(new PingStatusGenerator().createPingStatusOutput(target,
				CODESYSTEM_HIGHMED_PING_STATUS_VALUE_NOT_REACHABLE, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid1() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setInstantiatesUri("http://highmed.org/bpe/Process/ping/0.1.0"); // not valid

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid2() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setIntent(TaskIntent.FILLERORDER);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid3() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setAuthoredOn(null);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskStartPingProcess()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_START_PING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_START_PING_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		return task;
	}

	@Test
	public void testTaskPingValid() throws Exception
	{
		Task task = createValidTaskPing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskPingValidWithPingStatusOutput() throws Exception
	{
		Task task = createValidTaskPing();
		task.addOutput(new PingStatusGenerator().createPongStatusOutput(
				Target.createBiDirectionalTarget("target.org", "endpoint.target.org",
						"https://endpoint.target.org/fhir", UUID.randomUUID().toString()),
				CODESYSTEM_HIGHMED_PING_STATUS_VALUE_PONG_SEND));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskPing()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_PING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_PONG_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_PING_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY);
		task.addInput()
				.setValue(new Reference().setIdentifier(new Identifier()
						.setSystem(NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER).setValue("endpoint.target.org"))
						.setType(ResourceType.Endpoint.name()))
				.getType().addCoding().setSystem(CODESYSTEM_HIGHMED_PING)
				.setCode(CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER);

		return task;
	}

	@Test
	public void testTaskPongValid() throws Exception
	{
		Task task = createValidTaskPong();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskPong()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_PONG_TASK);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_PONG_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY);
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY);

		return task;
	}
}
