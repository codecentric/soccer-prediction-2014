# Daten zu vergangenen Spielen einlesen und type-casten
data <- read.csv("../../2-clean-data/1-tournament-names/output/games.csv", sep=";", colClasses="character")
spiele <- data
spiele$date <-strptime(spiele$b_date, format="%Y-%m-%d")

# Daten zu FIFA-Rankings einlesen, type-casten und nach Team gruppieren
rankings <- read.csv("input/fifa-rankings-with-iso.csv", colClasses="character")
rankings$Datum <- strptime(rankings$Datum, format="%d %b %Y")
rankings$Platzierung <- as.numeric(rankings$Platzierung)
rankings$Punkte <- as.numeric(rankings$Punkte)
rankings$Veraenderung <- as.numeric(rankings$Veraenderung)
rankings_by_team <- split(rankings, rankings$ISO)

# Sucht für ein gegenenes Team und Datum zugehörige Fifa-Statistik, d.h. 
# fifa_statistik_zu_datum("Deutschland", as.POSIXct("2000-10-10"))
# gibt die FIFA-Statistik (Platzierung, Punkte, Veränderung der Platzierung 
# seit letztem Monat) von Deutschland im Oktober 2000 zurück
fifa_statistik_zu_datum <- function(team, datum) {
  team_rankings <- rankings_by_team[[team]]
  if (is.null(team_rankings)) { return(c(Platzierung=NA,Punkte=NA,Veraenderung=NA)) }  
  relevant_bool <- team_rankings$Datum < datum
  relevant <- team_rankings[relevant_bool,]
  if (nrow(relevant) == 0) { return(c(Platzierung=NA,Punkte=NA,Veraenderung=NA)) }
  max_index <- which.max(as.numeric(relevant$Datum))
  return(c(
    Platzierung=relevant[max_index, "Platzierung"],
    Punkte=relevant[max_index, "Punkte"],
    Veraenderung=relevant[max_index, "Veraenderung"]))
}

# Anreichern der Spiele-Liste mit FIFA-Statistiken für jeweils beide Teams
fifa_statistiken_home <- mapply(
  fifa_statistik_zu_datum,
  spiele$b_team_home,
  # anscheinend interpretiert R POSIXlt Datum hier als int-Liste, argh...
  as.POSIXct(spiele$date),
  SIMPLIFY=TRUE,
  USE.NAMES=FALSE)
fifa_statistiken_away <- mapply(
  fifa_statistik_zu_datum,
  spiele$b_team_away,
  # anscheinend interpretiert R POSIXlt Datum hier als int-Liste, argh...
  as.POSIXct(spiele$date),
  SIMPLIFY=TRUE)

data$b_fifa_ranking_home <- fifa_statistiken_home["Platzierung",]
data$b_fifa_points_home <- fifa_statistiken_home["Punkte",]
data$b_fifa_ranking_away <- fifa_statistiken_away["Platzierung",]
data$b_fifa_points_away <- fifa_statistiken_away["Punkte",]
data$b_fifa_ranking_difference <- data$b_fifa_ranking_home - data$b_fifa_ranking_away
data$b_fifa_points_difference <- data$b_fifa_points_home - data$b_fifa_points_away

# Die Spalte "Veränderung" der FIFA-Statistiken enthält die Veränderung der Platzierung eines
# Teams im Vergleich zum Vormonat. Man könnte auch sagen, es ist der "Trend" einer Mannschaft.
# Wenn positiv, heißt es, dass die Mannschaft in den letzten Wochen besser geworden ist (d.h. 
# in der FIFA-Rangliste nach oben gewandert ist).
data$b_fifa_trend_home <- fifa_statistiken_home["Veraenderung",]
data$b_fifa_trend_away <- fifa_statistiken_away["Veraenderung",]
data$b_fifa_trend_difference <- data$b_fifa_trend_home - data$b_fifa_trend_away

write.table(data, "output/games-with-fifa-rankings.csv", quote=FALSE, row.names=FALSE, na="", sep=";")