data <- read.csv2("output/games.csv", colClasses="character")

library("ggplot2")
library("scales")
data$b_date <- as.Date(data$b_date)

only_em = data[data$b_tournament_name == "EM",]
only_wm = data[data$b_tournament_name == "WM",]
em_and_wm = data[data$b_tournament_name == "WM" | data$b_tournament_name == "EM",]
qualification = em_and_wm[em_and_wm$b_tournament_phase == "Qualifikation",]
finalround = em_and_wm[em_and_wm$b_tournament_phase == "Endrunde",]


ggplot(data, aes(b_date, ..count..)) + 
  geom_histogram(binwidth=365, aes(fill=b_tournament_name)) +
  scale_fill_discrete("Turnier") +
  scale_y_continuous("Anzahl Spiele", formatter=comma) +
  scale_x_date("Datum")

ggplot(em_and_wm, aes(b_date)) + 
  geom_histogram(binwidth=365, 
                 aes(fill=paste(em_and_wm$b_tournament_name,em_and_wm$b_tournament_phase))) + 
  scale_fill_discrete("Turnier") +
  scale_y_continuous("Anzahl Spiele", formatter=comma) +
  scale_x_date("Datum")

ggplot(only_em, aes(b_date)) + 
  geom_histogram(binwidth=365, 
                 aes(fill=b_tournament_phase)) +
  scale_fill_discrete("Turnier") +
  scale_y_continuous("Anzahl Spiele", formatter=comma) +
  scale_x_date("Datum")

ggplot(only_wm, aes(b_date)) + 
  geom_histogram(binwidth=365, 
                 aes(fill=b_tournament_phase)) +
  scale_fill_discrete("Turnier") +
  scale_y_continuous("Anzahl Spiele", formatter=comma) +
  scale_x_date("Datum")
