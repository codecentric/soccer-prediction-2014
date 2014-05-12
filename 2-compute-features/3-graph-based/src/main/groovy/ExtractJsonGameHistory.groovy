import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import groovy.json.JsonBuilder
import org.neo4j.graphdb.*
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.factory.GraphDatabaseSettings
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluator
import org.neo4j.graphdb.traversal.Uniqueness

import java.text.SimpleDateFormat

ExpandoMetaClass.enableGlobally()
PropertyContainer.metaClass.getProperty = { name -> delegate.getProperty(name) }
PropertyContainer.metaClass.setProperty = { name, val -> delegate.setProperty(name, val) }

def DB_PATH = "output/game-graph.db"
def CSV_INPUT_FILE = "../2-league-based/output/soccerDataWithFeatures.csv"
def CSV_OUTPUT_FILE = "output/games-with-graph-paths-json.csv"

def MILLI_SECONDS_IN_ONE_YEAR = 365l * 24 * 60 * 60 * 1000
def MAX_YEARS_BACK = 5l
def MAX_MILLI_SECONDS_BACK = MAX_YEARS_BACK * MILLI_SECONDS_IN_ONE_YEAR
def MAX_PATH_LENGTH = 3l
def DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

def reader = new CSVReader(new FileReader(CSV_INPUT_FILE), ';' as char, '"' as char);
def games = reader.readAll()
reader.close()

def headers = games[0]
def dateIndex = headers.findIndexOf { it == "b_date" }
def teamHomeIndex = headers.findIndexOf { it == "b_team_home" }
def teamAwayIndex = headers.findIndexOf { it == "b_team_away" }

def graphDatabase = new GraphDatabaseFactory()
        .newEmbeddedDatabaseBuilder(DB_PATH)
        .setConfig(GraphDatabaseSettings.node_keys_indexable, "teamName")
        .setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
        .setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true")
        .newGraphDatabase();
def graphIndex = graphDatabase.index().getNodeAutoIndexer().getAutoIndex();

def tx = graphDatabase.beginTx()

def writer = new CSVWriter(new FileWriter(CSV_OUTPUT_FILE), ';' as char, '"' as char)
writer.writeNext((String[]) headers.toList() + ["b_graph_paths_json"])
games[1..-1].eachWithIndex { item, index ->
    println "Processing entry $index of $games.size"

    def date = item[dateIndex]
    def dateNumeric = DATE_FORMAT.parse(date).getTime()
    def teamHome = item[teamHomeIndex]
    def teamAway = item[teamAwayIndex]

    def teamHomeNode = graphIndex.get("teamName", teamHome).getSingle()

    def traversal = graphDatabase.traversalDescription()
            .relationships(graphfeatures.RelTypes.GAME, Direction.OUTGOING)
            .uniqueness(Uniqueness.NODE_PATH)
            .evaluator(new GamePathEvaluator(dateNumeric - MAX_MILLI_SECONDS_BACK, dateNumeric, teamAway, MAX_PATH_LENGTH))

    def result = traversal.traverse(teamHomeNode).iterator().collect { Path path ->
        return path.relationships().collect { Relationship game ->
            [
                    game: "${game.getStartNode().teamName}-${game.getEndNode().teamName}",
                    goalDifference: "${game.goalDifference}",
                    dateNumeric: "${game.dateNumeric}"
            ]
        }
    }
    def json = new JsonBuilder(result);
    writer.writeNext((String[]) item.toList() + [json])
}
writer.close()

tx.success()
tx.close()
graphDatabase.shutdown()

class GamePathEvaluator implements Evaluator {
    def dateStart
    def dateEnd
    def otherTeamName
    def maxPathLength

    GamePathEvaluator(dateStartNumeric, dateEndNumeric, otherTeamName, maxPathLength) {
        this.dateStart = dateStartNumeric
        this.dateEnd = dateEndNumeric
        this.otherTeamName = otherTeamName
        this.maxPathLength = maxPathLength
    }

    @Override
    Evaluation evaluate(Path path) {
        def lastRelationship = path.lastRelationship()
        if (lastRelationship == null) {
            return Evaluation.EXCLUDE_AND_CONTINUE
        }

        def lastRelationshipDate = lastRelationship.getProperty("dateNumeric")
        boolean dateInRange =
                lastRelationshipDate >= dateStart &&
                        lastRelationshipDate <= dateEnd
        if (!dateInRange) {
            return Evaluation.EXCLUDE_AND_PRUNE
        }

        boolean doInclude = path.endNode().getProperty("teamName") == otherTeamName
        if (doInclude) {
            return Evaluation.INCLUDE_AND_PRUNE
        }

        boolean doContinue = path.length() < maxPathLength
        return Evaluation.of(false, doContinue)
    }
}