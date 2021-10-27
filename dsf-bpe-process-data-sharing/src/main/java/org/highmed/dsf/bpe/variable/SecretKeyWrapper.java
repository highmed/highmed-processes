package org.highmed.dsf.bpe.variable;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SecretKeyWrapper
{
	private static final Logger logger = LoggerFactory.getLogger(SecretKeyWrapper.class);
	private static final String AES_ALGORITHM = "AES";

	public static SecretKeyWrapper newAes256Key()
	{
		try
		{
			return new SecretKeyWrapper(AesGcmUtil.generateAES256Key().getEncoded(), AES_ALGORITHM);
		}
		catch (NoSuchAlgorithmException exception)
		{
			logger.warn("Could not create new AES256 key: {}", exception.getMessage());
			throw new RuntimeException(exception);
		}
	}

	public static SecretKeyWrapper fromAes256Key(byte[] key)
	{
		byte[] bytes = new byte[key.length];
		System.arraycopy(key, 0, bytes, 0, bytes.length);

		return new SecretKeyWrapper(bytes, AES_ALGORITHM);
	}

	private final byte[] bytes;
	private final String algorithm;

	@JsonCreator
	public SecretKeyWrapper(@JsonProperty("bytes") byte[] bytes, @JsonProperty("algorithm") String algorithm)
	{
		this.bytes = bytes;
		this.algorithm = algorithm;
	}

	@JsonIgnore
	public SecretKey getSecretKey()
	{
		return new SecretKeySpec(bytes, algorithm);
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

	public byte[] getBytes()
	{
		byte[] key = new byte[bytes.length];
		System.arraycopy(bytes, 0, key, 0, key.length);

		return key;
	}
}
