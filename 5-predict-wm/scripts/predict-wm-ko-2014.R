source("../4-train-model/scripts/prepare-soccer-data.R")
library(caret)

load(file="../4-train-model/output/trained-random-forest-models.RData")

ko_teams <- c("BRA","MEX","NLD","CHL","COL","GRC", "CRI","URY","FRA","CHE","ARG","NGA","DEU","USA","BEL","DZA")

wm2014 <- subset(
  data, 
  b_tournament_name=="WM" & 
    b_tournament_year=="2014" & 
    b_tournament_phase=="Endrunde")
wm2014_ko <- subset(
  wm2014, 
  b_tournament_group == "" & 
    b_team_home %in% ko_teams & 
    b_team_away %in% ko_teams)

predict_ko <- function(model) {
  prediction <- predict(
    model,
    newdata=wm2014_ko,
    type="prob",
    na.action=na.fail)
  result <- cbind(
    wm2014_ko[,c("b_team_home","b_team_away")], 
    prediction[c("HOME_WIN","AWAY_WIN")])
  
  result[result$b_team_home == result$b_team_away, "HOME_WIN"] <- 0.5
  result[result$b_team_home == result$b_team_away, "AWAY_WIN"] <- 0.5
  return(result)
}

dists <- function(model, sorted=FALSE) {
  result <- predict_ko(model)
  teams <- unique(wm2014_ko$b_team_home)
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
    teams_sorted <- sort(best_teams)
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
            col.regions=colorRampPalette(c("red","white","blue"))(50), 
            xlab="", ylab="", 
            scales=list(x=list(rot=90)),
            at=seq(0.5-max_rounded, 0.5+max_rounded, length.out=50))
}

prediction_em_wm <- predict_ko(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted)
write.csv2(prediction_em_wm,"output/game-predictions-ko-emwm.csv")

prediction_all <- predict_ko(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted)
write.csv2(prediction_em_wm,"output/game-predictions-ko-allgames.csv")

dist_em_wm <- dists(
  test_results$em_and_wm_with_qualification.all.notdiff.randomForest$fitted,
  TRUE)
dist_em_wm_mean <- (dist_em_wm + (1-t(dist_em_wm))) / 2

dist_allgames <- dists(
  test_results$all_since_1994.all.notdiff.randomForest$fitted)
dist_allgames <- dist_allgames[colnames(dist_em_wm),colnames(dist_em_wm)]
dist_allgames_mean <- (dist_allgames + (1-t(dist_allgames)))/2

png("output/winprob-heatmap-ko-emwm.png", width=900,height=800)
wm_heatmap(dist_em_wm)
dev.off()

png("output/winprob-heatmap-ko-emwm-mean.png", width=900,height=800)
wm_heatmap(dist_em_wm_mean)
dev.off()

png("output/winprob-heatmap-ko-allgames.png", width=900,height=800)
wm_heatmap(dist_allgames)
dev.off()

png("output/winprob-heatmap-ko-allgames-mean.png", width=900,height=800)
wm_heatmap(dist_allgames_mean)
dev.off()

png("output/winprob-diff-ko-allgames-minus-emwm.png", width=900,height=800)
wm_heatmap(dist_allgames - dist_em_wm)
dev.off()
