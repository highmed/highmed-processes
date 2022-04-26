package org.highmed.dsf.bpe.mpc;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Tobias Kussel
 */
public class ArithmeticSharing
{
	private static final int MAX_POWER_FOR_INT = 31;
	public static final BigInteger DEFAULT_RING_SIZE = BigInteger.valueOf(2).pow(MAX_POWER_FOR_INT)
			.subtract(BigInteger.ONE);

	private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();

	private final BigInteger ringSize;
	private final int numParties;

	public ArithmeticSharing(int numParties)
	{
		this(numParties, DEFAULT_RING_SIZE);
	}

	public ArithmeticSharing(int numParties, BigInteger ringSize)
	{
		Objects.requireNonNull(ringSize, "ringSize");

		if (BigInteger.ZERO.compareTo(ringSize) >= 0)
			throw new IllegalArgumentException("ringSize < 1");

		if (numParties < 1)
			throw new IllegalArgumentException("numParties < 1");

		this.numParties = numParties;
		this.ringSize = ringSize;
	}

	public int getNumParties()
	{
		return numParties;
	}

	public BigInteger getRingSize()
	{
		return ringSize;
	}

	public List<ArithmeticShare> createShares(int secret)
	{
		return createShares(BigInteger.valueOf(secret));
	}

	public List<ArithmeticShare> createShares(BigInteger secret)
	{
		BigInteger[] shares = new BigInteger[numParties];
		shares[numParties - 1] = secret;

		for (int i = 0; i != numParties - 1; i++)
		{
			shares[i] = getBlind(MAX_POWER_FOR_INT);
			shares[numParties - 1] = shares[numParties - 1].subtract(shares[i]);
		}

		shares[numParties - 1] = shares[numParties - 1].mod(ringSize);

		ArithmeticShare[] result = new ArithmeticShare[numParties];
		for (int i = 0; i != numParties; i++)
		{
			result[i] = new ArithmeticShare(shares[i], ringSize);
		}

		return Arrays.asList(result);
	}

	public int reconstructSecretToInt(List<ArithmeticShare> shares)
	{
		return reconstructSecret(shares).intValueExact();
	}

	public BigInteger reconstructSecret(List<ArithmeticShare> shares)
	{
		if (shares.size() != numParties)
			throw new IllegalArgumentException("Number of provided ArithmeticShares does not match numParties, shares="
					+ shares.size() + " ,numParties=" + numParties);

		ArithmeticShare[] sharesArray = shares.toArray(new ArithmeticShare[0]);

		BigInteger reconstruction = BigInteger.ZERO;
		BigInteger firstRingSize = sharesArray[0].getRingSize();
		for (int i = 0; i != sharesArray.length; i++)
		{
			if (!(sharesArray[i].getRingSize().equals(firstRingSize)))
			{
				throw new IllegalArgumentException("Incompatible ringSizes found!");
			}
			reconstruction = reconstruction.add(sharesArray[i].getValue()).remainder(sharesArray[i].getRingSize());
		}

		return reconstruction;
	}

	private BigInteger getBlind(int bitlength) throws IllegalArgumentException
	{
		if (bitlength < 2)
			throw new IllegalArgumentException("Bitlength must be larger than 2");

		return new BigInteger(bitlength, RANDOM_GENERATOR);
	}
}