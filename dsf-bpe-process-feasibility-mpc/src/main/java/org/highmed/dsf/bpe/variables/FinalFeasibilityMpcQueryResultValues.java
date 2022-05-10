package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FinalFeasibilityMpcQueryResultValues
{
	public static interface FinalFeasibilityMpcQueryResultValue extends PrimitiveValue<FinalFeasibilityMpcQueryResult>
	{
	}

	private static class FinalFeasibilityMpcQueryResultValueImpl
			extends PrimitiveTypeValueImpl<FinalFeasibilityMpcQueryResult>
			implements FinalFeasibilityMpcQueryResultValues.FinalFeasibilityMpcQueryResultValue
	{
		private static final long serialVersionUID = 1L;

		public FinalFeasibilityMpcQueryResultValueImpl(FinalFeasibilityMpcQueryResult value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FinalFeasibilityMpcQueryResultValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FinalFeasibilityMpcQueryResultValueTypeImpl()
		{
			super(FinalFeasibilityMpcQueryResult.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FinalFeasibilityMpcQueryResultValues.FinalFeasibilityMpcQueryResultValueImpl(
					(FinalFeasibilityMpcQueryResult) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FinalFeasibilityMpcQueryResultValues.FinalFeasibilityMpcQueryResultValueTypeImpl();

	private FinalFeasibilityMpcQueryResultValues()
	{
	}

	public static FinalFeasibilityMpcQueryResultValues.FinalFeasibilityMpcQueryResultValue create(
			FinalFeasibilityMpcQueryResult value)
	{
		return new FinalFeasibilityMpcQueryResultValues.FinalFeasibilityMpcQueryResultValueImpl(value, VALUE_TYPE);
	}
}
