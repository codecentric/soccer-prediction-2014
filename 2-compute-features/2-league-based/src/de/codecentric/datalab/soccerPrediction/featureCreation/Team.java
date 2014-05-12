package de.codecentric.datalab.soccerPrediction.featureCreation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Team {

	private String teamName;
	private List<Match> teamMatches = new ArrayList<>();
	
	public Team (String teamName) {
		this.teamName = teamName;
	}
	
	/**
	 * Adds a match for this team. It is assumed that matches are added in 
	 * historical order, old games first.
	 */
	public void addMatch(Match match) {
		teamMatches.add(match);
	}
	
	public String toString() {
		return teamName + " ("+teamMatches.size()+" games)";
	}
	
	public List<Match> getNPreviousMatches(int n, Date date) {
		List<Match> toReturn = new ArrayList<>(n);
		for (int i=teamMatches.size()-1;(i>=0&&n>0);i--) {
			if (teamMatches.get(i).getDate().before(date)){
				toReturn.add(teamMatches.get(i));
				n--;
			}
		}
		return toReturn;
	}
	
	/**
	 * Returns all games that happend before date but after data-relevantDuration, 
	 * e.g. all games in the month before that game.
	 */
	public List<Match> getPreviousMatches(Date date, long relevantDuration) {
		Date earliestGame = new Date(date.getTime()-relevantDuration);
		List<Match> toReturn = new ArrayList<>();
		for (int i=teamMatches.size()-1;i>=0;i--) {
			Date dateOfMatch = teamMatches.get(i).getDate();
			if (dateOfMatch.before(date) && dateOfMatch.after(earliestGame)) {
				toReturn.add(teamMatches.get(i));
			}
		}
		return toReturn;
	}

}
