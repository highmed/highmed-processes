package org.highmed.dsf.bpe.variables;

import static org.junit.Assert.*;

import org.highmed.dsf.bpe.variable.SecretKeyWrapper;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SecretKeyWrapperTest
{
	private static final Logger logger = LoggerFactory.getLogger(SecretKeyWrapperTest.class);

	@Test
	public void testReadWriteJson() throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();

		SecretKeyWrapper key = SecretKeyWrapper.newAes256Key();

		String value = objectMapper.writeValueAsString(key);

		logger.debug("SecretKeyWrapper: {}", value);

		SecretKeyWrapper readkey = objectMapper.readValue(value, SecretKeyWrapper.class);

		assertNotNull(readkey);
		assertEquals(key.getAlgorithm(), readkey.getAlgorithm());
		assertArrayEquals(key.getBytes(), readkey.getBytes());
		assertEquals(key.getSecretKey(), readkey.getSecretKey());
	}

	@Test
	public void testReadWriteJsonFromBytes() throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();

		byte[] bytes = AesGcmUtil.generateAES256Key().getEncoded();
		SecretKeyWrapper key = SecretKeyWrapper.fromAes256Key(bytes);
		assertArrayEquals(bytes, key.getBytes());

		String value = objectMapper.writeValueAsString(key);

		logger.debug("SecretKeyWrapper: {}", value);

		SecretKeyWrapper readkey = objectMapper.readValue(value, SecretKeyWrapper.class);

		assertNotNull(readkey);
		assertEquals(key.getAlgorithm(), readkey.getAlgorithm());
		assertArrayEquals(key.getBytes(), readkey.getBytes());
		assertEquals(key.getSecretKey(), readkey.getSecretKey());
	}
}
