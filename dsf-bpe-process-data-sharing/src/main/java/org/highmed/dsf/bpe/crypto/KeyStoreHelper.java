package org.highmed.dsf.bpe.crypto;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreHelper
{
	public static final String JCEKS = "JCEKS";
	public static final String PKCS12 = "PKCS12";

	public static KeyStore createJceks(char[] password)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		return create(password, JCEKS);
	}

	public static KeyStore createPkcs12(char[] password)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		return create(password, PKCS12);
	}

	private static KeyStore create(char[] password, String type)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		KeyStore keyStore = KeyStore.getInstance(type);
		keyStore.load(null, password);

		return keyStore;
	}
}
