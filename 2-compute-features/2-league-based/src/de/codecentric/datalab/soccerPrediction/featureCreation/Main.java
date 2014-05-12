package de.codecentric.datalab.soccerPrediction.featureCreation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.codecentric.datalab.soccerPrediction.featureCreation.computer.*;


public class Main {
 
	public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        LeagueTables leagueTables = new LeagueTables();

        List<FeatureComputer> featureComputers = getFeatureComputers(leagueTables);

        String inputFileName = "../1-fifa-ranking/output/games.csv";
        String outputCsvFileName = "output/soccerDataWithFeatures.csv";
        String outputDescriptionFileName = "output/featureDescriptions.txt";

        if (args.length >= 3) {
            inputFileName = args[0];
            outputCsvFileName = args[1];
            outputDescriptionFileName = args[2];
        }

        Matches matches = new Matches(leagueTables);
        matches.loadFromFile(inputFileName);
        matches.computeFeatures(featureComputers);
        matches.writeToFile(outputCsvFileName);

        printComputerDescriptions(featureComputers, outputDescriptionFileName);
		
	}

    private static List<FeatureComputer> getFeatureComputers(LeagueTables leagueTables) {
        List<FeatureComputer> featureComputers = new ArrayList<>();
        featureComputers.add(new FeatureGameOutcome());
        featureComputers.add(new FeatureLastNGamesPointsAverageWeighted(1, leagueTables.getLeagueTable("weighted_points_league_table")));
        featureComputers.add(new FeatureLastNGamesPointsAverageWeighted(3, leagueTables.getLeagueTable("weighted_points_league_table")));
        featureComputers.add(new FeatureLastNGamesPointsAverageWeighted(10, leagueTables.getLeagueTable("weighted_points_league_table")));
        featureComputers.add(new FeatureLastNGamesGoalAverageWeighted(1, leagueTables.getLeagueTable("weighted_points_league_table")));
        featureComputers.add(new FeatureLastNGamesGoalAverageWeighted(3, leagueTables.getLeagueTable("weighted_points_league_table")));
        featureComputers.add(new FeatureLastNGamesGoalAverageWeighted(10, leagueTables.getLeagueTable("weighted_points_league_table")));
        featureComputers.add(new FeatureLastNGamesGoalAverage(1));
        featureComputers.add(new FeatureLastNGamesGoalAverage(3));
        featureComputers.add(new FeatureLastNGamesGoalAverage(10));
        featureComputers.add(new FeaturePrintLeaguePositions(leagueTables.getLeagueTable("weighted_points_league_table")));
        featureComputers.add(new FeaturePrintLeaguePositions(leagueTables.getLeagueTable("points_league_table")));
        featureComputers.add(new FeatureLastNDaysGoalAverage(30));
        featureComputers.add(new FeatureLastNDaysGoalAverage(120));
        featureComputers.add(new FeatureLastNDaysGoalAverage(360));
        return featureComputers;
    }

    private static void printComputerDescriptions(List<FeatureComputer> featureComputers, String outputFileName) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(outputFileName));
		for (FeatureComputer fc: featureComputers) {
			writer.println(fc.getDescription());
			writer.println();
			writer.println();
		}
		writer.close();
		
		
	}
	
}
