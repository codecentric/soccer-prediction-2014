package de.codecentric.datalab.soccerPrediction.featureCreation;

import java.util.List;

public interface FeatureComputer {

	public String getDescription();
		
	public List<String> getColumnNames(); 
	
	public List<String> getValues(Match match, Matches matches, Teams teams, LeagueTables leagueTables);


}
