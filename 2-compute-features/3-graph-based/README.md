Graph Based Features
====================

In der finalen Ausgabedatei `output/games-with-graph-features.csv`
gibt es Spalten der Art
`b_graph_score_<Years>_<PathLength>_<YearsWeight>_<PathLengthWeight>`. Dazu
ein kurzes Beispiel: Angenommen es geht um das Spiel
Deutschland-Brasilien im Jahr 2012. Für das Score-Feature wird ein
Graph aller Spiele, die maximal <Years> Jahre vor dem dem betrachteten
Spiel stattfanden, aufgestellt (mit Ländern als Knoten und Spielen als
Kanten). In diesem Graphen werden alle Pfade vom Knoten "Deutschland"
zum Knoten "Brasilien" gesicht, die maximal Länge <PathLength>
haben. Für alle diese Pfade wird die summierte Tordifferenz berechnet,
d.h. wenn Deutschland gegen Italien 2:1 gewonnen hat und Italien gegen
Brasilien 3:0 verloren hat, wäre die summierte Tordifferenz für diesen
Pfad -2. Für das letztendliche Score-Feature werden nun der gewichtete
Mittelwert über die summierten Tordifferenzen aller Pfade
gebildet. Die Gewichte sind dabei <YearsWeight> ^ (Maximale Anzahl
Jahre, die ein Spiel des Pfades zurückliegt) * <PathLengthWeight> ^ (Länge
des Pfades). Zum Beispiel: Wenn das Spiel Deutschland-Italien 2011
stattfand und das Spiel Italien-Brasilien 2009 stattfand, wäre das
Gewicht für diesen Pfad <YearsWeight>^3*<PathLengthWeight>^2.

Bei den Spalten `b_graph_score_simple` wird nicht die summierte
Tordifferenz berechnet, sondern pauschal ein Sieg mit "1" und eine
Niederlage mit "+1" bewertet.
