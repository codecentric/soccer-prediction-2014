package de.codecentric.datalab.soccerPrediction.featureCreation.computer;

import java.util.ArrayList;
import java.util.List;

import de.codecentric.datalab.soccerPrediction.featureCreation.FeatureComputer;
import de.codecentric.datalab.soccerPrediction.featureCreation.LeagueTable;
import de.codecentric.datalab.soccerPrediction.featureCreation.LeagueTables;
import de.codecentric.datalab.soccerPrediction.featureCreation.Match;
import de.codecentric.datalab.soccerPrediction.featureCreation.Matches;
import de.codecentric.datalab.soccerPrediction.featureCreation.Teams;

public class FeaturePrintLeaguePositions implements FeatureComputer{

	private LeagueTable leagueTable; 
	
	public FeaturePrintLeaguePositions(LeagueTable leagueTable) {
		this.leagueTable = leagueTable; 
	}
	
	
	@Override
	public String getDescription() {
		StringBuilder buffer = new StringBuilder();
		for (String s:getColumnNames()) buffer.append(s+", ");
		buffer.append("\n   : prints the league positions in the computed league based on the "+leagueTable.getName());
		return buffer.toString();
	}

    @Override
    public List<String> getColumnNames() {
        List<String> toReturn = new ArrayList<>();
        String prefix = "b_league_positions_" + leagueTable.getName() + "_";
        toReturn.add(prefix + "home_team");
        toReturn.add(prefix + "away_team");
        toReturn.add(prefix + "home_minus_away_team");
        return toReturn;
    }

    @Override
	public List<String> getValues(Match match, Matches matches, Teams teams, LeagueTables leagueTables) {
		List<String> toReturn = new ArrayList<>();
		int positionHomeTeam = leagueTable.getLeagueEntry(match.homeTeam).getCurrentPosition();
		int positionAwayTeam = leagueTable.getLeagueEntry(match.awayTeam).getCurrentPosition();
		toReturn.add(""+positionHomeTeam);
		toReturn.add(""+positionAwayTeam);
		toReturn.add(""+(positionHomeTeam-positionAwayTeam));
		return toReturn;
	}

}
