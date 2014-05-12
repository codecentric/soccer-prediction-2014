package de.codecentric.datalab.soccerPrediction.featureCreation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeagueTable {

	public static class LeagueEntry implements Comparable<LeagueEntry> {
		
		public final String teamName;
		private double currentScore = 0;
		private int currentPosition = 0;
		
		LeagueEntry(String teamName) {
			this.teamName = teamName;
		}

		@Override
		public int compareTo(LeagueEntry o) {
			return (int) ((o.currentScore-currentScore)*10000d);
		}
		
		public int getCurrentPosition() {
			return currentPosition;
		}
		
	}
	
	public interface Scorer {
		double scoreHomeTeam(double oldScore, Match match, LeagueTable leagueTable);
		double scoreAwayTeam(double oldScore, Match match, LeagueTable leagueTable); 
	}

	public static class SimpleWinLooseScorer implements Scorer {

		@Override
		public double scoreHomeTeam(double oldScore, Match match,LeagueTable leagueTable) {
			double toReturn = oldScore * 0.9; 
			if (match.homeScore>match.awayScore) toReturn +=3;
			else if (match.homeScore==match.awayScore) toReturn +=1;
			return toReturn;
		}

		@Override
		public double scoreAwayTeam(double oldScore, Match match, LeagueTable leagueTable) {
			double toReturn = oldScore * 0.9; 
			if (match.awayScore>match.homeScore) toReturn +=3;
			else if (match.homeScore==match.awayScore) toReturn +=1;
			return toReturn;
		}
		
	}
	
	public static class WeightedWinLooseScorer implements Scorer {

		@Override
		public double scoreHomeTeam(double oldScore, Match match,LeagueTable leagueTable) {
			double toReturn = oldScore * 0.9; 
			double weightAwayTeam = getTeamWeight(match.awayTeam,leagueTable); 
			if (match.homeScore>match.awayScore) toReturn +=(3*weightAwayTeam);
			else if (match.homeScore==match.awayScore) toReturn +=(1*weightAwayTeam);
			return toReturn;
		}

		@Override
		public double scoreAwayTeam(double oldScore, Match match, LeagueTable leagueTable) {
			double toReturn = oldScore * 0.9; 
			double weightHomeTeam = getTeamWeight(match.homeTeam,leagueTable); 
			if (match.awayScore>match.homeScore) toReturn += (3*weightHomeTeam);
			else if (match.homeScore==match.awayScore) toReturn +=(1*weightHomeTeam);
			return toReturn;
		}
		
		public double getTeamWeight(String teamName, LeagueTable table) {
			int teamPosition = table.getLeagueEntry(teamName).currentPosition;
			if (teamPosition == 0) return 0.25;
			else {
				double toReturn =  (((double) table.maxPosition)-((double) teamPosition))/((double)table.maxPosition);
				return Math.pow(toReturn, 2);
			}
		}
		
	}
	
	private List<LeagueEntry> league = new ArrayList<>();
	private Map<String,LeagueEntry> leagueEntries = new HashMap<>();
	private Scorer scorer;
	private String name;
	private int maxPosition = 1;
	
	public LeagueTable (String name, Scorer scorer) {
		this.scorer = scorer;
		this.name = name;
	}
	
	public void updateLeague(Collection<Match> matches) {
		for (Match m: matches) updateLeague(m);
		Collections.sort(league);
		double lastSeenScore = Double.MAX_VALUE;
		int position = 0;
        for (LeagueEntry aLeague : league) {
            double currentScore = aLeague.currentScore;
            if (currentScore < lastSeenScore) {
                position++;
                lastSeenScore = currentScore;
            }
            aLeague.currentPosition = position;
        }
        maxPosition = position;
	}
	
	private void updateLeague(Match match) {
		LeagueEntry homeEntry = getLeagueEntry(match.homeTeam);
		LeagueEntry awayEntry = getLeagueEntry(match.awayTeam);
		homeEntry.currentScore = scorer.scoreHomeTeam(homeEntry.currentScore, match, this);
		awayEntry.currentScore = scorer.scoreAwayTeam(awayEntry.currentScore, match, this);
	}
	
	public LeagueEntry getLeagueEntry(String teamName) {
		LeagueEntry toReturn = leagueEntries.get(teamName);
		if (toReturn == null){
			toReturn = new LeagueEntry (teamName);
			leagueEntries.put(teamName, toReturn);
			league.add(toReturn);
		}
		return toReturn;
	}
	
	public String getName() {
		return name;
	}
	
	public int getMaxPosition() {
		return maxPosition;
	}
	
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("************* "+getName()+"\n");
		for (LeagueEntry l:league) {
			buffer.append("  "+l.currentPosition+" "+l.teamName+"   ("+l.currentScore+")\n");
		}
		return buffer.toString();
	}
	
}
