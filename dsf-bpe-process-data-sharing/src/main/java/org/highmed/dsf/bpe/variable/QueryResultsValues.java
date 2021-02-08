package org.highmed.dsf.bpe.variable;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class QueryResultsValues
{
	public static interface FeasibilityQueryResultsValue extends PrimitiveValue<QueryResults>
	{
	}

	private static class FeasibilityQueryResultsValueImpl extends PrimitiveTypeValueImpl<QueryResults>
			implements QueryResultsValues.FeasibilityQueryResultsValue
	{
		private static final long serialVersionUID = 1L;

		public FeasibilityQueryResultsValueImpl(QueryResults value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FeasibilityQueryResultsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FeasibilityQueryResultsValueTypeImpl()
		{
			super(QueryResults.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new QueryResultsValues.FeasibilityQueryResultsValueImpl((QueryResults) value,
					VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new QueryResultsValues.FeasibilityQueryResultsValueTypeImpl();

	private QueryResultsValues()
	{
	}

	public static QueryResultsValues.FeasibilityQueryResultsValue create(QueryResults value)
	{
		return new QueryResultsValues.FeasibilityQueryResultsValueImpl(value, VALUE_TYPE);
	}
}
