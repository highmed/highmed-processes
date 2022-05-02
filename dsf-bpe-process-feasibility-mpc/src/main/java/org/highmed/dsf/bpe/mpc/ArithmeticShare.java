package org.highmed.dsf.bpe.mpc;

import static org.highmed.dsf.bpe.mpc.ArithmeticSharing.DEFAULT_RING_SIZE;

import java.math.BigInteger;

/**
 * @author Tobias Kussel
 */
public class ArithmeticShare
{
	private final BigInteger value;
	private final BigInteger ringSize;

	public ArithmeticShare(long value)
	{
		this(BigInteger.valueOf(value), DEFAULT_RING_SIZE);
	}

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
	public String toString()
	{
		return value + " mod " + ringSize;
	}
}
