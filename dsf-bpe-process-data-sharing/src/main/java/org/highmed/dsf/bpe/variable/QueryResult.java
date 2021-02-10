package org.highmed.dsf.bpe.variable;

import org.highmed.openehr.model.structure.ResultSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryResult
{
	private final String organizationIdentifier;
	private final String cohortId;
	private final int count;
	private final ResultSet resultSet;
	private final String resultSetUrl;

	public static QueryResult count(String organizationIdentifier, String cohortId, int count)
	{
		if (count < 0)
			throw new IllegalArgumentException("count >= 0 expected");

		return new QueryResult(organizationIdentifier, cohortId, count, null, null);
	}

	public static QueryResult resultSet(String organizationIdentifier, String cohortId, ResultSet resultSet)
	{
		return new QueryResult(organizationIdentifier, cohortId, -1, resultSet, null);
	}

	public static QueryResult resultSet(String organizationIdentifier, String cohortId, String resultSetUrl)
	{
		return new QueryResult(organizationIdentifier, cohortId, -1, null, resultSetUrl);
	}

	@JsonCreator
	public QueryResult(@JsonProperty("organizationIdentifier") String organizationIdentifier,
			@JsonProperty("cohortId") String cohortId, @JsonProperty("count") int count,
			@JsonProperty("resultSet") ResultSet resultSet, @JsonProperty("resultSetUrl") String resultSetUrl)
	{
		this.organizationIdentifier = organizationIdentifier;
		this.cohortId = cohortId;
		this.count = count;
		this.resultSet = resultSet;
		this.resultSetUrl = resultSetUrl;
	}

	public String getOrganizationIdentifier()
	{
		return organizationIdentifier;
	}

	public String getCohortId()
	{
		return cohortId;
	}

	public int getCount()
	{
		return count;
	}

	public ResultSet getResultSet()
	{
		return resultSet;
	}

	public String getResultSetUrl()
	{
		return resultSetUrl;
	}

	@JsonIgnore
	public boolean isCountResult()
	{
		return resultSet == null && resultSetUrl == null;
	}

	@JsonIgnore
	public boolean isIdResultSetResult()
	{
		return resultSet != null && resultSetUrl == null;
	}

	@JsonIgnore
	public boolean isIdResultSetUrlResult()
	{
		return resultSet == null && resultSetUrl != null;
	}
}
