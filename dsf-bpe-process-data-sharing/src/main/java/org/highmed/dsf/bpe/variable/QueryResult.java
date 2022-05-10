package org.highmed.dsf.bpe.variable;

import org.highmed.openehr.model.structure.ResultSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryResult
{
	private final String organizationIdentifier;
	private final String cohortId;
	private final int cohortSize;
	private final ResultSet resultSet;
	private final String resultSetUrl;

	public static QueryResult countResult(String organizationIdentifier, String cohortId, int cohortSize)
	{
		if (cohortSize < 0)
			throw new IllegalArgumentException("cohortSize >= 0 expected");

		return new QueryResult(organizationIdentifier, cohortId, cohortSize, null, null);
	}

	public static QueryResult mpcCountResult(String organizationIdentifier, String cohortId, int cohortSize)
	{
		return new QueryResult(organizationIdentifier, cohortId, cohortSize, null, null);
	}

	public static QueryResult idResult(String organizationIdentifier, String cohortId, ResultSet resultSet)
	{
		return new QueryResult(organizationIdentifier, cohortId, -1, resultSet, null);
	}

	public static QueryResult idResult(String organizationIdentifier, String cohortId, String resultSetUrl)
	{
		return new QueryResult(organizationIdentifier, cohortId, -1, null, resultSetUrl);
	}

	@JsonCreator
	public QueryResult(@JsonProperty("organizationIdentifier") String organizationIdentifier,
			@JsonProperty("cohortId") String cohortId, @JsonProperty("cohortSize") int cohortSize,
			@JsonProperty("resultSet") ResultSet resultSet, @JsonProperty("resultSetUrl") String resultSetUrl)
	{
		this.organizationIdentifier = organizationIdentifier;
		this.cohortId = cohortId;
		this.cohortSize = cohortSize;
		this.resultSet = resultSet;
		this.resultSetUrl = resultSetUrl;
	}

	@JsonProperty("organizationIdentifier")
	public String getOrganizationIdentifier()
	{
		return organizationIdentifier;
	}

	@JsonProperty("cohortId")
	public String getCohortId()
	{
		return cohortId;
	}

	@JsonProperty("cohortSize")
	public int getCohortSize()
	{
		return cohortSize;
	}

	@JsonProperty("resultSet")
	public ResultSet getResultSet()
	{
		return resultSet;
	}

	@JsonProperty("resultSetUrl")
	public String getResultSetUrl()
	{
		return resultSetUrl;
	}

	@JsonIgnore
	public boolean isCohortSizeResult()
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