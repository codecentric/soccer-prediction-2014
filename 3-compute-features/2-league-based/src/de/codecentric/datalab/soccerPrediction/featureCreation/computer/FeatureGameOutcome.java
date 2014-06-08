package de.codecentric.datalab.soccerPrediction.featureCreation.computer;

import de.codecentric.datalab.soccerPrediction.featureCreation.*;

import java.util.List;

import static java.util.Arrays.asList;

public class FeatureGameOutcome implements FeatureComputer {
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<String> getColumnNames() {
        return asList("r_game_outcome_before_penalties", "r_game_outcome_before_penalties_as_int");
    }

    @Override
    public List<String> getValues(Match match, Matches matches, Teams teams, LeagueTables leagueTables) {
        String asEnum = match.matchClass.toString();
        return asList(asEnum, "" + match.matchClass.getAsInt());
    }
}
