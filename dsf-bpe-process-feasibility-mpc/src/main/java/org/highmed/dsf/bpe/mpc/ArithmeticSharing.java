package org.highmed.dsf.bpe.mpc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author Tobias Kussel
 */
public class ArithmeticSharing implements InitializingBean
{
	private static final int MAX_POWER_FOR_INT = 31;
	public static final BigInteger DEFAULT_PRIME = BigInteger.valueOf(2).pow(MAX_POWER_FOR_INT)
			.subtract(BigInteger.ONE);

	private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();

	private final BigInteger prime;
	private final int numParties;

	public ArithmeticSharing(int numParties)
	{
		this(numParties, DEFAULT_PRIME);
	}

	public ArithmeticSharing(int numParties, BigInteger prime)
	{
		this.prime = prime;
		this.numParties = numParties;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(prime, "prime");

		if (BigInteger.ZERO.compareTo(prime) >= 0)
		{
			throw new IllegalArgumentException("prime < 1");
		}

		if (numParties <= 0)
		{
			throw new IllegalArgumentException("numParties < 1");
		}
	}

	public int getNumParties()
	{
		return numParties;
	}

	public BigInteger getPrime()
	{
		return prime;
	}

	public List<ArithmeticShare> createShares(double secret, int fractionalBits)
	{
		return createShares(BigDecimal.valueOf(secret), fractionalBits);
	}

	public List<ArithmeticShare> createShares(BigDecimal secret, int fractionalBits)
	{
		return createShares(convertToFixedPoint(secret, fractionalBits));
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
			shares[i] = getSignedBlind(MAX_POWER_FOR_INT);
			shares[numParties - 1] = shares[numParties - 1].subtract(shares[i]);
		}

		shares[numParties - 1] = shares[numParties - 1].mod(prime);

		ArithmeticShare[] result = new ArithmeticShare[numParties];
		for (int i = 0; i != numParties; i++)
		{
			result[i] = new ArithmeticShare(shares[i], prime);
		}

		return Arrays.asList(result);
	}

	public double reconstructToDouble(List<ArithmeticShare> shares, int fractionalBits, RoundingMode roundingMode)
	{
		return reconstructToBigDecimal(shares, fractionalBits, roundingMode).doubleValue();
	}

	public BigDecimal reconstructToBigDecimal(List<ArithmeticShare> shares, int fractionalBits,
			RoundingMode roundingMode)
	{
		BigDecimal scaleFactor = BigDecimal.valueOf(2).pow(fractionalBits);
		BigDecimal result = new BigDecimal(reconstructSecret(shares));

		return result.divide(scaleFactor, roundingMode);
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
		BigInteger first_prime = sharesArray[0].getPrime();
		for (int i = 0; i != sharesArray.length; i++)
		{
			if (!(sharesArray[i].getPrime().equals(first_prime)))
			{
				throw new IllegalArgumentException("Incompatible primes found!");
			}
			reconstruction = reconstruction.add(sharesArray[i].getValue()).remainder(sharesArray[i].getPrime());
		}

		return reconstruction;
	}

	private BigInteger getSignedBlind(int bitlength) throws IllegalArgumentException
	{
		if (bitlength < 2)
			throw new IllegalArgumentException("Bitlength must be larger than 2");

		BigInteger value = new BigInteger(bitlength - 1, RANDOM_GENERATOR);
		byte[] randomByte = new byte[1];
		RANDOM_GENERATOR.nextBytes(randomByte);
		int signum = Byte.valueOf(randomByte[0]).intValue() & 0x01;
		if (signum == 1)
			value = value.negate();

		return value;
	}

	private BigInteger convertToFixedPoint(BigDecimal value, int fractionalBits)
	{
		if (fractionalBits < 0)
			throw new IllegalArgumentException("FractionalBits must be positive");

		BigDecimal scaleFactor = BigDecimal.valueOf(2).pow(fractionalBits);

		return value.multiply(scaleFactor).toBigInteger();
	}
}