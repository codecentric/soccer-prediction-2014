library(caret)

data_with_results <- read.csv2("input/wm-2014-vorrunde-with-result.csv", colClasses="character")
data_with_fifa_ranking <- read.csv2("../3-compute-features/3-graph-based/output/games-with-graph-features.csv")
allgames <- subset(data_with_fifa_ranking, 
                   b_date == "2014-07-13") 

for (i in 1:nrow(data_with_results)) {
  home <- data_with_results[i,"b_team_home"]
  away <- data_with_results[i,"b_team_away"]
  game <- subset(allgames, b_team_home == home & b_team_away == away)
  
  if (game$b_fifa_ranking_home[1] < game$b_fifa_ranking_away[1]) {
    data_with_results[i,"predicted"] <- "HOME_WIN"
  } else {
    data_with_results[i, "predicted"] <- "AWAY_WIN"
  }  
}

full_result <- cbind(
  data_with_results[,c("b_date","b_team_home", "b_team_away")],
  predicted_class=factor(data_with_results$predicted, 
                           levels=c("AWAY_WIN","DRAW","HOME_WIN")),
  actual_class=factor(data_with_results$r_game_outcome_before_penalties, 
                        levels=c("AWAY_WIN","DRAW","HOME_WIN")))

print("CONFUSION MATRIX FOR SIMPLE FIFA-RANKING BASED PREDICTOR")
print("========================================================")
full_result
confusionMatrix(full_result$predicted_class, 
                full_result$actual_class)