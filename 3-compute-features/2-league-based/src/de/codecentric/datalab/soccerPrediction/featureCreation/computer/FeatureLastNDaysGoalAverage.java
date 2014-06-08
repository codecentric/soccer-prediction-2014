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

public class FeatureLastNDaysGoalAverage implements FeatureComputer{
	
	private static final long MS_IN_DAY = 1000*60*60*24;
	
	private long durationInMilliSeconds;
	private int days;

	public FeatureLastNDaysGoalAverage(int days) {
		this.days = days;
		durationInMilliSeconds = days*MS_IN_DAY;
	}

    @Override
    public List<String> getColumnNames() {
        List<String> toReturn = new ArrayList<>();
        String prefix = "b_last_" + days + "days_goal_average_";
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
		for (String s:getColumnNames()) buffer.append(s + ", ");
		buffer.append("   Computes the averages from the games in the past " + days + " days. Based on the goals" +
                "   shot and goal difference between the teams  " +
                "   i.e. this can be negative if the competing teams shot more goals." +
                "   For convenience also the difference between these scores for home and away team are included" +
                "   For convenience 0 is written instead of NaN when games happened in the specified period.");
		return buffer.toString();
	}

	@Override
	public List<String> getValues(Match match, Matches matches, Teams teams, LeagueTables leagueTables) {
		double homeTeamAverageShot = averageGoalsShotLastNGames(match.homeTeam, durationInMilliSeconds, match, teams);
		double awayTeamAverageShot = averageGoalsShotLastNGames(match.awayTeam, durationInMilliSeconds, match, teams);
		double homeTeamAverageReceived = averageGoalsReceivedLastNGames(match.homeTeam, durationInMilliSeconds, match, teams);
		double awayTeamAverageReceived = averageGoalsReceivedLastNGames(match.awayTeam, durationInMilliSeconds, match, teams);
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
	
	private  double averageGoalsShotLastNGames(String teamName, long duration, Match currentMatch, Teams teams) {
		Team myTeam = teams.getTeam(teamName);
		double goals = 0;
		List<Match> lastNMatches = myTeam.getPreviousMatches(currentMatch.getDate(),duration);
		for (Match m: lastNMatches) {
			if (teamName.equals(m.homeTeam)) goals += m.homeScore;
			else goals += m.awayScore;
		}
		if (lastNMatches.size()==0) return 0d;
		else return goals / lastNMatches.size();
	}

	private  double averageGoalsReceivedLastNGames(String teamName, long duration, Match currentMatch, Teams teams) {
		Team myTeam = teams.getTeam(teamName);
		double goals = 0;
		List<Match> lastNMatches = myTeam.getPreviousMatches(currentMatch.getDate(), duration);
		for (Match m: lastNMatches) {
			if (teamName.equals(m.homeTeam)) goals += m.awayScore;
			else goals += m.homeScore;
		}
		if (lastNMatches.size()==0) return 0d;
		else return goals / lastNMatches.size();
	}

}
