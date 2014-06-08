package de.codecentric.datalab.soccerPrediction.featureCreation.computer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.codecentric.datalab.soccerPrediction.featureCreation.FeatureComputer;
import de.codecentric.datalab.soccerPrediction.featureCreation.LeagueTable;
import de.codecentric.datalab.soccerPrediction.featureCreation.LeagueTables;
import de.codecentric.datalab.soccerPrediction.featureCreation.Match;
import de.codecentric.datalab.soccerPrediction.featureCreation.Matches;
import de.codecentric.datalab.soccerPrediction.featureCreation.Team;
import de.codecentric.datalab.soccerPrediction.featureCreation.Teams;

public class FeatureLastNGamesPointsAverageWeighted implements FeatureComputer{
	
	private LeagueTable leagueTable;
	
	private int n;

	public FeatureLastNGamesPointsAverageWeighted(int n, LeagueTable leagueTable) {
		this.n =n;
		this.leagueTable = leagueTable;
	}

    @Override
    public List<String> getColumnNames() {
        List<String> toReturn = new ArrayList<>();
        String prefix = "b_last_" + n + "games_points_average_weighted_";
        toReturn.add(prefix + "home_team");
        toReturn.add(prefix + "away_team");
        toReturn.add(prefix + "home_minus_away_team");
        return toReturn;
    }

    @Override
	public String getDescription() {
		StringBuilder buffer = new StringBuilder();
		for (String s:getColumnNames()) buffer.append(s+", ");
		buffer.append("   Computes the weighted averages from the last "+n+" games. Based on the points"+
				"   gained (three for win, one for draw). "+
				"   Points are weighted based on the position of the competing team in the league table "+
				"   For convenience also the difference between these scores for home and away team are included"+
				"   For convenience 0 is written instead of NaN when no previous games are known.");
		return buffer.toString();
	}

	@Override
	public List<String> getValues(Match match, Matches matches, Teams teams, LeagueTables leagueTables) {
		double homeTeamAverageGained = averagePointsGainedLastNGames(match.homeTeam, n, match, teams);
		double awayTeamAverageGained = averagePointsGainedLastNGames(match.awayTeam, n, match, teams);
		List<String> toReturn = new ArrayList<>();
		DecimalFormat df = new DecimalFormat("###.###");
		
		toReturn.add(df.format(homeTeamAverageGained));
		toReturn.add(df.format(awayTeamAverageGained));
		toReturn.add(df.format(homeTeamAverageGained-awayTeamAverageGained));
		return toReturn;
	}
	
	private  double averagePointsGainedLastNGames(String teamName, int n, Match currentMatch, Teams teams) {
		Team myTeam = teams.getTeam(teamName);
		double points = 0;
		List<Match> lastNMatches = myTeam.getNPreviousMatches(n, currentMatch.getDate());
		for (Match m: lastNMatches) {
			if (teamName.equals(m.homeTeam)) {
				double weight = getTeamWeight(m.awayTeam, leagueTable);
				if (m.matchClass.equals(Match.MatchClass.HOME_WIN)) points += 3*weight;
				else if (m.matchClass.equals(Match.MatchClass.DRAW)) points += 1*weight;
			}
			else {
				double weight = getTeamWeight(m.homeTeam, leagueTable);
				if (m.matchClass.equals(Match.MatchClass.AWAY_WIN)) points += 3*weight;
				else if (m.matchClass.equals(Match.MatchClass.DRAW)) points += 1*weight;
			}
		}
		if (lastNMatches.size()==0) return 0d;
		else return points / lastNMatches.size();
	}
	
	public double getTeamWeight(String teamName, LeagueTable table) {
		int teamPosition = table.getLeagueEntry(teamName).getCurrentPosition();
		if (teamPosition == 0) return 0.25;
		else {
			double toReturn =  (((double) table.getMaxPosition())-((double) teamPosition))/((double)table.getMaxPosition());
			return Math.pow(toReturn, 2);
		}
	}


	
}
