package org.highmed.dsf.bpe.variable;

import java.io.IOException;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variable.BloomFilterConfigValues.BloomFilterConfigValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecretKeySerializer extends PrimitiveValueSerializer<SecretKeyValues.SecretKeyValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public SecretKeySerializer(ObjectMapper objectMapper)
	{
		super(SecretKeyValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(SecretKeyValues.SecretKeyValue value, ValueFields valueFields)
	{
		SecretKey target = value.getValue();
		try
		{
			if (target != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(target));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public SecretKeyValues.SecretKeyValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return SecretKeyValues.create((SecretKey) untypedValue.getValue());
	}

	@Override
	public SecretKeyValues.SecretKeyValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			SecretKey target = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, SecretKey.class);
			return SecretKeyValues.create(target);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
