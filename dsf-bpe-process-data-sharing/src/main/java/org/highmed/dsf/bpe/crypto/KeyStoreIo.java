package org.highmed.dsf.bpe.crypto;

import static org.highmed.dsf.bpe.crypto.KeyStoreHelper.JCEKS;
import static org.highmed.dsf.bpe.crypto.KeyStoreHelper.PKCS12;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreIo
{
	public static KeyStore readJceks(Path file, char[] password)
			throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
	{
		return read(file, password, JCEKS);
	}

	public static KeyStore readPkcs12(Path file, char[] password)
			throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
	{
		return read(file, password, PKCS12);
	}

	private static KeyStore read(Path file, char[] password, String type)
			throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
	{
		try (InputStream stream = Files.newInputStream(file))
		{
			KeyStore keystore = KeyStore.getInstance(type);
			keystore.load(stream, password);

			return keystore;
		}
	}

	public static void write(KeyStore keystore, Path file, char[] password)
			throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException
	{
		try (OutputStream stream = Files.newOutputStream(file))
		{
			keystore.store(stream, password);
		}
	}
}
