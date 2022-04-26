package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.FinalFeasibilityMpcQueryResultValues.FinalFeasibilityMpcQueryResultValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FinalFeasibilityMpcQueryResultSerializer
		extends PrimitiveValueSerializer<FinalFeasibilityMpcQueryResultValue> implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public FinalFeasibilityMpcQueryResultSerializer(ObjectMapper objectMapper)
	{
		super(FinalFeasibilityMpcQueryResultValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(FinalFeasibilityMpcQueryResultValue value, ValueFields valueFields)
	{
		FinalFeasibilityMpcQueryResult result = value.getValue();
		try
		{
			if (result != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(result));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public FinalFeasibilityMpcQueryResultValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FinalFeasibilityMpcQueryResultValues.create((FinalFeasibilityMpcQueryResult) untypedValue.getValue());
	}

	@Override
	public FinalFeasibilityMpcQueryResultValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			FinalFeasibilityMpcQueryResult result = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, FinalFeasibilityMpcQueryResult.class);
			return FinalFeasibilityMpcQueryResultValues.create(result);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
