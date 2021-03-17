package org.highmed.dsf.bpe.crypto;

import java.security.Key;

public interface KeyProvider
{
	Key get(String alias);
}
