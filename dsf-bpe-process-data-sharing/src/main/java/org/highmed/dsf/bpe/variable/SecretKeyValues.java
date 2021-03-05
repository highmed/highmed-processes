package org.highmed.dsf.bpe.variable;

import java.util.Map;

import javax.crypto.SecretKey;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class SecretKeyValues
{
	public static interface SecretKeyValue extends PrimitiveValue<SecretKey>
	{
	}

	private static class SecretKeyValueImpl extends PrimitiveTypeValueImpl<SecretKey> implements SecretKeyValue
	{
		private static final long serialVersionUID = 1L;

		public SecretKeyValueImpl(SecretKey value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class SecretKeyValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private SecretKeyValueTypeImpl()
		{
			super(SecretKey.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new SecretKeyValueImpl((SecretKey) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new SecretKeyValueTypeImpl();

	private SecretKeyValues()
	{
	}

	public static SecretKeyValue create(SecretKey value)
	{
		return new SecretKeyValueImpl(value, VALUE_TYPE);
	}
}
