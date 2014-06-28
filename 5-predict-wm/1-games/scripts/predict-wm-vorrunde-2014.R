wd <- getwd()
setwd("../../4-train-model")
source("scripts/prepare-soccer-data.R")
setwd(wd)
library(caret)

load(file="../../4-train-model/output/trained-random-forest-models.RData")

wm2014 <- subset(data, b_tournament_name=="WM" & b_tournament_year=="2014" & b_tournament_phase=="Endrunde")
wm2014_group <- subset(wm2014, b_tournament_group != "")

prediction_prob <- predict(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted, 
  newdata=wm2014_group,
  type="prob")
prediction_raw <- predict(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted, 
  newdata=wm2014_group,
  type="raw")

result <- cbind(
  wm2014_group[,c("b_team_home","b_team_away")], 
  prediction_prob[c("HOME_WIN","AWAY_WIN")],
  predicted_class=prediction_raw
)

print("Vorhersage für die Vorrunde")
print("===========================")
print(result);
