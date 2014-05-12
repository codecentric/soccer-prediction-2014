package de.codecentric.datalab.soccerPrediction.featureCreation.computer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.codecentric.datalab.soccerPrediction.featureCreation.FeatureComputer;
import de.codecentric.datalab.soccerPrediction.featureCreation.LeagueTables;
import de.codecentric.datalab.soccerPrediction.featureCreation.Match;
import de.codecentric.datalab.soccerPrediction.featureCreation.Matches;
import de.codecentric.datalab.soccerPrediction.featureCreation.Team;
import de.codecentric.datalab.soccerPrediction.featureCreation.Teams;

public class FeatureLastNGamesGoalAverage implements FeatureComputer{
	
	private int n;

	public FeatureLastNGamesGoalAverage(int n) {
		this.n =n;
	}

    @Override
    public List<String> getColumnNames() {
        List<String> toReturn = new ArrayList<>();
        String prefix = "b_last_" + n + "_games_goal_average_";
        toReturn.add(prefix + "home_team");
        toReturn.add(prefix + "away_team");
        toReturn.add(prefix + "home_minus_away_team");
        toReturn.add(prefix + "difference_home_team");
        toReturn.add(prefix + "difference_away_team");
        toReturn.add(prefix + "difference_home_minus_away_team");
        return toReturn;
    }

    @Override
	public String getDescription() {
		StringBuilder buffer = new StringBuilder();
		for (String s:getColumnNames()) buffer.append(s+", ");
		buffer.append("   Computes the averages from the last "+n+" games. Based on the goals"+
				"   shot and goal difference between the teams  "+
				"   i.e. this can be negative if the competing teams shot more goals."+
				"   For convenience also the difference between these scores for home and away team are included"+
				"   For convenience 0 is written instead of NaN when no previous games are known.");
		return buffer.toString();
	}

	@Override
	public List<String> getValues(Match match, Matches matches, Teams teams, LeagueTables leagueTables) {
		double homeTeamAverageShot = averageGoalsShotLastNGames(match.homeTeam, n, match, teams);
		double awayTeamAverageShot = averageGoalsShotLastNGames(match.awayTeam, n, match, teams);
		double homeTeamAverageReceived = averageGoalsReceivedLastNGames(match.homeTeam, n, match, teams);
		double awayTeamAverageReceived = averageGoalsReceivedLastNGames(match.awayTeam, n, match, teams);
		List<String> toReturn = new ArrayList<>();
		DecimalFormat df = new DecimalFormat("###.###");
		
		toReturn.add(df.format(homeTeamAverageShot));
		toReturn.add(df.format(awayTeamAverageShot));
		toReturn.add(df.format(homeTeamAverageShot-awayTeamAverageShot));
		toReturn.add(df.format(homeTeamAverageShot-homeTeamAverageReceived));
		toReturn.add(df.format(awayTeamAverageShot-awayTeamAverageReceived));
		toReturn.add(df.format((homeTeamAverageShot-homeTeamAverageReceived)-(awayTeamAverageShot-awayTeamAverageReceived)));
		return toReturn;
	}
	
	private  double averageGoalsShotLastNGames(String teamName, int n, Match currentMatch, Teams teams) {
		Team myTeam = teams.getTeam(teamName);
		double goals = 0;
		List<Match> lastNMatches = myTeam.getNPreviousMatches(n, currentMatch.getDate());
		for (Match m: lastNMatches) {
			if (teamName.equals(m.homeTeam)) goals += m.homeScore;
			else goals += m.awayScore;
		}
		if (lastNMatches.size()==0) return 0d;
		else return goals / lastNMatches.size();
	}

	private  double averageGoalsReceivedLastNGames(String teamName, int n, Match currentMatch, Teams teams) {
		Team myTeam = teams.getTeam(teamName);
		double goals = 0;
		List<Match> lastNMatches = myTeam.getNPreviousMatches(n, currentMatch.getDate());
		for (Match m: lastNMatches) {
			if (teamName.equals(m.homeTeam)) goals += m.awayScore;
			else goals += m.homeScore;
		}
		if (lastNMatches.size()==0) return 0d;
		else return goals / lastNMatches.size();
	}

	
}
