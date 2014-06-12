source("scripts/prepare-soccer-data.R")
library(caret)

load(file="output/trained-random-forest-models.RData")

wm2014 <- subset(data, b_date=="2014-06-12")
prediction = predict(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted, 
  newdata=wm2014,
  type="prob")
result <- cbind(wm2014[,c("b_team_home","b_team_away")], prediction[c("HOME_WIN","AWAY_WIN")])

print(result)
