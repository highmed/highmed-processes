package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.variable.BloomFilterConfigSerializer;
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
}
