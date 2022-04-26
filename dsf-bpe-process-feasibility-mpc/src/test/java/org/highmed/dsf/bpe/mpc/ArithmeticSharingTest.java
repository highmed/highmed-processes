package org.highmed.dsf.bpe.mpc;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

		ArithmeticShare[] singleMedicSharesOrg1 = arithmeticSharing.createShares(10);
		ArithmeticShare[] singleMedicSharesOrg2 = arithmeticSharing.createShares(20);
		ArithmeticShare[] singleMedicSharesOrg3 = arithmeticSharing.createShares(30);

		List<QueryResult> queryResultsSingleMedicSharesOrg1 = IntStream.range(0, arithmeticSharing.getNumParties())
				.mapToObj(i -> QueryResult.mpcCountResult("", "", singleMedicSharesOrg1[i].getValue().intValueExact()))
				.collect(Collectors.toList());
		List<QueryResult> queryResultsSingleMedicSharesOrg2 = IntStream.range(0, arithmeticSharing.getNumParties())
				.mapToObj(i -> QueryResult.mpcCountResult("", "", singleMedicSharesOrg2[i].getValue().intValueExact()))
				.collect(Collectors.toList());
		List<QueryResult> queryResultsSingleMedicSharesOrg3 = IntStream.range(0, arithmeticSharing.getNumParties())
				.mapToObj(i -> QueryResult.mpcCountResult("", "", singleMedicSharesOrg3[i].getValue().intValueExact()))
				.collect(Collectors.toList());

		ArithmeticShare[] reconstructedSingleMedicSharesOrg1 = queryResultsSingleMedicSharesOrg1.stream()
				.map(QueryResult::getCohortSize).map(ArithmeticShare::new).toArray(ArithmeticShare[]::new);
		ArithmeticShare[] reconstructedSingleMedicSharesOrg2 = queryResultsSingleMedicSharesOrg2.stream()
				.map(QueryResult::getCohortSize).map(ArithmeticShare::new).toArray(ArithmeticShare[]::new);
		ArithmeticShare[] reconstructedSingleMedicSharesOrg3 = queryResultsSingleMedicSharesOrg3.stream()
				.map(QueryResult::getCohortSize).map(ArithmeticShare::new).toArray(ArithmeticShare[]::new);

		int multiMedicShareOrg1 = arithmeticSharing
				.reconstructSecretToInt(new ArithmeticShare[] { reconstructedSingleMedicSharesOrg1[0],
						reconstructedSingleMedicSharesOrg2[1], reconstructedSingleMedicSharesOrg3[2] });
		int multiMedicShareOrg2 = arithmeticSharing
				.reconstructSecretToInt(new ArithmeticShare[] { reconstructedSingleMedicSharesOrg1[1],
						reconstructedSingleMedicSharesOrg2[2], reconstructedSingleMedicSharesOrg3[0] });
		int multiMedicShareOrg3 = arithmeticSharing
				.reconstructSecretToInt(new ArithmeticShare[] { reconstructedSingleMedicSharesOrg1[2],
						reconstructedSingleMedicSharesOrg2[0], reconstructedSingleMedicSharesOrg3[1] });

		long total = arithmeticSharing
				.reconstructSecretToInt(new ArithmeticShare[] { new ArithmeticShare(multiMedicShareOrg1),
						new ArithmeticShare(multiMedicShareOrg2), new ArithmeticShare(multiMedicShareOrg3) });

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
		List<ArithmeticShare[]> shares = new ArrayList<>();

		for (int i = 0; i < numParties; i++)
		{
			secrets[i] = randomGenerator.nextInt(maxSecret);
			ArithmeticShare[] sharesI = arithmeticSharing.createShares(secrets[i]);

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

		ArithmeticShare[] shares1 = arithmeticSharing.createShares(secrets[0]);
		ArithmeticShare[] shares2 = arithmeticSharing.createShares(secrets[1]);
		ArithmeticShare[] shares3 = arithmeticSharing.createShares(secrets[2]);
		ArithmeticShare[] shares4 = arithmeticSharing.createShares(secrets[3]);
		ArithmeticShare[] shares5 = arithmeticSharing.createShares(secrets[4]);

		List<Integer> sumShares = new ArrayList<>();
		for (int i = 0; i < numParties; i++)
		{
			sumShares.add(arithmeticSharing.reconstructSecretToInt(
					new ArithmeticShare[] { shares1[i], shares2[i], shares3[i], shares4[i], shares5[i] }));
		}

		int reconstructedResult = arithmeticSharing
				.reconstructSecretToInt(sumShares.stream().map(ArithmeticShare::new).toArray(ArithmeticShare[]::new));

		int cleartextResult = Arrays.stream(secrets).sum();

		assertEquals(reconstructedResult, cleartextResult);
	}

	@Test
	public void manyManyParties()
	{
		int secret = Integer.MAX_VALUE - 1;
		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(54321);

		ArithmeticShare[] shares = arithmeticSharing.createShares(secret);
		int reconstructed = arithmeticSharing.reconstructSecretToInt(shares);

		assertEquals(secret, reconstructed);
	}
}
