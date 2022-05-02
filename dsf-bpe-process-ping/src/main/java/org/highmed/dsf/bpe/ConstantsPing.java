package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_HIGHMED_URI_BASE;
import static org.highmed.dsf.bpe.PingProcessPluginDefinition.VERSION;

public interface ConstantsPing
{
	String PROFILE_HIGHMED_TASK_START_AUTOSTART = "http://highmed.org/fhir/StructureDefinition/task-start-autostart-ping-process";
	String PROFILE_HIGHMED_TASK_START_AUTOSTART_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_START_AUTOSTART + "|"
			+ VERSION;
	String PROFILE_HIGHMED_TASK_START_AUTOSTART_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "autostartPing/";
	String PROFILE_HIGHMED_TASK_START_AUTOSTART_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_START_AUTOSTART_PROCESS_URI
			+ VERSION;
	String PROFILE_HIGHMED_TASK_START_AUTOSTART_MESSAGE_NAME = "startAutostartPingProcessMessage";

	String PROFILE_HIGHMED_TASK_STOP_AUTOSTART = "http://highmed.org/fhir/StructureDefinition/task-stop-autostart-ping-process";
	String PROFILE_HIGHMED_TASK_STOP_AUTOSTART_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_STOP_AUTOSTART + "|" + VERSION;
	String PROFILE_HIGHMED_TASK_STOP_AUTOSTART_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "autostartPing/";
	String PROFILE_HIGHMED_TASK_STOP_AUTOSTART_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_STOP_AUTOSTART_PROCESS_URI
			+ VERSION;
	String PROFILE_HIGHMED_TASK_STOP_AUTOSTART_MESSAGE_NAME = "stopAutostartPingProcessMessage";

	String PROFILE_HIGHMED_TASK_START_PING = "http://highmed.org/fhir/StructureDefinition/task-start-ping-process";
	String PROFILE_HIGHMED_TASK_START_PING_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_START_PING + "|" + VERSION;
	String PROFILE_HIGHMED_TASK_START_PING_MESSAGE_NAME = "startPingProcessMessage";

	String PROFILE_HIGHMED_TASK_PING = "http://highmed.org/fhir/StructureDefinition/task-ping";
	String PROFILE_HIGHMED_TASK_PING_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_PING + "|" + VERSION;
	String PROFILE_HIGHMED_TASK_PING_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "ping/";
	String PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_PING_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_PING_MESSAGE_NAME = "pingMessage";

	String PROFILE_HIGHMED_TASK_PONG_TASK = "http://highmed.org/fhir/StructureDefinition/task-pong";
	String PROFILE_HIGHMED_TASK_PONG_TASK_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_PONG_TASK + "|" + VERSION;
	String PROFILE_HIGHMED_TASK_PONG_PROCESS_URI = PROCESS_HIGHMED_URI_BASE + "pong/";
	String PROFILE_HIGHMED_TASK_PONG_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_PONG_PROCESS_URI + VERSION;
	String PROFILE_HIGHMED_TASK_PONG_MESSAGE_NAME = "pongMessage";

	String CODESYSTEM_HIGHMED_PING = "http://highmed.org/fhir/CodeSystem/ping";
	String CODESYSTEM_HIGHMED_PING_VALUE_PING_RESPONSE = "ping-response";
	String CODESYSTEM_HIGHMED_PING_VALUE_ENDPOINT_IDENTIFIER = "endpoint-identifier";
	String CODESYSTEM_HIGHMED_PING_VALUE_TARGET_ENDPOINTS = "target-endpoints";
	String CODESYSTEM_HIGHMED_PING_VALUE_TIMER_INTERVAL = "timer-interval";

	String CODESYSTEM_HIGHMED_PING_RESPONSE = "http://highmed.org/fhir/CodeSystem/ping-response";
	String CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_RECEIVED = "received";
	String CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_MISSING = "missing";
	String CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_NOT_REACHABLE = "not-reachable";
	String CODESYSTEM_HIGHMED_PING_RESPONSE_VALUE_NOT_ALLOWED = "not-allowed";

	String EXTENSION_URL_PING_RESPONSE = "http://highmed.org/fhir/StructureDefinition/extension-ping-response";
	String EXTENSION_URL_CORRELATION_KEY = "correlation-key";
	String EXTENSION_URL_ORGANIZATION_IDENTIFIER = "organization-identifier";
	String EXTENSION_URL_ENDPOINT_IDENTIFIER = "endpoint-identifier";
	String EXTENSION_URL_ERROR_MESSAGE = "error-message";

	String BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL = "timerInterval";
	String BPMN_EXECUTION_VARIABLE_STOP_TIMER = "stopTimer";

	String TIMER_INTERVAL_DEFAULT_VALUE = "PT24H";
}
