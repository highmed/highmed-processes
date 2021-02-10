package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.Column;
import org.highmed.pseudonymization.openehr.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ProvideLocalPseudonyms extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ProvideLocalPseudonyms.class);

	private final String ehrIdColumnPath;

	public ProvideLocalPseudonyms(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			String ehrIdColumnPath)
	{
		super(clientProvider, taskHelper);
		this.ehrIdColumnPath = ehrIdColumnPath;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		logger.info(this.getClass().getName() + " doExecute called");
	}

	private int getEhrColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isEhrIdColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private Predicate<? super Column> isEhrIdColumn()
	{
		return column -> Constants.EHRID_COLUMN_NAME.equals(column.getName())
				&& ehrIdColumnPath.equals(column.getPath());
	}
}
