package com.superpixel.advokit.mapper.fixtures;

import java.util.List;

public class WeekendJava {

	private String weekName;
	private List<MatchJava> matchList;
	public WeekendJava(String weekName, List<MatchJava> matchList) {
		super();
		this.weekName = weekName;
		this.matchList = matchList;
	}
	public String getWeekName() {
		return weekName;
	}
	public void setWeekName(String weekName) {
		this.weekName = weekName;
	}
	public List<MatchJava> getMatchList() {
		return matchList;
	}
	public void setMatchList(List<MatchJava> matchList) {
		this.matchList = matchList;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((matchList == null) ? 0 : matchList.hashCode());
		result = prime * result
				+ ((weekName == null) ? 0 : weekName.hashCode());
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
		WeekendJava other = (WeekendJava) obj;
		if (matchList == null) {
			if (other.matchList != null)
				return false;
		} else if (!matchList.equals(other.matchList))
			return false;
		if (weekName == null) {
			if (other.weekName != null)
				return false;
		} else if (!weekName.equals(other.weekName))
			return false;
		return true;
	}
	
	
	
}
