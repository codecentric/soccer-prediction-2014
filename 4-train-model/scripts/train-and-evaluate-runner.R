library(caret)

# transform list(foo=bar,...) into list(list(name=foo,value=bar),...) to allow tracing 
# better naming of the runconfigurations
entries <- function(l) {
  mapply(function(name,value) {list(name=name,value=value)}, names(l), l, SIMPLIFY=FALSE)
}

build_formula <- function(target, features) {
  formula_txt <- paste(target, paste(features, collapse=" + "), sep=" ~ ")
  formula <- as.formula(formula_txt)
  return(formula)
}

build_run_configurations <- function(datasets, splits, featuresets, targets, models) {
  Reduce(x=entries(datasets), init=list(), f=function(acc, dataset) {
    Reduce(x=entries(splits), init=acc, f=function(acc,splitFunction) {
      Reduce(x=entries(targets), init=acc, f=function(acc,target) {
        dataset$value <- dataset$value[!is.na(dataset$value[[target$value]]),]
        # if splits are random, make sure every split-function is only executed once for a single dataset
        # we don't want to compare different models for the same dataset and same split-functions but actually different splits
        splitData <- splitFunction$value(dataset$value, target$value)
        Reduce(x=entries(featuresets), init=acc, f=function(acc,featureset) {
          Reduce(x=entries(models), init=acc, f=function(acc,model) {
            title <- paste(dataset$name, splitFunction$name, featureset$name, model$name,sep=".")
            result <- list(
              title=title,
              datasetName=dataset$name,
              dataset=dataset$value,
              splitName=splitFunction$name)
            result <- append(result, splitData)
            result <- append(result, list(
              featuresetName=featureset$name,
              featureset=featureset$value,
              targetName=target$name,
              target=target$value,
              formula=build_formula(target$value, featureset$value),
              modelName=model$name,
              modelConfiguration=model$value))
            resultList <- list(result)
            names(resultList) <- title
            append(acc, resultList)
          })
        })
      })
    })
  })
}

run_train <- function(configuration) {
  message(paste("Training model",configuration$title))
  fitted <- train(
    configuration$formula,
    data=configuration$trainData,
    method=configuration$modelConfiguration$method,
    trControl=configuration$modelConfiguration$trControl,
    tuneGrid=configuration$modelConfiguration$tuneGrid
  )
  append(
    configuration,
    list(fitted=fitted))
}

run_evaluate <- function(configuration) {
  if (nrow(configuration$testData) == 0) {
    return(configuration)
  }
  predictionProb <- predict(
    configuration$fitted, 
    newdata=configuration$testData,
    type="prob")
  predictionClass <- predict(
    configuration$fitted, 
    newdata=configuration$testData)
  confusionMatrix <- confusionMatrix(
    data=predictionClass,
    reference=configuration$testData[[configuration$target]])
  append(
    configuration,
    list(
      predictionProb=predictionProb,
      predictionClass=predictionClass,
      confusionMatrix=confusionMatrix))
}
