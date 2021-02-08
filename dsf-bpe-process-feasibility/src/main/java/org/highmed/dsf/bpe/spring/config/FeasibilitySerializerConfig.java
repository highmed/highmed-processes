package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultSerializer;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultsSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class FeasibilitySerializerConfig
{
	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public FinalFeasibilityQueryResultSerializer finalFeasibilityQueryResultSerializer()
	{
		return new FinalFeasibilityQueryResultSerializer(objectMapper);
	}

	@Bean
	public FinalFeasibilityQueryResultsSerializer finalFeasibilityQueryResultsSerializer()
	{
		return new FinalFeasibilityQueryResultsSerializer(objectMapper);
	}
}
