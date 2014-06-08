iso_code_files <- list.files(path="../../1-merge-data/input/", pattern="^iso|fifa.*de.*csv", full.names=TRUE)
iso_codes_list <- lapply(iso_code_files, read.csv, sep=";", header=FALSE)
iso_codes <- Reduce(function(...) merge(..., all=T), iso_codes_list)

code_by_country <- iso_codes[,1]
names(code_by_country) <- iso_codes[,2]

fifa_rankings <- read.csv("input/fifa-rankings.csv", colClasses="character")
fifa_rankings$ISO <- sapply(fifa_rankings$Team, 
    function(team) { return(code_by_country[team]) })

write.csv(fifa_rankings, "input/fifa-rankings-with-iso.csv")
