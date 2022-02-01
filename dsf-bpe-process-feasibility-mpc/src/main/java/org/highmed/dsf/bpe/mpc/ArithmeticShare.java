package org.highmed.dsf.bpe.mpc;

import static org.highmed.dsf.bpe.mpc.ArithmeticSharing.DEFAULT_PRIME;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Tobias Kussel
 */
public class ArithmeticShare implements Serializable
{
	private final BigInteger value;
	private final BigInteger prime;

	public ArithmeticShare(BigInteger value)
	{
		this(value, DEFAULT_PRIME);
	}

	@JsonCreator
	public ArithmeticShare(@JsonProperty("value") BigInteger value, @JsonProperty("prime") BigInteger prime)
	{
		this.value = value;
		this.prime = prime;
	}

	public BigInteger getValue()
	{
		return value;
	}

	public BigInteger getPrime()
	{
		return prime;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof ArithmeticShare))
			return false;
		ArithmeticShare as = (ArithmeticShare) o;
		return as.value.equals(value) && as.prime.equals(prime);
	}

	@Override
	public int hashCode()
	{
		int result = value.hashCode();
		result = 31 * result + prime.hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return value + " mod " + prime;
	}
}