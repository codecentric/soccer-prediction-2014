library(caret)
data <- read.csv2("../3-compute-features/3-graph-based/output/games-with-graph-features.csv", colClasses="character")

data$b_date <- as.Date(data$b_date)
data$r_goals_before_penalties_home <- as.numeric(data$r_goals_before_penalties_home)
data$r_goals_before_penalties_away <- as.numeric(data$r_goals_before_penalties_away)
data$r_goals_final_home <- as.numeric(data$r_goals_final_home)
data$r_goals_final_away <- as.numeric(data$r_goals_final_away)

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

possible_targets <- c(
  "r_game_outcome_before_penalties",
  "r_game_outcome_before_penalties_num",
  "r_game_outcome_before_penalties_winloose",
  "r_game_outcome_before_penalties_draw",
  "r_game_outcome_after_penalties",
  "r_game_outcome_after_penalties_num",
  "r_game_outcome_after_penalties_winloose",
  "r_game_outcome_after_penalties_draw")

for (possible_target in possible_targets) {
  data[[possible_target]] <- factor(as.character(data[[possible_target]]), exclude=c(""))
}

build_soccer_data <- function(tournaments=NULL, since=NULL, include_qualification=TRUE, features="all") {
  thedata <- data
  if (! is.null(tournaments)) {
    thedata <- thedata[thedata$b_tournament_name %in% tournaments,]
  }
  if (! is.null(since)) {
    sinceDate <- as.Date(since)
    thedata <- thedata[thedata$b_date > sinceDate,]
  }
  if (! include_qualification) {
    thedata <- thedata[thedata$b_tournament_phase == "Endrunde",]
  }
  if (length(features) == 1 && features == "all") {
    features <- all_features
  }
  thedata <- subset(thedata, b_date < as.Date("2014-06-12"))
  return(thedata[,c("b_date", "b_team_home","b_team_away", "b_tournament_name", "b_tournament_phase", "b_tournament_year", "b_tournament_group", possible_targets, features)])
}

soccer.datasets <- list(
  #all=data,
  all_since_1994=build_soccer_data(since="1994-01-01"),
  em_and_wm_final_rounds=build_soccer_data(tournaments=c("EM","WM"),since="1994-01-01",include_qualification=FALSE),
  em_and_wm_with_qualification=build_soccer_data(tournaments=c("EM","WM"),since="1994-01-01",include_qualification=TRUE)
)


soccer.splits <- list(
  #random=function(dataset, target) {
  #  trainIndex=createDataPartition(dataset[[target]], p=.8,list=FALSE, times=1)
  #  list(
  #    trainData=dataset[trainIndex,],
  #    testData=dataset[-trainIndex,])
  #},
  all=function(dataset, target) {
    list(
      trainData=dataset,
      testData=dataset[c(),]
    )
  }
  #,
  #wm2010=function(dataset, target) {
  #  list(
  #    trainData=dataset[dataset$b_date < as.Date("2010-06-11"),],
  #    testData=dataset[dataset$b_tournament_name == "WM" & dataset$b_tournament_year=="2010",])
  #}
  #,
  #em2012=function(dataset, target) {
  #  list(
  #    trainData=dataset[dataset$b_date < as.Date("2012-06-12"),],
  #    testData=dataset[dataset$b_tournament_name == "EM" & dataset$b_tournament_year=="2012",])
  #}
)

soccer.featuresets <- list(
  #all=all_features,
  #graph=graph_features,
  notgraph=notgraph_features,
  diff=diff_features,
  notdiff=notdiff_features
  #selected=c("b_fifa_ranking_home","b_fifa_ranking_away","b_last_3_games_goal_average_home_team")
)

soccer.targets <- list(
  #"r_game_outcome_before_penalties",
  #"r_game_outcome_before_penalties_num",
  #"r_game_outcome_before_penalties_winloose",
  #"r_game_outcome_before_penalties_draw",
  #"r_game_outcome_after_penalties",
  #"r_game_outcome_after_penalties_num",
  winloose="r_game_outcome_after_penalties_winloose"
  #draw="r_game_outcome_after_penalties_draw"
)