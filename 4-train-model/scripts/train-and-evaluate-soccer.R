source("scripts/train-and-evaluate-runner.R")
source("scripts/prepare-soccer-data.R")
library(caret)


trControl <- trainControl(
  method="repeatedcv",
  number=5,
  repeats=1)
soccer.models <- list(
  randomForest=list(
    method="rf",
    tuneGrid=expand.grid(mtry=c(1,2,3,5,7)),
    trControl=trControl
  )
  #,
  #stochasticGradientBoosting=list(
  #  method="gbm",
  #  tuneGrid=expand.grid(n.trees=(1:400)*4,interaction.depth=c(1,3,6,11,15),shrinkage=0.1),
  #  trControl=trControl)
)

configurations = build_run_configurations(
  soccer.datasets,
  soccer.splits,
  soccer.featuresets,
  soccer.targets,
  soccer.models)
train_results <- lapply(configurations, run_train)
test_results <- lapply(train_results, run_evaluate)

save(test_results, file="output/trained-random-forest-models.RData")

