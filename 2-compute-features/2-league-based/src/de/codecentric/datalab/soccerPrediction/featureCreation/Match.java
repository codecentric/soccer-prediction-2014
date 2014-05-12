package de.codecentric.datalab.soccerPrediction.featureCreation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Match {

	private static final SimpleDateFormat INPUT_DATE_FORMATTER  = new SimpleDateFormat("yyyy-MM-dd");

    private final String[] originalValues;
    public final String homeTeam;
    public final String awayTeam;
    private final Date date;
    public final int homeScore;
    public final int awayScore;
    public final MatchClass matchClass;

	private final List<String> features = new ArrayList<>();

    public enum MatchClass {
        HOME_WIN(1), DRAW(0), AWAY_WIN(-1);
        private final int asInt;

        MatchClass(int asInt) {
            this.asInt = asInt;
        }

        public int getAsInt() {
            return asInt;
        }
    }

    public Match(String values[], Map<String, String> map) throws ParseException {
        originalValues = values;
        date = INPUT_DATE_FORMATTER.parse(map.get("b_date"));
        homeTeam = map.get("b_team_home");
        awayTeam = map.get("b_team_away");
        homeScore = Integer.parseInt(map.get("r_goals_before_penalties_home"));
        awayScore = Integer.parseInt(map.get("r_goals_before_penalties_away"));
        if (homeScore > awayScore) matchClass = MatchClass.HOME_WIN;
        else if (homeScore == awayScore) matchClass = MatchClass.DRAW;
        else matchClass = MatchClass.AWAY_WIN;
    }

    public void addFeatureValues(List<String> values) {
		features.addAll(values);
	}

    public Collection<? extends String> getExtraValues() {
        return features;
    }

    public String[] getOriginalValues() {
        return originalValues;
    }

    public Date getDate() {
		return date;
	}

}
