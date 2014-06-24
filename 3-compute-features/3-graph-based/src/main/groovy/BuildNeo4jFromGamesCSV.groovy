import au.com.bytecode.opencsv.CSVReader
import org.neo4j.graphdb.PropertyContainer
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.factory.GraphDatabaseSettings

import java.text.SimpleDateFormat

ExpandoMetaClass.enableGlobally()
PropertyContainer.metaClass.getProperty = {name -> delegate.getProperty(name)}
PropertyContainer.metaClass.setProperty = {name, val -> delegate.setProperty(name, val)}

def INPUT_FILE = "../2-league-based/output/soccerDataWithFeatures.csv"

def DB_PATH = "output/game-graph.db"

def DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")

def reader = new CSVReader(new FileReader(INPUT_FILE), ';' as char,'"' as char);
def lines = reader.readAll()
reader.close()

def graphDatabase = new GraphDatabaseFactory()
        .newEmbeddedDatabaseBuilder(DB_PATH)
        .setConfig( GraphDatabaseSettings.node_keys_indexable, "teamName" )
//        .setConfig( GraphDatabaseSettings.relationship_keys_indexable, "relProp1,relProp2" )
        .setConfig( GraphDatabaseSettings.node_auto_indexing, "true" )
        .setConfig( GraphDatabaseSettings.relationship_auto_indexing, "true" )
        .newGraphDatabase();

def graphIndex = graphDatabase.index().getNodeAutoIndexer().getAutoIndex();

// First line is only headers
def headers = lines[0]
def dateIndex = headers.findIndexOf { it == "b_date" }
def teamHomeIndex = headers.findIndexOf { it == "b_team_home" }
def teamAwayIndex = headers.findIndexOf { it == "b_team_away" }
def goalsHomeIndex = headers.findIndexOf { it == "r_goals_before_penalties_home" }
def goalsAwayIndex = headers.findIndexOf { it == "r_goals_before_penalties_away" }

tx = graphDatabase.beginTx()
lines[1..-1].eachWithIndex { item,index ->
    println "Processing entry $index of $lines.size"

    date = item[dateIndex]
    dateNumeric = DATE_FORMAT.parse(date)
    teamHome = item[teamHomeIndex]
    teamAway = item[teamAwayIndex]
    try {
    	goalsHome = Integer.parseInt(item[goalsHomeIndex])
    	goalsAway = Integer.parseInt(item[goalsAwayIndex])
    } catch (NumberFormatException e) {
        println("Skip item because of NumberFormatException when reading goals")
        return;
    }	  
    goalDifference = goalsHome - goalsAway;

    println "$date $teamHome:$teamAway $goalDifference"

    org.neo4j.graphdb.Node homeNode = graphIndex.get("teamName", teamHome).getSingle();
    if (homeNode) {
        println "Node for team $teamHome already exists: $homeNode"
    } else {
        homeNode = graphDatabase.createNode()
        homeNode.addLabel(graphfeatures.NodeLabels.TEAM)
        homeNode.teamName = teamHome
        println "Created new node for team $teamHome: $homeNode"
    }

    awayNode = graphIndex.get("teamName", teamAway).getSingle();
    if (awayNode) {
        println "Node for team $teamAway already exists: $awayNode"
    } else {
        awayNode = graphDatabase.createNode()
        awayNode.addLabel(graphfeatures.NodeLabels.TEAM)
        awayNode.teamName = teamAway
        println "Created new node for team $teamAway: $awayNode"
    }

    // clean up old games
    homeNode.getRelationships(graphfeatures.RelTypes.GAME).grep { Relationship relationship ->
        return relationship.getProperty("date") == date
    }.each { game ->
        game.delete()
    }

    game = homeNode.createRelationshipTo(awayNode, graphfeatures.RelTypes.GAME)
    game.date = date
    game.goalDifference = goalDifference
    game.dateNumeric = dateNumeric.getTime()
    println "Created relationship $teamHome->$teamAway with date=$date and goalDifference=$goalDifference: $game"

    game = awayNode.createRelationshipTo(homeNode, graphfeatures.RelTypes.GAME)
    game.date = date
    game.goalDifference = goalDifference
    game.dateNumeric = dateNumeric.getTime()
    println "Created relationship $teamAway->$teamHome with date=$date and goalDifference=$goalDifference: $game"

}
tx.success()
tx.close()
graphDatabase.shutdown()
