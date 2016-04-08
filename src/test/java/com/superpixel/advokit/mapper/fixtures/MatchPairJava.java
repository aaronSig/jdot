package com.superpixel.advokit.mapper.fixtures;

public class MatchPairJava {

	private MatchJava matchOne;
	private MatchJava matchTwo;

	public MatchPairJava(MatchJava matchOne, MatchJava matchTwo) {
		super();
		this.matchOne = matchOne;
		this.matchTwo = matchTwo;
	}

	public MatchJava getMatchOne() {
		return matchOne;
	}

	public void setMatchOne(MatchJava matchOne) {
		this.matchOne = matchOne;
	}

	public MatchJava getMatchTwo() {
		return matchTwo;
	}

	public void setMatchTwo(MatchJava matchTwo) {
		this.matchTwo = matchTwo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((matchOne == null) ? 0 : matchOne.hashCode());
		result = prime * result
				+ ((matchTwo == null) ? 0 : matchTwo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchPairJava other = (MatchPairJava) obj;
		if (matchOne == null) {
			if (other.matchOne != null)
				return false;
		} else if (!matchOne.equals(other.matchOne))
			return false;
		if (matchTwo == null) {
			if (other.matchTwo != null)
				return false;
		} else if (!matchTwo.equals(other.matchTwo))
			return false;
		return true;
	}

}
