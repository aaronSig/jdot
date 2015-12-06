package com.superpixel.advokit.mapper;

public class SimpleMatchJava {
	private String game;
	private String venue;
	private String score;
	private String winningTeam;
	public SimpleMatchJava(String game, String venue, String score,
			String winningTeam) {
		super();
		this.game = game;
		this.venue = venue;
		this.score = score;
		this.winningTeam = winningTeam;
	}
	public String getGame() {
		return game;
	}
	public void setGame(String game) {
		this.game = game;
	}
	public String getVenue() {
		return venue;
	}
	public void setVenue(String venue) {
		this.venue = venue;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getWinningTeam() {
		return winningTeam;
	}
	public void setWinningTeam(String winningTeam) {
		this.winningTeam = winningTeam;
	}
	@Override
	public String toString() {
		return "SimpleMatch [game=" + game + ", venue=" + venue
				+ ", score=" + score + ", winningTeam=" + winningTeam + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((game == null) ? 0 : game.hashCode());
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		result = prime * result + ((venue == null) ? 0 : venue.hashCode());
		result = prime * result
				+ ((winningTeam == null) ? 0 : winningTeam.hashCode());
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
		SimpleMatchJava other = (SimpleMatchJava) obj;
		if (game == null) {
			if (other.game != null)
				return false;
		} else if (!game.equals(other.game))
			return false;
		if (score == null) {
			if (other.score != null)
				return false;
		} else if (!score.equals(other.score))
			return false;
		if (venue == null) {
			if (other.venue != null)
				return false;
		} else if (!venue.equals(other.venue))
			return false;
		if (winningTeam == null) {
			if (other.winningTeam != null)
				return false;
		} else if (!winningTeam.equals(other.winningTeam))
			return false;
		return true;
	}
}
