source("scripts/train-and-evaluate-runner.R")
source("scripts/prepare-soccer-data.R")
library(caret)


trControl <- trainControl(
  method="repeatedcv",
  number=10,
  repeats=2)
soccer.models <- list(
  #logisticRegression = list(
  #  method="glm",
  #  trControl=trainControl(
  #    method="repeatedcv",
  #    number=10,
  #    repeats=2,
  #    selectionFunction="oneSE"),
  #  tuneGrid=NULL
  #)
  #,
  randomForest=list(
    method="rf",
    tuneGrid=expand.grid(mtry=c(1,2,3,5,7)),
    trControl=trControl
  )
  #,
  #stochasticGradientBoosting=list(
  #  method="gbm",
  #  tuneGrid=expand.grid(n.trees=(1:5)*100,interaction.depth=c(1,2,3,4,5),shrinkage=0.1),
  #  trControl=trControl)
)

configurations = build_run_configurations(
  soccer.datasets,
  soccer.splits,
  soccer.featuresets,
  soccer.targets,
  soccer.models)
train_results <- lapply(configurations, run_train)
save(train_results, file="output/trained-models.RData")

test_results <- lapply(train_results, run_evaluate)
save(test_results, file="output/tested-models.RData")