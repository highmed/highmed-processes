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

public class FinalFeasibilityQueryResultsSerializationTest
{
	private static final Logger logger = LoggerFactory.getLogger(FinalFeasibilityQueryResultsSerializationTest.class);

	@Test
	public void testEmptyFinalFeasibilityQueryResultsSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		FinalFeasibilityQueryResults results = new FinalFeasibilityQueryResults(Collections.emptyList());

		String resultsAsString = mapper.writeValueAsString(results);
		assertNotNull(resultsAsString);

		logger.debug("FinalFeasibilityQueryResults (empty): '{}'", resultsAsString);

		FinalFeasibilityQueryResults readResults = mapper.readValue(resultsAsString,
				FinalFeasibilityQueryResults.class);
		assertNotNull(readResults);
		assertNotNull(readResults.getResults());
		assertTrue(readResults.getResults().isEmpty());
	}

	@Test
	public void testNonEmptyFinalFeasibilityQueryResultsSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		FinalFeasibilityQueryResult result = new FinalFeasibilityQueryResult("cohortId", 1, 2);
		FinalFeasibilityQueryResults results = new FinalFeasibilityQueryResults(Collections.singleton(result));

		String resultsAsString = mapper.writeValueAsString(results);
		assertNotNull(resultsAsString);

		logger.debug("FinalFeasibilityQueryResults (non empty): '{}'", resultsAsString);

		FinalFeasibilityQueryResults readResults = mapper.readValue(resultsAsString,
				FinalFeasibilityQueryResults.class);
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
