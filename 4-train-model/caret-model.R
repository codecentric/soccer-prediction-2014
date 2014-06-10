source("prepare-soccer-data.R")

data.diff <- build_soccer_data(features=diff_features)
formula.diff <- build_formula("r_game_outcome_after_penalties", features=diff_features)

data.diff.winloose <- transform_winloose(data.diff)
data.diff.draw <- transform_draw(data.diff)


library(caret)

customTrain <- function(data, f, method,tuneGrid=NULL) {
  fitControl <- trainControl(method="repeatedcv",number=5,repeats=5)
  train(formula.diff,
        data=data,
        method=method,
        tuneGrid=tuneGrid,
        trControl=fitControl)
}

buildModel <- function(data, f, method, tuneGrid=NULL) {
  inTraining <- createDataPartition(
    data$r_game_outcome_before_penalties, 
    p=0.75, list=FALSE)
  training <- data[inTraining,]
  test <- data[-inTraining,]
  fit <- customTrain(data,f,method,tuneGrid)
  fit
}

table(predict(svm$fit,data.diff.draw))

gbm <- buildModel(data.diff.draw, formula.diff, "gbm")
rbf <- buildModel(data.diff.draw, formula.diff, "rbf")

trellis.par.set(caretTheme())
plot(gbm.fit)
plot(nn.fit)

predict(gbm.fit, newdata=)