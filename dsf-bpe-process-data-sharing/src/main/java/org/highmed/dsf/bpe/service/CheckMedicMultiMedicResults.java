package org.highmed.dsf.bpe.service;

import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;

public class CheckMedicMultiMedicResults extends AbstractCheckResults
{
	public CheckMedicMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected boolean testResultAndAddPossibleError(QueryResult result, Task task)
	{
		return super.testResultAndAddPossibleError(result, task); // TODO: define further checks
	}
}
