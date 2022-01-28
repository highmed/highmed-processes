package de.tu_darmstadt.cbs.secretshare;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Objects;

import org.springframework.beans.factory.InitializingBean;

/**
 * This class implements arithmetic sharing
 *
 * @author Tobias Kussel
 */
public class ArithmeticSharing implements InitializingBean
{
	/**
	 * Convert a BigDecimal to a fixed point representation
	 *
	 * @param value
	 *            Value as (unscaled) BigDecimal
	 * @param fractionalBits
	 *            number of bits for fixed point scaling. Must be positive
	 * @return Fixed point represented value
	 * @throws IllegalArgumentException
	 *             Negative fractionalBits
	 */
	public static BigInteger convertToFixedPoint(BigDecimal value, int fractionalBits) throws IllegalArgumentException
	{
		if (fractionalBits < 0)
			throw new IllegalArgumentException("FractionalBits must be positive");
		final BigDecimal scaleFactor = BigDecimal.valueOf(2).pow(fractionalBits);
		return value.multiply(scaleFactor).toBigInteger();
	}

	/**
	 * Reconstruct secret from shares
	 *
	 * @param shares
	 *            Array of all arithmetic shares
	 * @return Clear text BigInteger
	 * @throws IllegalArgumentException
	 *             Incompatible primes
	 */
	public static BigInteger reconstruct(ArithmeticShare[] shares) throws IllegalArgumentException
	{
		BigInteger reconstruction = BigInteger.ZERO;
		BigInteger first_prime = shares[0].prime;
		for (int i = 0; i != shares.length; i++)
		{
			if (!(shares[i].prime.equals(first_prime)))
			{
				throw new IllegalArgumentException("Incompatible primes found!");
			}
			reconstruction = reconstruction.add(shares[i].value).remainder(shares[i].prime);
		}
		return reconstruction;
	}

	/**
	 * Reconstruct secret from shares
	 *
	 * @param shares
	 *            Array of all arithmetic shares (containing decimal values)
	 * @param fractionalBits
	 *            Number of bits for fractional part during construction of shares
	 * @return Clear text BigDecimal
	 * @throws IllegalArgumentException
	 *             Incompatible primes
	 */
	public static BigDecimal reconstruct(ArithmeticShare[] shares, int fractionalBits) throws IllegalArgumentException
	{
		final BigDecimal scaleFactor = BigDecimal.valueOf(2).pow(fractionalBits);
		BigDecimal result = new BigDecimal(reconstruct(shares));
		return result.divide(scaleFactor);
	}

	/**
	 * Reconstruct secret from shares
	 *
	 * @param shares
	 *            Array of all arithmetic shares (containing decimal values)
	 * @param fractionalBits
	 *            Number of bits for fractional part during construction of shares
	 * @param roundingMode
	 *            Rounding mode for final rescaling
	 * @return Clear text BigDecimal
	 * @throws IllegalArgumentException
	 *             Incompatible primes
	 */
	public static BigDecimal reconstruct(ArithmeticShare[] shares, int fractionalBits, RoundingMode roundingMode)
			throws IllegalArgumentException
	{
		final BigDecimal scaleFactor = BigDecimal.valueOf(2).pow(fractionalBits);
		BigDecimal result = new BigDecimal(reconstruct(shares));
		return result.divide(scaleFactor, roundingMode);
	}

	/**
	 * RNG
	 */
	private final SecureRandom randomGenerator = new SecureRandom();

	/**
	 * Prime
	 */
	private final BigInteger prime;

	/**
	 * Number of parties
	 */
	private final int numParties;

	/**
	 * Creates a new instance
	 *
	 * @param numParties
	 */
	public ArithmeticSharing(int numParties)
	{
		this(numParties, BigInteger.valueOf(2).pow(127).subtract(BigInteger.ONE));
	}

	/**
	 * Creates a new instance
	 *
	 * @param numParties
	 * @param prime
	 */
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

	/**
	 * Share a secret
	 *
	 * @param secret
	 *            Secret BigDecimal value to share
	 * @param fractionalBits
	 *            number of bits for fixed point scaling. Must be positive
	 * @return Array of arithmetic shares
	 * @throws IllegalArgumentException
	 *             Negative fractionalBits
	 */
	public ArithmeticShare[] share(BigDecimal secret, int fractionalBits) throws IllegalArgumentException
	{
		return share(ArithmeticSharing.convertToFixedPoint(secret, fractionalBits));
	}

	/**
	 * Share a secret
	 *
	 * @param secret
	 *            BigInteger secret value to share
	 * @return Array of arithmetic shares
	 */
	public ArithmeticShare[] share(BigInteger secret)
	{
		BigInteger[] shares = new BigInteger[numParties];
		shares[numParties - 1] = secret;
		for (int i = 0; i != numParties - 1; i++)
		{
			shares[i] = getSignedBlind(127);
			shares[numParties - 1] = shares[numParties - 1].add(prime).subtract(shares[i]);
		}
		ArithmeticShare[] result = new ArithmeticShare[numParties];
		for (int i = 0; i != numParties; i++)
		{
			result[i] = new ArithmeticShare(shares[i], prime);
		}
		return result;
	}

	/**
	 * Share a secret
	 *
	 * @param secret
	 *            Secret double value to share
	 * @param fractionalBits
	 *            number of bits for fixed point scaling. Must be positive
	 * @return Array of arithmetic shares
	 * @throws IllegalArgumentException
	 *             Negative fractionalBits
	 */
	public ArithmeticShare[] share(double secret, int fractionalBits) throws IllegalArgumentException
	{
		return share(BigDecimal.valueOf(secret), fractionalBits);
	}

	/**
	 * Share a secret
	 *
	 * @param secret
	 *            Secret integral value to share
	 * @return Array of arithmetic shares
	 */
	public ArithmeticShare[] share(int secret)
	{
		return share(BigInteger.valueOf(secret));
	}

	/**
	 * Generate a signed blind
	 *
	 * @param bitlength
	 *            Bitlength *including* sign bit
	 * @return BigInteger
	 * @throws IllegalArgumentException
	 *             Too small or negative bitlength
	 */
	private BigInteger getSignedBlind(int bitlength) throws IllegalArgumentException
	{
		if (bitlength < 2)
			throw new IllegalArgumentException("Bitlength must be larger than 2");
		BigInteger value = new BigInteger(bitlength - 1, randomGenerator);
		byte[] randomByte = new byte[1];
		randomGenerator.nextBytes(randomByte);
		int signum = Byte.valueOf(randomByte[0]).intValue() & 0x01;
		if (signum == 1)
			value = value.negate();
		return value;
	}
}