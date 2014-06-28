import groovy.transform.AutoClone
import au.com.bytecode.opencsv.CSVReader
class Engine {
    def gameProbMatrix

    def koGames = []

    static Engine fromCSV(filename) {
        def is = new InputStreamReader(new FileInputStream(filename))
        List<String[]> rows = new CSVReader(is, (char)';',(char)'"').readAll()
        def headers = rows[0]
        def content = rows[1..-1]

        def teamHomeIndex = headers.findIndexOf { it == "b_team_home" }
        def teamAwayIndex = headers.findIndexOf { it == "b_team_away" }
        def homeWinIndex = headers.findIndexOf { it == "HOME_WIN" }
        def awayWinIndex = headers.findIndexOf { it == "AWAY_WIN" }
        def drawIndex = headers.findIndexOf { it == "DRAW" }

        // strange way of R enumerating lines (the rownumber-column has no header)
        if (headers.length < content[0].length) {
            teamHomeIndex++
            teamAwayIndex++
            if (homeWinIndex != -1) homeWinIndex++
            if (awayWinIndex != -1) awayWinIndex++
            if (drawIndex != -1) drawIndex++
        }

        def probMatrix = [:]
        for (line in content) {
            def home = line[teamHomeIndex]
            def away = line[teamAwayIndex]
            def probs = [
                    Double.parseDouble(homeWinIndex != -1 ? line[homeWinIndex].replace((char)',',(char)'.') : "0"),
                    Double.parseDouble(drawIndex != -1 ? line[drawIndex].replace((char)',',(char)'.') : "0"),
                    Double.parseDouble(awayWinIndex != -1 ? line[awayWinIndex].replace((char)',',(char)'.') : "0")]

            if (probMatrix[home] == null) {
                probMatrix[home] = [:]
            }
            probMatrix[home][away] = probs
        }
        return new Engine(probMatrix)
    }

    Engine(probMatrix) {
        gameProbMatrix = probMatrix
    }

    Game addKoGame(home, away) {
        def theGame = new Game(home, away)
        koGames.add(theGame)
        return theGame;
    }

    class Game {
        TeamWithProb[] homeTeams
        TeamWithProb[] awayTeams

        Game(home, away) {
            if (home instanceof String) {
                this.homeTeams = [new TeamWithProb(home, 1)]
            } else {
                this.homeTeams = home
            }
            if (away instanceof String) {
                this.awayTeams = [new TeamWithProb(away, 1)]
            } else {
                this.awayTeams = away
            }
        }

        def opponents() {
            if (homeTeams.size() != 1 || awayTeams.size() != 1) {
                throw new IllegalAccessException("Cannot extract opponents if teams don't have prob=1")
            }
            [homeTeams[0].name, awayTeams[0].name]
        }

        @Lazy GameResult[] result = {
            def results = []
            for (home in homeTeams) {
                for (away in awayTeams) {
                    def homeWinProb = gameProbMatrix[home.name][away.name][0]
                    def drawProb = gameProbMatrix[home.name][away.name][1]
                    def awayWinProb = gameProbMatrix[home.name][away.name][2]
                    def prob = home.prob * away.prob
                    results += [
                            new GameResult(home: home.name, away: away.name, outcome: "HOME_WIN", prob: homeWinProb*prob),
                            new GameResult(home: home.name, away: away.name, outcome: "DRAW", prob: drawProb*prob),
                            new GameResult(home: home.name, away: away.name, outcome: "AWAY_WIN", prob: awayWinProb*prob)
                    ]
                }
            }
            return results
        }()

        @Lazy TeamWithProb[] winner = {
            def allTeams = (homeTeams.collect({it.name}) + awayTeams.collect({it.name})).unique()
            def tmp = [:]
            for (team in allTeams) {
                tmp << [(team):new TeamWithProb(team, 0)]
            }
            for (GameResult gameResult in result) {
                if (! gameResult.draw) {
                    tmp[gameResult.winner].prob += gameResult.prob
                }
            }
            tmp.values()
        }()
    }

    class GameResult {
        def home
        def away
        def outcome
        def prob

        def opponents() { [home, away] }

        def getWinner() {
            switch (outcome) {
                case "HOME_WIN": home
                    break
                case "AWAY_WIN": away
                    break
                case "DRAW": throw new IllegalAccessException("No winner in draw game")
            }
        }

        @Override
        public String toString() {
            return home + ":" + away + " " + outcome + " (" + prob + ")"
        }

        def getLooser() {
            switch (outcome) {
                case "HOME_WIN": away
                    break
                case "AWAY_WIN": home
                    break
                case "DRAW": throw new IllegalAccessException("No winner in draw game")
            }
        }

        boolean isDraw() { outcome == "DRAW"}

    }


    class TeamWithProb {
        def name
        def prob

        TeamWithProb(name, prob) {
            this.name = name
            this.prob = prob
        }

        @Override
        public String toString() { sprintf("%s (%2.0f%%)", name, prob*100) }
    }

}