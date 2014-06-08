import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import groovy.json.JsonSlurper
import org.neo4j.graphdb.Label
import org.neo4j.graphdb.RelationshipType

import java.text.SimpleDateFormat

def CSV_INPUT_FILE = "output/games-with-graph-paths-json.csv"
def CSV_OUTPUT_FILE = "output/games-with-graph-features.csv"

def DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

def reader = new CSVReader(new FileReader(CSV_INPUT_FILE), ';' as char,'"' as char);
def lines = reader.readAll()
reader.close()

def headers = lines[0]
def dateIndex = headers.findIndexOf { it == "b_date" }
def teamHomeIndex = headers.findIndexOf { it == "b_team_home" }
def teamAwayIndex = headers.findIndexOf { it == "b_team_away" }

def result = []
result[0] = headers.toList()[0..-2] + [
        "b_graph_score_5_2_08_06","b_graph_score_5_3_08_06","b_graph_score_3_2_08_06","b_graph_score_3_3_08_06",
        "b_graph_score_5_2_06_06","b_graph_score_5_3_06_06","b_graph_score_3_2_06_06","b_graph_score_3_3_06_06",
        "b_graph_score_5_2_08_08","b_graph_score_5_3_08_08","b_graph_score_3_2_08_08","b_graph_score_3_3_08_08",
        "b_graph_score_5_2_06_08","b_graph_score_5_3_06_08","b_graph_score_3_2_06_08","b_graph_score_3_3_06_08",
        "b_graph_score_3_3_04_04","b_graph_score_3_3_1_1","b_graph_score_2_3_1_1",
        "b_graph_score_simple_08_08","b_graph_score_simple_06_08",
        "b_graph_score_simple_08_06","b_graph_score_simple_06_06"]

def slurper = new JsonSlurper()

result += lines[1..-1].collect() { line ->
    date = line[dateIndex]
    dateNumeric = DATE_FORMAT.parse(date).getTime()
    teamHome = line[teamHomeIndex]
    teamAway = line[teamAwayIndex]
    pathsJson = line[-1]

    paths = slurper.parseText(pathsJson)
    if (paths == null) paths = []

    println "Processing $teamHome-$teamAway on $date (${paths.size()} paths)"

    line.toList()[0..-2] + [
            computeScore(paths, dateNumeric, 5, 2, 0.8, 0.6),
            computeScore(paths, dateNumeric, 5, 3, 0.8, 0.6),
            computeScore(paths, dateNumeric, 3, 2, 0.8, 0.6),
            computeScore(paths, dateNumeric, 3, 3, 0.8, 0.6),

            computeScore(paths, dateNumeric, 5, 2, 0.6, 0.6),
            computeScore(paths, dateNumeric, 5, 3, 0.6, 0.6),
            computeScore(paths, dateNumeric, 3, 2, 0.6, 0.6),
            computeScore(paths, dateNumeric, 3, 3, 0.6, 0.6),

            computeScore(paths, dateNumeric, 5, 2, 0.8, 0.8),
            computeScore(paths, dateNumeric, 5, 3, 0.8, 0.8),
            computeScore(paths, dateNumeric, 3, 2, 0.8, 0.8),
            computeScore(paths, dateNumeric, 3, 3, 0.8, 0.8),

            computeScore(paths, dateNumeric, 5, 2, 0.6, 0.8),
            computeScore(paths, dateNumeric, 5, 3, 0.6, 0.8),
            computeScore(paths, dateNumeric, 3, 2, 0.6, 0.8),
            computeScore(paths, dateNumeric, 3, 3, 0.6, 0.8),

            computeScore(paths, dateNumeric, 3, 3, 0.4, 0.4),
            computeScore(paths, dateNumeric, 3, 3, 1, 1),
            computeScore(paths, dateNumeric, 2, 3, 1, 1),

            computeSimpleScore(paths, dateNumeric,3,3,0.8,0.8),
            computeSimpleScore(paths, dateNumeric,3,3,0.6,0.8),
            computeSimpleScore(paths, dateNumeric,3,3,0.8,0.6),
            computeSimpleScore(paths, dateNumeric,3,3,0.6,0.6)
    ]
}

def writer = new CSVWriter(new FileWriter(CSV_OUTPUT_FILE),';' as char, '"' as char)
result.each { line -> writer.writeNext((String[]) line.collect {item -> item.toString()}.toArray()) }
writer.close()

private computeScore(paths, dateOfGame, maxYearsBack, maxPathLength, timeWeight, pathWeight) {
    def MILLI_SECONDS_IN_ONE_YEAR = 365l * 24 * 60 * 60 * 1000;
    def weightedSum = 0
    def sumOfWeights = 0

    def relevantPaths = paths.findAll { path ->
        dates = path.collect { game -> Long.valueOf(game["dateNumeric"]) }
        maxDate = dates.max()
        minDate = dates.min()
        return (maxDate < dateOfGame) &&
                (minDate > dateOfGame - MILLI_SECONDS_IN_ONE_YEAR*maxYearsBack) &&
                (path.size() <= maxPathLength)
    }
    relevantPaths.each { path ->
        def sumOfGoalDifference = path.collect { game -> Long.valueOf(game["goalDifference"]) }.sum()
        def pathLength = path.size()
        def maxYearsSince = path.collect { game -> Long.valueOf(game["dateNumeric"]) }.max() / MILLI_SECONDS_IN_ONE_YEAR
        def weight = Math.pow(timeWeight, maxYearsSince) * Math.pow(pathWeight, (pathLength - 1))
        weightedSum += weight * sumOfGoalDifference
        sumOfWeights += weight
    }

    def score = ""
    if (sumOfWeights > 0)
        score = weightedSum / sumOfWeights

    return score
}

private computeSimpleScore(paths, dateOfGame, maxYearsBack, maxPathLength, timeWeight, pathWeight) {
    def MILLI_SECONDS_IN_ONE_YEAR = 365l * 24 * 60 * 60 * 1000;
    def weightedSum = 0
    def sumOfWeights = 0

    def relevantPaths = paths.findAll { path ->
        dates = path.collect { game -> Long.valueOf(game["dateNumeric"]) }
        maxDate = dates.max()
        minDate = dates.min()
        return maxDate < dateOfGame &&
                minDate > dateOfGame - MILLI_SECONDS_IN_ONE_YEAR*maxYearsBack &&
                path.size() <= maxPathLength
    }

    relevantPaths.each { path ->
        def sumOfGoalDifference = path.collect { game -> Long.valueOf(game["goalDifference"]) }.sum()
        def pathLength = path.size()
        def maxYearsSince = path.collect { game -> Long.valueOf(game["dateNumeric"]) }.max() / MILLI_SECONDS_IN_ONE_YEAR
        def weight = Math.pow(timeWeight, maxYearsSince) * Math.pow(pathWeight, (pathLength - 1))
        if (path.every {game -> Long.valueOf(game["goalDifference"]) <= 0}) {
            weightedSum += weight
        }
        if (path.every {game -> Long.valueOf(game["goalDifference"]) >= 0}) {
            weightedSum -= weight
        }
        sumOfWeights += weight
    }

    def score = ""
    if (sumOfWeights > 0)
        score = weightedSum / sumOfWeights

    return score
}
