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
				.reconstructSecretToInt(List.of(reconstrucedSingleMedicSharesOrg1.get(0), reconstrucedSingleMedicSharesOrg2.get(1), reconstrucedSingleMedicSharesOrg3.get(2)));
		int multiMedicShareOrg2 = arithmeticSharing
				.reconstructSecretToInt(List.of(reconstrucedSingleMedicSharesOrg1.get(1), reconstrucedSingleMedicSharesOrg2.get(2), reconstrucedSingleMedicSharesOrg3.get(0)));
		int multiMedicShareOrg3 = arithmeticSharing
				.reconstructSecretToInt(List.of(reconstrucedSingleMedicSharesOrg1.get(2), reconstrucedSingleMedicSharesOrg2.get(0), reconstrucedSingleMedicSharesOrg3.get(1)));

		long total = arithmeticSharing
				.reconstructSecretToInt(List.of(new ArithmeticShare(BigInteger.valueOf(multiMedicShareOrg1)),
						new ArithmeticShare(BigInteger.valueOf(multiMedicShareOrg2)),
						new ArithmeticShare(BigInteger.valueOf(multiMedicShareOrg3))));

		assertEquals(60, total);
	}
}
