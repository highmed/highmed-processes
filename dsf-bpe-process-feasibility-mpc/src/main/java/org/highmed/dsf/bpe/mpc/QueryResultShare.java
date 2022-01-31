package org.highmed.dsf.bpe.mpc;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryResultShare implements Serializable
{
	private final String cohortId;
	private final String organizationIdentifier;
	private final ArithmeticShare arithmeticShare;

	@JsonCreator
	public QueryResultShare(@JsonProperty("cohortId") String cohortId,
			@JsonProperty("organizationIdentifier") String organizationIdentifier,
			@JsonProperty("arithmeticShare") ArithmeticShare arithmeticShare)
	{
		this.cohortId = cohortId;
		this.organizationIdentifier = organizationIdentifier;
		this.arithmeticShare = arithmeticShare;
	}

	public String getCohortId()
	{
		return cohortId;
	}

	public String getOrganizationIdentifier()
	{
		return organizationIdentifier;
	}

	public ArithmeticShare getArithmeticShare()
	{
		return arithmeticShare;
	}
}
