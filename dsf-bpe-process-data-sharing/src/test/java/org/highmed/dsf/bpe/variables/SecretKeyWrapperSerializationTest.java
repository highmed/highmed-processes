package org.highmed.dsf.bpe.variables;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.highmed.dsf.bpe.variable.SecretKeyWrapper;
import org.highmed.dsf.fhir.json.ObjectMapperFactory;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

public class SecretKeyWrapperSerializationTest
{
	private static final Logger logger = LoggerFactory.getLogger(SecretKeyWrapperSerializationTest.class);

	@Test
	public void testReadWriteJson() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		SecretKeyWrapper key = SecretKeyWrapper.newAes256Key();

		String value = mapper.writeValueAsString(key);

		logger.debug("SecretKeyWrapper: {}", value);

		SecretKeyWrapper readkey = mapper.readValue(value, SecretKeyWrapper.class);

		assertNotNull(readkey);
		assertEquals(key.getAlgorithm(), readkey.getAlgorithm());
		assertArrayEquals(key.getBytes(), readkey.getBytes());
		assertEquals(key.getSecretKey(), readkey.getSecretKey());
	}

	@Test
	public void testReadWriteJsonFromBytes() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		byte[] bytes = AesGcmUtil.generateAES256Key().getEncoded();
		SecretKeyWrapper key = SecretKeyWrapper.fromAes256Key(bytes);
		assertArrayEquals(bytes, key.getBytes());

		String value = mapper.writeValueAsString(key);

		logger.debug("SecretKeyWrapper: {}", value);

		SecretKeyWrapper readkey = mapper.readValue(value, SecretKeyWrapper.class);

		assertNotNull(readkey);
		assertEquals(key.getAlgorithm(), readkey.getAlgorithm());
		assertArrayEquals(key.getBytes(), readkey.getBytes());
		assertEquals(key.getSecretKey(), readkey.getSecretKey());
	}
}
