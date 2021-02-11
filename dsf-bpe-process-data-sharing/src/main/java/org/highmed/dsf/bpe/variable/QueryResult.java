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
	private boolean isRbfResultSet;

	public static QueryResult count(String organizationIdentifier, String cohortId, int count)
	{
		if (count < 0)
			throw new IllegalArgumentException("count >= 0 expected");

		return new QueryResult(organizationIdentifier, cohortId, count, null, null, false);
	}

	public static QueryResult rbfResultSet(String organizationIdentifier, String cohortId, ResultSet resultSet)
	{
		return new QueryResult(organizationIdentifier, cohortId, -1, resultSet, null, true);
	}

	public static QueryResult rbfResultSet(String organizationIdentifier, String cohortId, String resultSetUrl)
	{
		return new QueryResult(organizationIdentifier, cohortId, -1, null, resultSetUrl, true);
	}

	public static QueryResult dataResultSet(String organizationIdentifier, String cohortId, ResultSet resultSet)
	{
		return new QueryResult(organizationIdentifier, cohortId, -1, resultSet, null, false);
	}

	public static QueryResult dataResultSet(String organizationIdentifier, String cohortId, String resultSetUrl)
	{
		return new QueryResult(organizationIdentifier, cohortId, -1, null, resultSetUrl, false);
	}

	@JsonCreator
	public QueryResult(@JsonProperty("organizationIdentifier") String organizationIdentifier,
			@JsonProperty("cohortId") String cohortId, @JsonProperty("count") int count,
			@JsonProperty("resultSet") ResultSet resultSet, @JsonProperty("resultSetUrl") String resultSetUrl,
			@JsonProperty("isRbfResultSet") boolean isRbfResultSet)
	{
		this.organizationIdentifier = organizationIdentifier;
		this.cohortId = cohortId;
		this.count = count;
		this.resultSet = resultSet;
		this.resultSetUrl = resultSetUrl;
		this.isRbfResultSet = isRbfResultSet;
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
	public boolean isRbfResultSetResult()
	{
		return isRbfResultSet && isResultSetResult();
	}

	@JsonIgnore
	public boolean isRbfResultSetUrlResult()
	{
		return isRbfResultSet && isResultSetUrlResult();
	}

	@JsonIgnore
	public boolean isDataResultSetResult()
	{
		return !isRbfResultSet && isResultSetResult();
	}

	@JsonIgnore
	public boolean isDataResultSetUrlResult()
	{
		return !isRbfResultSet && isResultSetUrlResult();
	}

	@JsonIgnore
	private boolean isResultSetResult()
	{
		return resultSet != null;
	}

	@JsonIgnore
	private boolean isResultSetUrlResult()
	{
		return resultSetUrl != null;
	}
}
