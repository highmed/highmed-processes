package org.highmed.dsf.bpe.service;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckMedicMultiMedicResultSets extends CheckResultSets
{
	private static final Logger logger = LoggerFactory.getLogger(CheckMedicMultiMedicResultSets.class);

	public CheckMedicMultiMedicResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected Stream<BiFunction<QueryResult, Task, Boolean>> getChecks(QueryResult result, Task task)
	{
		// TODO: define and implement further result set checks
		return Stream.of(this::checkColumns, this::checkRows);
	}
}
