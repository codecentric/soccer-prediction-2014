# Vorhersage für die KO-Runde der WM 2014

Folgende Vorhersage können einfach mit `gradlew predict` oder `make` erstellt werden.

Für die Vorhersage wurde ein Random Forest Modell verwendet, dass auf allen Spielen seit 1994 trainiert wurde. Um den Einfluss des "Heimvorteils" (der bei einer WM nicht relevant ist) zu minimieren,
wurde für die Gewinnwahrscheinlichkeiten einer Mannschaft X im Spiel X-Y das Mittel aus Heimsiegwahrscheinlichkeit für Spiel X-Y und Auswärtssiegwahrscheinlichkeit für Spiel Y-X verwendet.

```
Achtelfinale
=============
AF1: BRA (52%), CHL (48%)
AF2: COL (50%), URY (50%)
AF3: NLD (57%), MEX (43%)
AF4: GRC (59%), CRI (41%)
AF5: FRA (66%), NGA (34%)
AF6: DEU (66%), DZA (34%)
AF7: CHE (55%), ARG (45%)
AF8: USA (54%), BEL (46%)


Viertelfinale
=============
VF1 (Sieger AF5 - Sieger AF6): DEU (38%), FRA (35%), DZA (14%), NGA (13%)
VF2 (Sieger AF1 - Sieger AF2): BRA (27%), COL (25%), URY (24%), CHL (24%)
VF3 (Sieger AF7 - Sieger AF8): USA (30%), CHE (26%), BEL (24%), ARG (20%)
VF4 (Sieger AF3 - Sieger AF4): GRC (32%), NLD (30%), MEX (19%), CRI (19%)


Halbfinale
==========
HF1 (Sieger VF1 - Sieger VF2): DEU (22%), FRA (19%), BRA (14%), URY (12%), COL (12%), CHL (11%), DZA ( 5%), NGA ( 5%)
HF2 (Sieger VF3 - Sieger VF4): GRC (17%), USA (16%), NLD (16%), CHE (13%), BEL (12%), ARG ( 9%), MEX ( 8%), CRI ( 8%)


Finale
======
Finale: DEU (13%), FRA (11%), USA ( 8%), GRC ( 8%), NLD ( 8%), BRA ( 7%), CHE ( 6%), URY ( 6%), 
        COL ( 6%), BEL ( 6%), CHL ( 6%), ARG ( 4%), MEX ( 3%), CRI ( 3%), DZA ( 2%), NGA ( 2%)
```