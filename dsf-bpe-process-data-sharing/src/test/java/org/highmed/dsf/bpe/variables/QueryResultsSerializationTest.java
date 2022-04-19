package org.highmed.dsf.bpe.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.bpe.variable.QueryResults;
import org.highmed.dsf.fhir.json.ObjectMapperFactory;
import org.highmed.openehr.model.datatypes.IntegerRowElement;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

public class QueryResultsSerializationTest
{
	private static final Logger logger = LoggerFactory.getLogger(QueryResultsSerializationTest.class);

	@Test
	public void testEmptyQueryResultsSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		QueryResults results = new QueryResults(Collections.emptyList());

		String resultsAsString = mapper.writeValueAsString(results);
		assertNotNull(resultsAsString);

		logger.debug("QueryResults (empty): '{}'", resultsAsString);

		QueryResults readResults = mapper.readValue(resultsAsString, QueryResults.class);
		assertNotNull(readResults);
		assertNotNull(readResults.getResults());
		assertTrue(readResults.getResults().isEmpty());
	}

	@Test
	public void testNonEmptyQueryResultsSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		QueryResult result = new QueryResult("test.org", UUID.randomUUID().toString(), 1, createResultSet(),
				"http://test.com/fhir/Binary/" + UUID.randomUUID().toString());
		QueryResults results = new QueryResults(Collections.singleton(result));

		String resultsAsString = mapper.writeValueAsString(results);
		assertNotNull(resultsAsString);

		logger.debug("QueryResults (non empty): '{}'", resultsAsString);

		QueryResults readResults = mapper.readValue(resultsAsString, QueryResults.class);
		assertNotNull(readResults);
		assertNotNull(readResults.getResults());
		assertFalse(readResults.getResults().isEmpty());
		assertEquals(1, readResults.getResults().size());
		assertNotNull(readResults.getResults().get(0));
		assertEquals(result.getCohortId(), readResults.getResults().get(0).getCohortId());
		assertEquals(result.getCohortSize(), readResults.getResults().get(0).getCohortSize());
		assertEquals(result.getOrganizationIdentifier(), readResults.getResults().get(0).getOrganizationIdentifier());
		assertEquals(result.getResultSetUrl(), readResults.getResults().get(0).getResultSetUrl());
		assertNotNull(readResults.getResults().get(0).getResultSet());
		assertEquals(result.getResultSet().getName(), readResults.getResults().get(0).getResultSet().getName());
		assertEquals(result.getResultSet().getQuery(), readResults.getResults().get(0).getResultSet().getQuery());
		// Not testing every ResultSet property, see ResultSet serialization tests
	}

	private ResultSet createResultSet()
	{
		Meta meta = new Meta("href", "type", "schemaVersion", "created", "generator", "executedAql");
		List<Column> columns = Arrays.asList(new Column("name1", "path1"), new Column("name2", "path2"));
		List<List<RowElement>> rows = Arrays.asList(Arrays.asList(new IntegerRowElement(123)),
				Arrays.asList(new StringRowElement("456")));
		ResultSet resultSet = new ResultSet(meta, "name", "query", columns, rows);
		return resultSet;
	}
}
