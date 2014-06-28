wd <- getwd()
setwd("../../4-train-model")
source("scripts/prepare-soccer-data.R")
setwd(wd)

library(caret)

load(file="../../4-train-model/output/trained-random-forest-models.RData")

wm2014 <- subset(data, b_tournament_name=="WM" & b_tournament_year=="2014" & b_tournament_phase=="Endrunde")
wm2014_all <- subset(wm2014, b_tournament_group == "")

predict_all <- function(model) {
  prediction <- predict(
    model,
    newdata=wm2014_all,
    type="prob",
    na.action=na.fail)
  result <- cbind(
    wm2014_all[,c("b_team_home","b_team_away")], 
    prediction[c("HOME_WIN","AWAY_WIN")])
  
  result[result$b_team_home == result$b_team_away, "HOME_WIN"] <- 0.5
  result[result$b_team_home == result$b_team_away, "AWAY_WIN"] <- 0.5
  return(result)
}

dists <- function(model, sorted=FALSE) {
  result <- predict_all(model)
  teams <- unique(wm2014_all$b_team_home)
  # each cell is the probability of the row-team winning against the colum team
  dist_matrix <- matrix(0, nrow=length(teams), ncol=length(teams))
  rownames(dist_matrix) <- teams
  colnames(dist_matrix) <- teams
  for(x in teams) {
    for(y in teams) {
      dist_matrix[x,y] <- subset(result, b_team_home == x & b_team_away==y)$HOME_WIN
    }
  }
  
  if (sorted) {
    # simply add up all winning probabilities
    best_teams <- rowSums(dist_matrix)
    teams_sorted <- sort(best_teams,decreasing=TRUE)
    dist_sorted <- dist_matrix[names(teams_sorted),names(teams_sorted)]
    return(dist_sorted)
  }
  return(dist_matrix)
}

wm_heatmap <- function(data) {
  max_from_middle <- max(max(range(data)) - 0.5, 0.5-min(range(data)))
  max_rounded = ceiling(max_from_middle *10)/10
  # why do I have to transpose the data to see rows in the matrix as rows in the image?
  levelplot(t(data), 
            col.regions=colorRampPalette(c("blue", "white","red"))(50),
            xlab="", ylab="", 
            scales=list(x=list(rot=90)),
            at=seq(0.5-max_rounded, 0.5+max_rounded, length.out=50))
}

prediction_em_wm <- predict_all(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted)
write.csv2(prediction_em_wm,"output/game-predictions-emwm.csv")

prediction_all <- predict_all(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted)
write.csv2(prediction_em_wm,"output/game-predictions-all.csv")

dist_em_wm <- dists(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted,
  TRUE)
dist_em_wm_mean <- (dist_em_wm + (1-t(dist_em_wm))) / 2

dist_all <- dists(
  test_results$all_since_1994.all.notdiff.randomForest$fitted)
dist_all <- dist_all[colnames(dist_em_wm),colnames(dist_em_wm)]
dist_all_mean <- (dist_all + (1-t(dist_all)))/2

png("output/winprob-heatmap-emwm.png", width=900,height=800)
wm_heatmap(dist_em_wm)
dev.off()

png("output/winprob-heatmap-emwm-mean.png", width=900,height=800)
wm_heatmap(dist_em_wm_mean)
dev.off()

png("/tmp/test.png", width=900,height=800)
wm_heatmap(dist_em_wm - (1-t(dist_em_wm)))
dev.off()

png("output/winprob-heatmap-allgames.png", width=900,height=800)
wm_heatmap(dist_all)
dev.off()

png("output/winprob-heatmap-allgames-mean.png", width=900,height=800)
wm_heatmap(dist_all_mean)
dev.off()

png("output/winprob-diff-allgames-minus-emwm.png", width=900,height=800)
wm_heatmap(dist_all - dist_em_wm)
dev.off()
