package org.highmed.dsf.bpe.mpc;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.highmed.dsf.bpe.variable.QueryResult;
import org.junit.Test;

public class ArithmeticSharingTest
{
	@Test(expected = IllegalStateException.class)
	public void testMaxSecret()
	{
		int numParties = 3;
		int secret = 1000000000;
		int maxSecret = new ArithmeticSharing(numParties).getRingSize().divide(BigInteger.valueOf(numParties))
				.intValueExact();

		if (secret > maxSecret)
		{
			throw new IllegalStateException("Secret to big for numParties");
		}
	}

	@Test
	public void arithmeticSharingValid()
	{
		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(3);

		List<ArithmeticShare> singleMedicSharesOrg1 = arithmeticSharing.createShares(10);
		List<ArithmeticShare> singleMedicSharesOrg2 = arithmeticSharing.createShares(20);
		List<ArithmeticShare> singleMedicSharesOrg3 = arithmeticSharing.createShares(30);

		List<QueryResult> queryResultsSingleMedicSharesOrg1 = singleMedicSharesOrg1.stream()
				.map(s -> QueryResult.mpcCountResult("", "", s.getValue().intValueExact()))
				.collect(Collectors.toList());
		List<QueryResult> queryResultsSingleMedicSharesOrg2 = singleMedicSharesOrg2.stream()
				.map(s -> QueryResult.mpcCountResult("", "", s.getValue().intValueExact()))
				.collect(Collectors.toList());
		List<QueryResult> queryResultsSingleMedicSharesOrg3 = singleMedicSharesOrg3.stream()
				.map(s -> QueryResult.mpcCountResult("", "", s.getValue().intValueExact()))
				.collect(Collectors.toList());

		List<ArithmeticShare> reconstrucedSingleMedicSharesOrg1 = queryResultsSingleMedicSharesOrg1.stream()
				.map(q -> new ArithmeticShare(BigInteger.valueOf(q.getCohortSize()))).collect(Collectors.toList());
		List<ArithmeticShare> reconstrucedSingleMedicSharesOrg2 = queryResultsSingleMedicSharesOrg2.stream()
				.map(q -> new ArithmeticShare(BigInteger.valueOf(q.getCohortSize()))).collect(Collectors.toList());
		List<ArithmeticShare> reconstrucedSingleMedicSharesOrg3 = queryResultsSingleMedicSharesOrg3.stream()
				.map(q -> new ArithmeticShare(BigInteger.valueOf(q.getCohortSize()))).collect(Collectors.toList());

		int multiMedicShareOrg1 = arithmeticSharing
				.reconstructSecretToInt(List.of(reconstrucedSingleMedicSharesOrg1.get(0),
						reconstrucedSingleMedicSharesOrg2.get(1), reconstrucedSingleMedicSharesOrg3.get(2)));
		int multiMedicShareOrg2 = arithmeticSharing
				.reconstructSecretToInt(List.of(reconstrucedSingleMedicSharesOrg1.get(1),
						reconstrucedSingleMedicSharesOrg2.get(2), reconstrucedSingleMedicSharesOrg3.get(0)));
		int multiMedicShareOrg3 = arithmeticSharing
				.reconstructSecretToInt(List.of(reconstrucedSingleMedicSharesOrg1.get(2),
						reconstrucedSingleMedicSharesOrg2.get(0), reconstrucedSingleMedicSharesOrg3.get(1)));

		long total = arithmeticSharing
				.reconstructSecretToInt(List.of(new ArithmeticShare(BigInteger.valueOf(multiMedicShareOrg1)),
						new ArithmeticShare(BigInteger.valueOf(multiMedicShareOrg2)),
						new ArithmeticShare(BigInteger.valueOf(multiMedicShareOrg3))));

		assertEquals(60, total);
	}

	@Test
	public void fiftyPartiesAndSecrets()
	{
		SecureRandom randomGenerator = new SecureRandom();

		int numParties = 50;
		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(numParties);

		int maxSecret = arithmeticSharing.getRingSize().divide(BigInteger.valueOf(numParties)).intValueExact();

		int[] secrets = new int[numParties];
		List<List<ArithmeticShare>> shares = new ArrayList<>();

		for (int i = 0; i < numParties; i++)
		{
			secrets[i] = randomGenerator.nextInt(maxSecret);
			List<ArithmeticShare> sharesI = arithmeticSharing.createShares(secrets[i]);

			shares.add(sharesI);
		}

		for (int i = 0; i < numParties; i++)
		{
			assertEquals(secrets[i], arithmeticSharing.reconstructSecretToInt(shares.get(i)));
		}
	}

	@Test
	public void homomorphicAddition()
	{
		SecureRandom randomGenerator = new SecureRandom();

		int numParties = 5;
		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(numParties);

		int maxSecret = arithmeticSharing.getRingSize().divide(BigInteger.valueOf(numParties)).intValueExact();

		int[] secrets = new int[numParties];
		for (int i = 0; i < numParties; i++)
		{
			secrets[i] = randomGenerator.nextInt(maxSecret);
		}

		List<ArithmeticShare> shares1 = arithmeticSharing.createShares(secrets[0]);
		List<ArithmeticShare> shares2 = arithmeticSharing.createShares(secrets[1]);
		List<ArithmeticShare> shares3 = arithmeticSharing.createShares(secrets[2]);
		List<ArithmeticShare> shares4 = arithmeticSharing.createShares(secrets[3]);
		List<ArithmeticShare> shares5 = arithmeticSharing.createShares(secrets[4]);

		List<Integer> sumShares = new ArrayList<>();
		for (int i = 0; i < numParties; i++)
		{
			sumShares.add(arithmeticSharing.reconstructSecretToInt(
					List.of(shares1.get(i), shares2.get(i), shares3.get(i), shares4.get(i), shares5.get(i))));
		}

		int reconstructedResult = arithmeticSharing.reconstructSecretToInt(
				sumShares.stream().map(s -> new ArithmeticShare(BigInteger.valueOf(s))).collect(Collectors.toList()));

		int cleartextResult = Arrays.stream(secrets).sum();

		assertEquals(reconstructedResult, cleartextResult);
	}

	@Test
	public void manyManyParties()
	{
		int secret = Integer.MAX_VALUE - 1;
		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(54321);

		List<ArithmeticShare> shares = arithmeticSharing.createShares(secret);
		int reconstructed = arithmeticSharing.reconstructSecretToInt(shares);

		assertEquals(secret, reconstructed);
	}
}
