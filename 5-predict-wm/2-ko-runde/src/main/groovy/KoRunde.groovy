engine = Engine.fromCSV("../1-games/output/game-predictions-ko-allgames-mean.csv")

println engine.gameProbMatrix

def printWinner(gameName, Engine.Game game) {
    println gameName + ": " + game.winner.sort({a,b -> b.prob <=> a.prob}).join(", ")
}

// Achtelfinale
def AF = []
AF[1] = engine.addKoGame("BRA","CHL")
AF[2] = engine.addKoGame("COL","URY")
AF[3] = engine.addKoGame("NLD","MEX")
AF[4] = engine.addKoGame("CRI","GRC")
AF[5] = engine.addKoGame("FRA","NGA")
AF[6] = engine.addKoGame("DEU","DZA")
AF[7] = engine.addKoGame("ARG","CHE")
AF[8] = engine.addKoGame("BEL","USA")

//Viertelfinale
def VF = []
def VFDesc = []
VF[1] = engine.addKoGame(AF[5].winner, AF[6].winner)
VFDesc[1] = "VF1 (Sieger AF5 - Sieger AF6)"
VF[2] = engine.addKoGame(AF[1].winner, AF[2].winner)
VFDesc[2] = "VF2 (Sieger AF1 - Sieger AF2)"
VF[3] = engine.addKoGame(AF[7].winner, AF[8].winner)
VFDesc[3] = "VF3 (Sieger AF7 - Sieger AF8)"
VF[4] = engine.addKoGame(AF[3].winner, AF[4].winner)
VFDesc[4] = "VF4 (Sieger AF3 - Sieger AF4)"

//Halbfinale
def HF = []
def HFDesc = []
HF[1] = engine.addKoGame(VF[1].winner, VF[2].winner)
HFDesc[1] = "HF1 (Sieger VF1 - Sieger VF2)"
HF[2] = engine.addKoGame(VF[3].winner, VF[4].winner)
HFDesc[2] = "HF2 (Sieger VF3 - Sieger VF4)"

//Finale
def F = engine.addKoGame(HF[1].winner, HF[2].winner)

println("Achtelfinale")
println("=============")
for (i in 1..8) {
    printWinner("AF$i", AF[i])
}
println("\n")

println("Viertelfinale")
println("=============")
for (i in 1..4) {
    printWinner(VFDesc[i], VF[i])
}
println("\n")

println("Halbfinale")
println("==========")
for (i in 1..2) {
    printWinner(HFDesc[i], HF[i])
}
println("\n")

println("Finale")
println("======")
printWinner("F", F)
println("\n")




