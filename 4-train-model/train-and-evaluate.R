source("prepare-soccer-data.R")

datasets = list(
  c(1,2,3),
  c(4,5,6)
)

featuresets = list(

)

targets = list(

)

splits <- list(
  wm_em=function(dataset) {
    list(
      testdata=dataset[1],
      traindata=dataset[2:])
  }
)

models <- list(
  list(
    method="rf",
    tuneGrid=expand.grid(mtry=c(1,2,3,5,7,9)),
    trControl=NULL
  )
)

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

configurations <- Reduce(x=entries(datasets), init=list(), f=function(dataset, acc) {
  Reduce(x=entries(splits), init=acc, f=function(split, acc) {
    # if splits are random, make sure every split-function is only executed once for a single dataset
    # we don't want to compare different models for the same dataset and same split-functions but actually different splits
    spitData <- split.value(dataset.value)
    Reduce(x=entries(featuresets), init=acc, f=function(featureset, acc) {
      Reduce(x=entries(targets), init=acc, f=function(target,acc) {
        Reduce(x=entries(models), init=acc, f=function(model, acc) {
          append(acc, 
            append(
              list(
                datasetName=dataset$name
                dataset=dataset$value
                splitName=split$name
              ),
              # splitData has two elements: "trainData" and "testData
              splitData,
              list(
                featuresetName=featureset$name,
                featureset=featureset$value,
                targetName=target$name,
                target=target$value,
                formula=build_formula(target, featureset)
                modelName=model$name,
                modelConfiguration=model$value
              )
          ))
        })
      })
    })
  })
})

run_train <- function(configuration) {
  fitted <- train(
    configuration$formula,
    data=configuration$trainData,
    method=configuration$modelConfiguration$method,
    trControl=configuration$modelConfiguration$trControl,
    tuneGrid=configuration$modelConfiguration$tuneGrid
  )
  append(
    configuration,
    fitted=fitted)
}

run_evaluate <- function(configuration) {
  prediction <- predict(
    configuration$fitted, 
    newdata=configuration$testData,
    type="prob")
  append(
    configuration,
    prediction=prediction)
}

train_results <- lapply(configurations, run_train)
test_results <- lapply(train_results, run_train)