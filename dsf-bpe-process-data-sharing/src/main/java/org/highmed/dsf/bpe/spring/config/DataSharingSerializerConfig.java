package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.variable.BloomFilterConfigSerializer;
import org.highmed.dsf.bpe.variable.QueryResultSerializer;
import org.highmed.dsf.bpe.variable.QueryResultsSerializer;
import org.highmed.dsf.bpe.variable.SecretKeySerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DataSharingSerializerConfig
{
	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public BloomFilterConfigSerializer bloomFilterConfigSerializer()
	{
		return new BloomFilterConfigSerializer(objectMapper);
	}

	@Bean
	public SecretKeySerializer secretKeySerializer()
	{
		return new SecretKeySerializer(objectMapper);
	}

	@Bean
	public QueryResultSerializer queryResultSerializer()
	{
		return new QueryResultSerializer(objectMapper);
	}

	@Bean
	public QueryResultsSerializer queryResultsSerializer()
	{
		return new QueryResultsSerializer(objectMapper);
	}
}
