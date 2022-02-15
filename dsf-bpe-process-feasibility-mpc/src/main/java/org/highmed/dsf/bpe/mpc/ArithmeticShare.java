package org.highmed.dsf.bpe.mpc;

import static org.highmed.dsf.bpe.mpc.ArithmeticSharing.DEFAULT_RING_SIZE;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author Tobias Kussel
 */
public class ArithmeticShare implements Serializable
{
	private final BigInteger value;
	private final BigInteger ringSize;

	public ArithmeticShare(BigInteger value)
	{
		this(value, DEFAULT_RING_SIZE);
	}

	public ArithmeticShare(BigInteger value, BigInteger ringSize)
	{
		this.value = value;
		this.ringSize = ringSize;
	}

	public BigInteger getValue()
	{
		return value;
	}

	public BigInteger getRingSize()
	{
		return ringSize;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof ArithmeticShare))
			return false;
		ArithmeticShare as = (ArithmeticShare) o;
		return as.value.equals(value) && as.ringSize.equals(ringSize);
	}

	@Override
	public int hashCode()
	{
		int result = value.hashCode();
		result = 31 * result + ringSize.hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return value + " mod " + ringSize;
	}
}
