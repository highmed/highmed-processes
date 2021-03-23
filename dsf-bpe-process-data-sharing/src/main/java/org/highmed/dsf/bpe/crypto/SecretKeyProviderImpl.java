package org.highmed.dsf.bpe.crypto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.util.Objects;

import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SecretKeyProviderImpl implements KeyProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SecretKeyProviderImpl.class);

	private final OrganizationProvider organizationProvider;

	private final Path keystoreFile;
	private final char[] keystorePassword;

	private KeyStore keystore;

	public SecretKeyProviderImpl(OrganizationProvider organizationProvider, Path keystoreFile, char[] keystorePassword)
	{
		this.keystoreFile = keystoreFile;
		this.keystorePassword = keystorePassword;

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(keystoreFile, "keystoreFile");
		Objects.requireNonNull(keystorePassword, "keystorePassword");

		Objects.requireNonNull(organizationProvider, "organizationProvider");

		try
		{
			keystore = KeyStoreIo.readJceks(keystoreFile, keystorePassword);
		}
		catch (FileNotFoundException | NoSuchFileException e)
		{
			logger.warn("Could not find keystore at {}, creating a new keystore containing a new organization key",
					keystoreFile);

			keystore = KeyStoreHelper.createJceks(keystorePassword);
			keystore.setKeyEntry(organizationProvider.getLocalIdentifierValue(), AesGcmUtil.generateAES256Key(),
					keystorePassword, null);
			KeyStoreIo.write(keystore, keystoreFile, keystorePassword);
		}

		if (!Files.isReadable(keystoreFile))
			throw new IOException("Organization keystore at " + keystoreFile.toString() + " not readable");
	}

	@Override
	public Key get(String alias)
	{
		try
		{
			return keystore.getKey(alias, keystorePassword);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not get SecretKey", e);
		}
	}
}
