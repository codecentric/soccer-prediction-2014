data <- read.csv2("../../1-merge-data/output/games.csv", colClasses="character")

remove_year <- function(s) {
  s <- gsub("[0-9]","",s)
  s <- gsub("^\\s*", "",s)
  s <- gsub("\\s$","",s)
}

extract_year <- function(s) {
  s <- gsub("[^0-9]","",s)
  s <- gsub("^\\s*", "",s)
  s <- gsub("\\s$","",s)  
}

prepare_name_table <- function(data, filename) {
  names1 <- sapply(data$b_tournament_name1, remove_year)
  names2 <- sapply(data$b_tournament_name2, remove_year)
  nametable1 <- table(names1)
  nametable2 <- table(names2)
  nametable <- c(rownames(nametable1),rownames(nametable2))
  write.csv2(cbind(nametable, nametable),
             filename, row.names=FALSE, col.names=FALSE)
}
#prepare_name_table(data, "input/turniernamen.csv")

extract_tournament_name <- function(data) {
  name_table <- read.csv2("input/turniernamen.csv",colClasses="character",header=FALSE)
  simplify_tournament_name <- function(name) {
    name <- remove_year(name)
    normal <- name_table[1] == name
    if (sum(normal, na.rm=TRUE) == 1) {
      finalname <- name_table[which(normal),2]
      return(as.character(finalname))
    } else {
      return("")
    } 
  }
  
  simplenames1 <- sapply(data$b_tournament_name1, simplify_tournament_name)
  simplenames2 <- sapply(data$b_tournament_name2, simplify_tournament_name)
  nameconflicts <- simplenames1 != "" & simplenames2 != "" & simplenames1 != simplenames2
  if (any(nameconflicts)) {
    message("Turnier der folgenden Spiele konnte nicht eindeutig bestimmt werden: ")
    print(cbind(data[nameconflicts,c("b_date","b_team_home","b_team_away")], 
                simplenames1[nameconflicts], simplenames2[nameconflicts]))
  }
  
  simplenames <- ifelse(simplenames1=="", simplenames2, simplenames1)
  return(simplenames)
}
data$b_tournament_name <- extract_tournament_name(data)

prepare_phase_table <- function(data, filename) {
  names1 <- sapply(data$b_tournament_name1, remove_year)
  names2 <- sapply(data$b_tournament_name2, remove_year)
  phases1 <- sapply(data$b_tournament_phase1, remove_year)
  phases2 <- sapply(data$b_tournament_phase2, remove_year)
  phases3 <- sapply(data$b_tournament_phase3, remove_year)
  nametable1 <- table(names1)
  nametable2 <- table(names2)
  phasetable1 <- table(phases1)
  phasetable2 <- table(phases2)
  phasetable3 <- table(phases3)
  
  phases <- c(rownames(nametable1), 
              rownames(nametable2), 
              rownames(phasetable1), 
              rownames(phasetable2), 
              rownames(phasetable3))
  
  write.csv2(cbind(phases, phases),
             filename, row.names=FALSE)
}
#prepare_phase_table(data, "input/turnierphasen.csv")

extract_tournament_phase <- function(data) {
  phase_table <- read.csv2("input/turnierphasen.csv",colClasses="character",header=FALSE)
  simplify_tournament_phase <- function(phase) {
    phase <- remove_year(phase)
    normal <- phase_table[1] == phase
    if (sum(normal, na.rm=TRUE) == 1) {
      finalphase <- phase_table[which(normal),2]
      return(as.character(finalphase))
    } else {
      return("")
    } 
  }
  
  simplephase = rbind(sapply(data$b_tournament_name1, simplify_tournament_phase),
                      sapply(data$b_tournament_name2, simplify_tournament_phase),
                      sapply(data$b_tournament_phase1, simplify_tournament_phase),
                      sapply(data$b_tournament_phase2, simplify_tournament_phase),
                      sapply(data$b_tournament_phase3, simplify_tournament_phase))
  
  phases <- ifelse(simplephase[1,] != "", simplephase[1,],
                   ifelse(simplephase[2,] != "", simplephase[2,],
                          ifelse(simplephase[3,] != "", simplephase[3,],
                                 ifelse(simplephase[4,] != "", simplephase[4,], simplephase[5,]))))
  return(phases)
}
data$b_tournament_phase <- extract_tournament_phase(data)

extract_tournament_year <- function(data) {
  years1 <- sapply(data$b_tournament_name1, extract_year)
  years2 <- sapply(data$b_tournament_name2, extract_year)
  yearconflicts <- years1 != "" & years2 != "" & years1 != years2
  if (any(yearconflicts)) {
    message("Turnierjahr der folgenden Spiele konnte nicht eindeutig bestimmt werden: ")
    print(data[nameconflicts,c("b_date","b_team_home","b_team_away")])
  }
  
  years <- ifelse(years1!="",years1,years2)
  return(years)
}
data$b_tournament_year <- extract_tournament_year(data)

prepare_name_country_combination_table <- function(data, filename) {
  t <- table(data[,c("b_tournament_country","b_tournament_name","b_tournament_year")])
  tt <- as.data.frame(t)
  ttt <- tt[tt$Freq!=0 & 
              tt$b_tournament_country != "" & 
              tt$b_tournament_name != "" &
              tt$b_tournament_year != "",]
  tttt <- ttt[,c("b_tournament_country","b_tournament_name","b_tournament_year")]
  write.csv2(tttt, filename, row.names=FALSE)
}
#prepare_name_country_combination_table(data,"input/name-country-combination.csv")

fill_missing_tournament_names_and_years <- function(data) {
  name_country_table <- read.csv2("input/name-country-combination.csv",colClasses="character")
  for (rowname in rownames(data)) {
    row <- data[rowname,]
    name <- data[rowname,"b_tournament_name"]
    tournament_year <- data[rowname, "b_tournament_year"]
    game_year <- substr(data[rowname, "b_date"],1,4)
    country <- data[rowname,"b_tournament_country"]
    
    if (!is.na(name) && name == "") {
      if (country != "") {
        relevant <- name_country_table[name_country_table$b_tournament_country == country,]
        if (nrow(relevant) == 1) {
          data[rowname, "b_tournament_name"] <- as.character(relevant$b_tournament_name[1])
        } else if (nrow(relevant) > 1) {
          if (tournament_year != "") {
            relevant2 <- relevant[relevant$b_tournament_year == tournament_year,]
            if (nrow(relevant2) == 1) {
              data[rowname, "b_tournament_name"] <- as.character(relevant2$b_tournament_name[1])
            } else {
              message(sprintf("Konnte Turnier für Spiel %s-%s am %s nicht auflösen (passendes Austragungsland aber kein passendes Jahr)",
                              row$b_team_home,row$b_team_away,row$b_date))
            } 
          } else {
            potential_years <- as.numeric(relevant$b_tournament_year)
            relevant2 <- relevant[abs(potential_years - as.numeric(game_year)) <= 2,]
            if (nrow(relevant2) == 1) {
              data[rowname, "b_tournament_name"] <- as.character(relevant2$b_tournament_name[1])
              data[rowname, "b_tournament_year"] <- as.character(relevant2$b_tournament_year[1])
            }
          }
        }
      }
    }
  }
}
fill_missing_tournament_names_and_years(data)

write.csv2(data,"output/games.csv")
