source("scripts/train-and-evaluate-runner.R")
library(datasets)
library(caret)

datasets <- list(
  iris=iris
)

featuresets <- list(
  all=c("Sepal.Length","Sepal.Width","Petal.Length","Petal.Width")
)

targets <- list(
  Species="Species"
)

splits <- list(
  half=function(dataset, target) {
    trainIndex=createDataPartition(dataset[[target]], p=.5,list=FALSE, times=1)
    list(
      trainData=dataset[trainIndex,],
      testData=dataset[-trainIndex,])
  }
)

trControl <- trainControl(
  method="repeatedcv",
  number=5,
  repeats=2)
models <- list(
  randomForest=list(
    method="rf",
    tuneGrid=expand.grid(mtry=c(1,2,3,4)),
    trControl=trControl
  ),
  stocasticGradientBoosting=list(
    method="gbm",
    tuneGrid=expand.grid(n.trees=(1:30)*50,interaction.depth=c(1,2,3,4),shrinkage=0.1),
    trControl=trControl)
)

configurations = build_run_configurations(datasets,splits,featuresets,targets,models)
train_results <- lapply(configurations, run_train)
test_results <- lapply(train_results, run_evaluate)
