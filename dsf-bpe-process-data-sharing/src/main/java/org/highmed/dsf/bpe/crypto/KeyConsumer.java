package org.highmed.dsf.bpe.crypto;

import java.security.Key;

public interface KeyConsumer
{
	void store(String alias, Key key);
}
