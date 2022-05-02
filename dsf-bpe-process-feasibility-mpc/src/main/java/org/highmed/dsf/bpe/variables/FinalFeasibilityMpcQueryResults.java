package org.highmed.dsf.bpe.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FinalFeasibilityMpcQueryResults
{
	private final List<FinalFeasibilityMpcQueryResult> results = new ArrayList<>();

	@JsonCreator
	public FinalFeasibilityMpcQueryResults(
			@JsonProperty("results") Collection<? extends FinalFeasibilityMpcQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public void add(FinalFeasibilityMpcQueryResult newResult)
	{
		if (newResult != null)
			results.add(newResult);
	}

	public void addAll(Collection<FinalFeasibilityMpcQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	@JsonProperty("results")
	public List<FinalFeasibilityMpcQueryResult> getResults()
	{
		return Collections.unmodifiableList(results);
	}
}
