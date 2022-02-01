package org.highmed.dsf.bpe.mpc;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.highmed.dsf.bpe.variable.QueryResult;
import org.junit.Test;

public class ArithmeticSharingTest
{
	@Test
	public void arithmeticSharingValid()
	{
		ArithmeticSharing arithmeticSharing = new ArithmeticSharing(3);

		List<ArithmeticShare> shares1 = arithmeticSharing.createShares(15);
		List<ArithmeticShare> shares2 = arithmeticSharing.createShares(15);
		List<ArithmeticShare> shares3 = arithmeticSharing.createShares(15);

		List<QueryResult> shares1q = shares1.stream()
				.map(s -> QueryResult.mpcCountResult("", "", s.getValue().intValueExact()))
				.collect(Collectors.toList());
		List<QueryResult> shares2q = shares2.stream()
				.map(s -> QueryResult.mpcCountResult("", "", s.getValue().intValueExact()))
				.collect(Collectors.toList());
		List<QueryResult> shares3q = shares3.stream()
				.map(s -> QueryResult.mpcCountResult("", "", s.getValue().intValueExact()))
				.collect(Collectors.toList());

		List<ArithmeticShare> shares1s = shares1q.stream()
				.map(q -> new ArithmeticShare(BigInteger.valueOf(q.getCohortSize()))).collect(Collectors.toList());
		List<ArithmeticShare> shares2s = shares2q.stream()
				.map(q -> new ArithmeticShare(BigInteger.valueOf(q.getCohortSize()))).collect(Collectors.toList());
		List<ArithmeticShare> shares3s = shares3q.stream()
				.map(q -> new ArithmeticShare(BigInteger.valueOf(q.getCohortSize()))).collect(Collectors.toList());

		int reconstructed1 = arithmeticSharing
				.reconstructSecretToInt(List.of(shares1s.get(0), shares2s.get(0), shares3s.get(0)));
		int reconstructed2 = arithmeticSharing
				.reconstructSecretToInt(List.of(shares1s.get(1), shares2s.get(1), shares3s.get(1)));
		int reconstructed3 = arithmeticSharing
				.reconstructSecretToInt(List.of(shares1s.get(2), shares2s.get(2), shares3s.get(2)));

		int total = arithmeticSharing
				.reconstructSecretToInt(List.of(new ArithmeticShare(BigInteger.valueOf(reconstructed1)),
						new ArithmeticShare(BigInteger.valueOf(reconstructed2)),
						new ArithmeticShare(BigInteger.valueOf(reconstructed3))));

		assertEquals(45, total);
	}
}
