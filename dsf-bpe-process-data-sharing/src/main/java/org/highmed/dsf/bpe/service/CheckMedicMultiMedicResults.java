package org.highmed.dsf.bpe.service;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;

public class CheckMedicMultiMedicResults extends CheckResults
{
	public CheckMedicMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected Stream<BiFunction<QueryResult, Task, Boolean>> getChecks(QueryResult result, Task task)
	{
		// TODO: define and implement further result set checks
		return Stream.of(this::checkColumns, this::checkRows);
	}
}
