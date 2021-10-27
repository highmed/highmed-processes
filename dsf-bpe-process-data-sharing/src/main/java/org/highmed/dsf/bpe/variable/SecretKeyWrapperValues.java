package org.highmed.dsf.bpe.variable;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class SecretKeyWrapperValues
{
	public static interface SecretKeyWrapperValue extends PrimitiveValue<SecretKeyWrapper>
	{
	}

	private static class SecretKeyWrapperValueImpl extends PrimitiveTypeValueImpl<SecretKeyWrapper>
			implements SecretKeyWrapperValues.SecretKeyWrapperValue
	{
		private static final long serialVersionUID = 1L;

		public SecretKeyWrapperValueImpl(SecretKeyWrapper value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class SecretKeyWrapperValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private SecretKeyWrapperValueTypeImpl()
		{
			super(SecretKeyWrapper.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new SecretKeyWrapperValues.SecretKeyWrapperValueImpl((SecretKeyWrapper) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new SecretKeyWrapperValues.SecretKeyWrapperValueTypeImpl();

	private SecretKeyWrapperValues()
	{
	}

	public static SecretKeyWrapperValues.SecretKeyWrapperValue create(SecretKeyWrapper value)
	{
		return new SecretKeyWrapperValues.SecretKeyWrapperValueImpl(value, VALUE_TYPE);
	}
}
