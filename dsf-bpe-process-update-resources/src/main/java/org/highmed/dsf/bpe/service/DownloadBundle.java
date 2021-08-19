package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsUpdateResources.BPMN_EXECUTION_VARIABLE_BUNDLE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadBundle extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadBundle.class);

	public DownloadBundle(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables();
		IdType bundleId = getBundleId(task);
		FhirWebserviceClient requesterClient = getFhirWebserviceClientProvider()
				.getWebserviceClient(bundleId.getBaseUrl());

		Bundle bundle;
		try
		{
			if (bundleId.hasVersionIdPart())
				bundle = requesterClient.read(Bundle.class, bundleId.getIdPart(), bundleId.getVersionIdPart());
			else
				bundle = requesterClient.read(Bundle.class, bundleId.getIdPart());
		}
		catch (WebApplicationException e)
		{
			logger.error("Error while reading Bundle with id {} from organization {}: {}", bundleId.getValue(),
					task.getRequester().getReference(), e.getMessage());
			throw new RuntimeException("Error while reading Bundle with id " + bundleId.getValue()
					+ " from organization " + task.getRequester().getReference() + ", " + e.getMessage(), e);
		}

		if (!EnumSet.of(BundleType.TRANSACTION, BundleType.BATCH).contains(bundle.getType()))
		{
			logger.error("Bundle type TRANSACTION or BATCH expected, but got {}", bundle.getType());
			throw new RuntimeException("Bundle type TRANSACTION or BATCH expected, but got " + bundle.getType());
		}

		execution.setVariable(BPMN_EXECUTION_VARIABLE_BUNDLE, FhirResourceValues.create(bundle));
	}

	private IdType getBundleId(Task task)
	{
		List<Reference> bundleReferences = getTaskHelper().getInputParameterReferenceValues(task,
				CODESYSTEM_HIGHMED_UPDATE_RESOURCE, CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE)
				.collect(Collectors.toList());

		if (bundleReferences.size() != 1)
		{
			logger.error("Task input parameter {} contains unexpected number of Bundle IDs, expected 1, got {}",
					CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE, bundleReferences.size());
			throw new RuntimeException(
					"Task input parameter " + CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE
							+ " contains unexpected number of Bundle IDs, expected 1, got " + bundleReferences.size());
		}
		else if (!bundleReferences.get(0).hasReference()
				|| !bundleReferences.get(0).getReference().contains("/Bundle/"))
		{
			logger.error("Task input parameter {} has no Bundle reference",
					CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE);
			throw new RuntimeException("Task input parameter "
					+ CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE + " has no Bundle reference");
		}

		return new IdType(bundleReferences.get(0).getReference());
	}
}
