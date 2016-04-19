package com.superpixel.jdot.fixtures;

import java.util.List;

public class GoalsHolder {

	private SimpleMatchJava match;
	private List<String> awayGoals;
	private String firstHomeGoal;
	private List<String> scoreNames;
	public GoalsHolder(SimpleMatchJava match, List<String> awayGoals,
			String firstHomeGoal, List<String> scoreNames) {
		super();
		this.match = match;
		this.awayGoals = awayGoals;
		this.firstHomeGoal = firstHomeGoal;
		this.scoreNames = scoreNames;
	}
	public SimpleMatchJava getMatch() {
		return match;
	}
	public void setMatch(SimpleMatchJava match) {
		this.match = match;
	}
	public List<String> getAwayGoals() {
		return awayGoals;
	}
	public void setAwayGoals(List<String> awayGoals) {
		this.awayGoals = awayGoals;
	}
	public String getFirstHomeGoal() {
		return firstHomeGoal;
	}
	public void setFirstHomeGoal(String firstHomeGoal) {
		this.firstHomeGoal = firstHomeGoal;
	}
	public List<String> getScoreNames() {
		return scoreNames;
	}
	public void setScoreNames(List<String> scoreNames) {
		this.scoreNames = scoreNames;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((awayGoals == null) ? 0 : awayGoals.hashCode());
		result = prime * result
				+ ((firstHomeGoal == null) ? 0 : firstHomeGoal.hashCode());
		result = prime * result + ((match == null) ? 0 : match.hashCode());
		result = prime * result
				+ ((scoreNames == null) ? 0 : scoreNames.hashCode());
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
		GoalsHolder other = (GoalsHolder) obj;
		if (awayGoals == null) {
			if (other.awayGoals != null)
				return false;
		} else if (!awayGoals.equals(other.awayGoals))
			return false;
		if (firstHomeGoal == null) {
			if (other.firstHomeGoal != null)
				return false;
		} else if (!firstHomeGoal.equals(other.firstHomeGoal))
			return false;
		if (match == null) {
			if (other.match != null)
				return false;
		} else if (!match.equals(other.match))
			return false;
		if (scoreNames == null) {
			if (other.scoreNames != null)
				return false;
		} else if (!scoreNames.equals(other.scoreNames))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "GoalsHolder [match=" + match + ", awayGoals=" + awayGoals
				+ ", firstHomeGoal=" + firstHomeGoal + ", scoreNames="
				+ scoreNames + "]";
	}
	
	
}
