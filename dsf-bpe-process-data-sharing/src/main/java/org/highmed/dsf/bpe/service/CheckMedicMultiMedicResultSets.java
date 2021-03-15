package org.highmed.dsf.bpe.service;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;

public class CheckMedicMultiMedicResultSets extends CheckResultSets
{
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
