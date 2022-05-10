package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FinalFeasibilityMpcQueryResultsValues
{
	public static interface FinalFeasibilityMpcQueryResultsValue extends PrimitiveValue<FinalFeasibilityMpcQueryResults>
	{
	}

	private static class FinalFeasibilityMpcQueryResultsValueImpl
			extends PrimitiveTypeValueImpl<FinalFeasibilityMpcQueryResults>
			implements FinalFeasibilityMpcQueryResultsValues.FinalFeasibilityMpcQueryResultsValue
	{
		private static final long serialVersionUID = 1L;

		public FinalFeasibilityMpcQueryResultsValueImpl(FinalFeasibilityMpcQueryResults value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FinalFeasibilityMpcQueryResultsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FinalFeasibilityMpcQueryResultsValueTypeImpl()
		{
			super(FinalFeasibilityMpcQueryResults.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FinalFeasibilityMpcQueryResultsValues.FinalFeasibilityMpcQueryResultsValueImpl(
					(FinalFeasibilityMpcQueryResults) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FinalFeasibilityMpcQueryResultsValues.FinalFeasibilityMpcQueryResultsValueTypeImpl();

	private FinalFeasibilityMpcQueryResultsValues()
	{
	}

	public static FinalFeasibilityMpcQueryResultsValues.FinalFeasibilityMpcQueryResultsValue create(
			FinalFeasibilityMpcQueryResults value)
	{
		return new FinalFeasibilityMpcQueryResultsValues.FinalFeasibilityMpcQueryResultsValueImpl(value, VALUE_TYPE);
	}
}
