source("../4-train-model/scripts/prepare-soccer-data.R")
library(caret)

load(file="../4-train-model/output/trained-random-forest-models.RData")

wm2014 <- subset(data, b_tournament_name=="WM" & b_tournament_year=="2014" & b_tournament_phase=="Endrunde")
wm2014_group <- subset(wm2014, b_tournament_group != "")

prediction_raw <- predict(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted, 
  newdata=wm2014_group,
  type="raw")

data_with_results <- read.csv2("input/wm-2014-vorrunde-with-result.csv", colClasses="character")

result <- cbind(
  wm2014_group[,c("b_date", "b_team_home","b_team_away")], 
  predicted_class=prediction_raw
)
full_result <- merge(result, data_with_results, by=c("b_date","b_team_home","b_team_away"))

towrite <- full_result[c("b_date","b_team_home","b_team_away","predicted_class","r_game_outcome_before_penalties"),]
colnames(towrite) <-c("b_date","b_team_home","b_team_away","predicted_class","actual_class") 
write.csv2(full_result, "output/vorrunde-prediction-with-reference.csv")

full_result$predicted_class <- factor(full_result$predicted_class, levels=c("AWAY_WIN","DRAW","HOME_WIN"))
full_result$actual_class <- factor(full_result$r_game_outcome_before_penalties, levels=c("AWAY_WIN","DRAW","HOME_WIN"))
notmissing <- !is.na(full_result$actual_class)

result_confusion <- confusionMatrix(
  data=full_result$predicted_class[notmissing], 
  reference=full_result$actual_class[notmissing])

print(result_confusion)
