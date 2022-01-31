package org.highmed.dsf.bpe.mpc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryResultShares implements Serializable
{
	private final List<QueryResultShare> shares = new ArrayList<>();

	@JsonCreator
	public QueryResultShares(@JsonProperty("shares") Collection<? extends QueryResultShare> shares)
	{
		if (shares != null)
			this.shares.addAll(shares);
	}

	public void add(QueryResultShare newShare)
	{
		if (newShare != null)
			shares.add(newShare);
	}

	public void addAll(Collection<QueryResultShare> shares)
	{
		if (shares != null)
			this.shares.addAll(shares);
	}

	public List<QueryResultShare> getShares()
	{
		return Collections.unmodifiableList(shares);
	}
}
