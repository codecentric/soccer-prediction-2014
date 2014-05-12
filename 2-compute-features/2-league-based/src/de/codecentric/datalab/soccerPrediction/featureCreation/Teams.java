package de.codecentric.datalab.soccerPrediction.featureCreation;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage data about all teams. 
 * @author valentin
 *
 */
public class Teams {

	private Map<String, Team> teams = new HashMap<>();

    public Teams(Matches matches) {
        for (Match m : matches) {
            addMatchToTeam(m.homeTeam, m);
            addMatchToTeam(m.awayTeam, m);
        }
    }

    private void addMatchToTeam(String teamName, Match match) {
		Team team = teams.get(teamName);
		if (team == null) team = new Team(teamName);
		team.addMatch(match);
		teams.put(teamName, team);
	}
	
	public Team getTeam(String teamName) {
		return teams.get(teamName);
	}
	
}
