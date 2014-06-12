source("scripts/multiplot.R")
library(caret)

load(file="output/trained-random-forest-models.RData")

plots <- lapply(test_results,function(test_result) {
  #TODO: Parameter-Plot mit dem "all"-model, wm-2010-accuracy-plot mit dem "pre-2014"-model
  ggplot(test_result$fitted) +  
    ggtitle(test_result$title) +
    coord_cartesian(ylim=c(.5,.8)) +
    geom_hline(
      yintercept=test_result$confusionMatrix$overall["Accuracy"],
      color="red")
})

png("output/random-forest-models-comparison.png", width=1600,height=900)
multiplot(plotlist=plots, cols=3)
dev.off()
