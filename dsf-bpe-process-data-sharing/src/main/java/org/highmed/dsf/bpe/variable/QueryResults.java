package org.highmed.dsf.bpe.variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryResults
{
	private final List<QueryResult> results = new ArrayList<>();

	@JsonCreator
	public QueryResults(@JsonProperty("results") Collection<? extends QueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public void add(QueryResult newResult)
	{
		if (newResult != null)
			results.add(newResult);
	}

	public void addAll(Collection<QueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	@JsonProperty("results")
	public List<QueryResult> getResults()
	{
		return Collections.unmodifiableList(results);
	}
}
