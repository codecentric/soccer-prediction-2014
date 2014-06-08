package de.codecentric.datalab.soccerPrediction.featureCreation;

import java.util.*;

public class LeagueTables {
	
	private Map<String,LeagueTable> tables= new HashMap<>();
	
	private Set<Match> matchBuffer = new LinkedHashSet<>();
	private int lastSeenDay = -1;
	
	
	public LeagueTable getLeagueTable(String tableName) {
		return tables.get(tableName);
	}
	
	public LeagueTables() {
		LeagueTable pointsLeagueTable = new LeagueTable("points_league_table", new LeagueTable.SimpleWinLooseScorer());
		tables.put(pointsLeagueTable.getName(), pointsLeagueTable);
		LeagueTable weightedPointsLeagueTable = new LeagueTable("weighted_points_league_table", new LeagueTable.WeightedWinLooseScorer());
		tables.put(weightedPointsLeagueTable.getName(), weightedPointsLeagueTable);
	}
	

	/**
	 * Updates all league tables with a match. Expects to be called with 
	 * matches in chronological order (earliest first) and only updates 
	 * before the games on the next day. 
	 */
	public void updateWithMatch(Match match) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(match.getDate());
		int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR); 
		if (dayOfYear != lastSeenDay) {
			lastSeenDay = dayOfYear; 
			for (LeagueTable lTable: tables.values()) {
				lTable.updateLeague(matchBuffer);
			}
			matchBuffer.clear();
		}
		matchBuffer.add(match);
	}
	
	
	
	

}
