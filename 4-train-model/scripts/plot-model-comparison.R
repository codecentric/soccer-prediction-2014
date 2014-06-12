source("scripts/prepare-soccer-data.R")
source("scripts/multiplot.R")
library(caret)

load(file="output/trained-random-forest-models.RData")

plots <- list()
for (datasetName in names(soccer.datasets)) {
  for (featuresetName in names(soccer.featuresets)) {
    allModel = test_results[[paste(datasetName, ".all.",featuresetName,".randomForest", sep="")]]
    wm2010Model = test_results[[paste(datasetName, ".wm2010.",featuresetName,".randomForest", sep="")]]
    plots <- append(plots, list(ggplot(allModel$fitted) +  
      ggtitle(allModel$title) +
      coord_cartesian(ylim=c(.5,.8)) +
      geom_hline(
        yintercept=wm2010Model$confusionMatrix$overall["Accuracy"],
        color="red")))
  }
}

png("output/random-forest-models-comparison.png", width=1600,height=900)
multiplot(plotlist=plots, cols=3)
dev.off()
