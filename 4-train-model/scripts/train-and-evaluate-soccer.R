source("scripts/train-and-evaluate-runner.R")
source("scripts/prepare-soccer-data.R")
source("scripts/multiplot.R")
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

plots <- lapply(test_results,function(test_result) {
  ggplot(test_result$fitted) +  
    ggtitle(test_result$title) +
    geom_hline(
      yintercept=test_result$confusionMatrix$overall["Accuracy"],
      color="red")
})

png("output/wm-2010-model-test.png", width=1600,height=900)
multiplot(plotlist=plots, cols=3)
dev.off()
