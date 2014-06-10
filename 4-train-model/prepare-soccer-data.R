data <- read.csv2("../3-compute-features/3-graph-based/output/games-with-graph-features.csv", colClasses="character")

data$b_date <- as.Date(data$b_date)

cols <- colnames(data)

match_cols <- function(patterns, colnames=cols) {
  c(sapply(patterns, function(pattern) {
    colnames[sapply(colnames, function(col) { 
      grepl(pattern, col) 
    })]
  }),recursive=TRUE)
}
all_features <- match_cols(c("fifa","last","league","graph"))
graph_features <- match_cols(c("graph"),all_features)
notgraph_features <- setdiff(all_features, graph_features)
diff_features <- match_cols(c("diff","minus"),notgraph_features)
notdiff_features <- setdiff(notgraph_features, diff_features)

for (col in all_features) {
  data[[col]] <- as.numeric(data[[col]])
}

data$r_game_outcome_before_penalties <- as.factor(data$r_game_outcome_before_penalties) 

data$r_game_outcome_before_penalties_num <- as.numeric(
  ifelse(data$r_game_outcome_before_penalties == "HOME_WIN", 1,
         ifelse(data$r_game_outcome_before_penalties == "AWAY_WIN", 0, 0.5)))

data$r_game_outcome_before_penalties_winloose <- as.factor(
  ifelse(data$r_game_outcome_before_penalties == "HOME_WIN", "HOME_WIN",
         ifelse(data$r_game_outcome_before_penalties == "AWAY_WIN", "AWAY_WIN", NA)))

data$r_game_outcome_before_penalties_draw <- (
  ifelse(data$r_game_outcome_before_penalties == "HOME_WIN", FALSE,
         ifelse(data$r_game_outcome_before_penalties == "AWAY_WIN", FALSE, TRUE)))

data$r_game_outcome_after_penalties <- as.factor( 
  ifelse(data$r_goals_final_home>data$r_goals_final_away, "HOME_WIN",
         ifelse(data$r_goals_final_home<data$r_goals_final_away, "AWAY_WIN", "DRAW")))

data$r_game_outcome_after_penalties_num <- as.numeric(
  ifelse(data$r_game_outcome_after_penalties == "HOME_WIN", 1,
         ifelse(data$r_game_outcome_after_penalties == "AWAY_WIN", 0, 0.5)))

data$r_game_outcome_after_penalties_num <- as.factor(
  ifelse(data$r_game_outcome_after_penalties == "HOME_WIN", "HOME_WIN",
         ifelse(data$r_game_outcome_after_penalties == "AWAY_WIN", "AWAY_WIN", NA)))

data$r_game_outcome_after_penalties_draw <- as.factor(
  ifelse(data$r_game_outcome_after_penalties == "HOME_WIN", FALSE,
         ifelse(data$r_game_outcome_after_penalties == "AWAY_WIN", FALSE, TRUE)))

possible_targets=c("r_game_outcome_before_penalties",
                   "r_game_outcome_before_penalties_num",
                   "r_game_outcome_before_penalties_winloose",
                   "r_game_outcome_before_penalties_draw",
                   "r_game_outcome_after_penalties",
                   "r_game_outcome_after_penalties_num"
                   "r_game_outcome_after_penalties_winloose",
                   "r_game_outcome_after_penalties_draw",)

build_soccer_data <- function(em_or_wm="both", since="1994-01-01", include_qualification=TRUE, features="all") {
  thedata <- data
  em_or_wm <- toupper(em_or_wm)
  if (em_or_wm == "BOTH") {
    thedata <- thedata[thedata$b_tournament_name %in% c("EM","WM"),]
  } else {
    if (em_or_wm != "EM" && em_or_wm != "WM") {
      stop(sprintf("Invalid argument em_or_wm=%s. Possible options: BOTH, EM or WM.", em_or_wm))
    }
    thedata <- thedata[thedata$b_tournament_name == em_or_wm,]
  }
  sinceDate <- as.Date(since)
  thedata <- thedata[thedata$b_date > sinceDate,]
  if (! include_qualification) {
    thedata <- thedata[thedata$b_tournament_phase == "Endrunde",]
  }
  if (length(features) == 1 && features == "all") {
    features <- all_features
  }
  return(thedata[,c("b_date", "b_team_home","b_team_away", possible_targets, features)])
}

build_formula <- function(target, features="all") {
  if (length(features) == 1 && features == "all") {
    features <- all_features
  }
  formula_txt <- paste(target, paste(features, collapse=" + "), sep=" ~ ")
  formula <- as.formula(formula_txt)
  return(formula)
}

transform_winloose <- function(data, outcomecol="r_game_outcome_after_penalties") {
  data <- data[data[[outcomecol]] != "DRAW",]
  data[[outcomecol]] <- as.factor(as.character(data[[outcomecol]]))
  return(data)
}

transform_draw <- function(data, outcomecol="r_game_outcome_after_penalties") {
  data[[outcomecol]] <- ifelse(data[[outcomecol]] == "DRAW","DRAW","NOT_DRAW")
  data[[outcomecol]] <- as.factor(as.character(data[[outcomecol]]))
  return(data)
}

library(rpart)
library(randomForest)
set.seed(24347) 

#caret funktioniert nicht :()
splitdf <- function(dataframe, seed=NULL) {
  if (!is.null(seed)) set.seed(seed)
  index <- 1:nrow(dataframe)
  trainindex <- sample(index, trunc(length(index)/4)*3)
  trainset <- dataframe[trainindex, ]
  testset <- dataframe[-trainindex, ]
  list(trainset=trainset,testset=testset)
}

thedata <- build_soccer_data(features=diff_features)
parts <- splitdf(thedata)
traindata <- parts$trainset
testdata <- parts$testset
formula <- build_formula("r_game_outcome_after_penalties",diff_features)
fit <- randomForest(formula, data=traindata)
pr <- predict(fit,testdata, type="prob")
result <- as.data.frame(pr)
result$actual <- testdata$r_game_outcome_after_penalties
result$predicted = ifelse(
  result$HOME_WIN > result$AWAY_WIN & result$HOME_WIN > result$DRAW, "HOME_WIN",
  ifelse(result$AWAY_WIN > result$HOME_WIN & result$AWAY_WIN > result$DRAW, "AWAY_WIN", "DRAW"))
result$correct <- result$actual == result$predicted
View(result)
table(result[, c("predicted","actual")])
table(result$correct)

thedata <- build_soccer_data(features=diff_features)
formula <- build_formula("r_game_outcome_after_penalties",diff_features)
thedata <- thedata[thedata$r_game_outcome_after_penalties != "DRAW",]
thedata$r_game_outcome_after_penalties <- as.factor(as.character(thedata$r_game_outcome_after_penalties))
fit1 <- randomForest(formula, data=thedata)

thedata <- build_soccer_data(features=notdiff_features)
formula <- build_formula("r_game_outcome_after_penalties",notdiff_features)
thedata$r_game_outcome_after_penalties <- ifelse(thedata$r_game_outcome_after_penalties == "DRAW","DRAW","NOT_DRAW")
thedata$r_game_outcome_after_penalties <- as.factor(as.character(thedata$r_game_outcome_after_penalties))
fit2 <- randomForest(formula, data=thedata)


library(neuralnet)

thedata <- build_soccer_data(features=notdiff_features)
formula <- build_formula("r_game_outcome_after_penalties_num",notdiff_features)
thedata$r_game_outcome_after_penalties <- ifelse(thedata$r_game_outcome_after_penalties == "DRAW","DRAW","NOT_DRAW")
thedata$r_game_outcome_after_penalties <- as.factor(as.character(thedata$r_game_outcome_after_penalties))
nn <- neuralnet(r_game_outcome_after_penalties_num ~ 
                  b_fifa_ranking_difference +
                  b_league_positions_points_league_table_home_minus_away_team +
                  b_last_360days_goal_average_difference_home_minus_away_team,
                thedata, hidden=c(5))


library(nnet)
thedata <- build_soccer_data(features=notdiff_features)
thedata$r_game_outcome_after_penalties <- ifelse(thedata$r_game_outcome_after_penalties == "DRAW","DRAW","NOT_DRAW")
thedata$r_game_outcome_after_penalties <- as.factor(as.character(thedata$r_game_outcome_after_penalties))
formula <- build_formula("r_game_outcome_after_penalties",notdiff_features)
parts <- splitdf(thedata)
traindata <- parts$trainset
testdata <- parts$testset
nn.fit <- nnet(formula,
               traindata, 
               size=25,
               maxit=2000)
result <- data.frame(
  predicted=predict(nn.fit,newdata=testdata,type="class"),
  actual=testdata$r_game_outcome_after_penalties)
#result$predicted = ifelse(
#  result$HOME_WIN > result$AWAY_WIN & result$HOME_WIN > result$DRAW, "HOME_WIN",
#  ifelse(result$AWAY_WIN > result$HOME_WIN & result$AWAY_WIN > result$DRAW, "AWAY_WIN", "DRAW"))
result$correct <- result$actual == result$predicted
table(result$correct)
table(result[,c("predicted","actual")])
View(result)
?nnet

library(ggplot2)
ggplot(thedata, aes(b_date, ..count..)) + 
  geom_histogram(binwidth=365, aes(fill=r_game_outcome_after_penalties)) +
  scale_fill_discrete("Ergebnis") +
  scale_y_continuous("Anzahl Spiele", formatter=comma) +
  scale_x_date("Datum")
write.csv(thedata,"/tmp/test.csv")
