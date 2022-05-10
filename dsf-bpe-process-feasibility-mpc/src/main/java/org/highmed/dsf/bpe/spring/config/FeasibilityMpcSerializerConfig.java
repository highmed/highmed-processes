package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.variables.FinalFeasibilityMpcQueryResultSerializer;
import org.highmed.dsf.bpe.variables.FinalFeasibilityMpcQueryResultsSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class FeasibilityMpcSerializerConfig
{
	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public FinalFeasibilityMpcQueryResultSerializer finalFeasibilityMpcQueryResultSerializer()
	{
		return new FinalFeasibilityMpcQueryResultSerializer(objectMapper);
	}

	@Bean
	public FinalFeasibilityMpcQueryResultsSerializer finalFeasibilityMpcQueryResultsSerializer()
	{
		return new FinalFeasibilityMpcQueryResultsSerializer(objectMapper);
	}
}
