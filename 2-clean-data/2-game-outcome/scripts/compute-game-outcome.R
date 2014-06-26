data <- read.csv2("../1-tournament-names/output/games.csv", colClasses="character")

data$r_goals_before_penalties_home <- as.numeric(data$r_goals_before_penalties_home)
data$r_goals_before_penalties_away <- as.numeric(data$r_goals_before_penalties_away)
data$r_goals_final_home <- as.numeric(data$r_goals_final_home)
data$r_goals_final_away <- as.numeric(data$r_goals_final_away)

compute_game_outcome <- function(home_goals, away_goals, outcomes) {
  ifelse(is.na(home_goals) | is.na(away_goals), NA,
    ifelse(home_goals > away_goals, outcomes[1],
      ifelse(home_goals == away_goals, outcomes[2], outcomes[3])))
}

compute_game_outcome_before_penalties <- function(outcomes) {
  mapply(
    compute_game_outcome,
    data$r_goals_before_penalties_home, 
    data$r_goals_before_penalties_away,
    MoreArgs=list(outcomes))
}

compute_game_outcome_after_penalties <- function(outcomes) {
  mapply(
    compute_game_outcome,
    data$r_goals_final_home, 
    data$r_goals_final_away,
    MoreArgs=list(outcomes))
}

data$r_game_outcome_before_penalties <- 
  compute_game_outcome_before_penalties(c("HOME_WIN","DRAW","AWAY_WIN"))

data$r_game_outcome_before_penalties_num <- 
  compute_game_outcome_before_penalties(c(1,0.5,0))

data$r_game_outcome_before_penalties_winloose <- 
  compute_game_outcome_before_penalties(c("HOME_WIN",NA,"AWAY_WIN"))

data$r_game_outcome_before_penalties_draw <- 
  compute_game_outcome_before_penalties(c(FALSE,TRUE,FALSE))

data$r_game_outcome_after_penalties <- 
  compute_game_outcome_after_penalties(c("HOME_WIN","DRAW","AWAY_WIN"))

data$r_game_outcome_after_penalties_num <- 
  compute_game_outcome_after_penalties(c(1,0.5,0))

data$r_game_outcome_after_penalties_winloose <- 
  compute_game_outcome_after_penalties(c("HOME_WIN",NA,"AWAY_WIN"))

data$r_game_outcome_after_penalties_draw <- 
  compute_game_outcome_after_penalties(c(FALSE,TRUE,FALSE))

write.csv2(data, "output/games.csv")
