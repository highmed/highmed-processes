package org.highmed.dsf.bpe.variable;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class QueryResultValues
{
	public static interface FeasibilityQueryResultValue extends PrimitiveValue<QueryResult>
	{
	}

	private static class FeasibilityQueryResultValueImpl extends PrimitiveTypeValueImpl<QueryResult>
			implements QueryResultValues.FeasibilityQueryResultValue
	{
		private static final long serialVersionUID = 1L;

		public FeasibilityQueryResultValueImpl(QueryResult value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FeasibilityQueryResultValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FeasibilityQueryResultValueTypeImpl()
		{
			super(QueryResult.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new QueryResultValues.FeasibilityQueryResultValueImpl((QueryResult) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new QueryResultValues.FeasibilityQueryResultValueTypeImpl();

	private QueryResultValues()
	{
	}

	public static QueryResultValues.FeasibilityQueryResultValue create(QueryResult value)
	{
		return new QueryResultValues.FeasibilityQueryResultValueImpl(value, VALUE_TYPE);
	}
}
