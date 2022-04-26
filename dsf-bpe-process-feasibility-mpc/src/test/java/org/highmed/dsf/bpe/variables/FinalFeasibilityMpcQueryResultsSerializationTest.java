package org.highmed.dsf.bpe.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.highmed.dsf.fhir.json.ObjectMapperFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

public class FinalFeasibilityMpcQueryResultsSerializationTest
{
	private static final Logger logger = LoggerFactory
			.getLogger(FinalFeasibilityMpcQueryResultsSerializationTest.class);

	@Test
	public void testEmptyFinalFeasibilityMpcQueryResultsSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		FinalFeasibilityMpcQueryResults results = new FinalFeasibilityMpcQueryResults(Collections.emptyList());

		String resultsAsString = mapper.writeValueAsString(results);
		assertNotNull(resultsAsString);

		logger.debug("FinalFeasibilityMpcQueryResults (empty): '{}'", resultsAsString);

		FinalFeasibilityMpcQueryResults readResults = mapper.readValue(resultsAsString,
				FinalFeasibilityMpcQueryResults.class);
		assertNotNull(readResults);
		assertNotNull(readResults.getResults());
		assertTrue(readResults.getResults().isEmpty());
	}

	@Test
	public void testNonEmptyFinalFeasibilityMpcQueryResultsSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		FinalFeasibilityMpcQueryResult result = new FinalFeasibilityMpcQueryResult("cohortId", 1, 2);
		FinalFeasibilityMpcQueryResults results = new FinalFeasibilityMpcQueryResults(Collections.singleton(result));

		String resultsAsString = mapper.writeValueAsString(results);
		assertNotNull(resultsAsString);

		logger.debug("FinalFeasibilityMpcQueryResults (non empty): '{}'", resultsAsString);

		FinalFeasibilityMpcQueryResults readResults = mapper.readValue(resultsAsString,
				FinalFeasibilityMpcQueryResults.class);
		assertNotNull(readResults);
		assertNotNull(readResults.getResults());
		assertFalse(readResults.getResults().isEmpty());
		assertEquals(1, readResults.getResults().size());
		assertNotNull(readResults.getResults().get(0));
		assertEquals(result.getCohortId(), readResults.getResults().get(0).getCohortId());
		assertEquals(result.getCohortSize(), readResults.getResults().get(0).getCohortSize());
		assertEquals(result.getParticipatingMedics(), readResults.getResults().get(0).getParticipatingMedics());
	}
}
