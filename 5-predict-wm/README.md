# Vorhersage der Gruppenphase der WM-2014

Die folgenden Wahrscheinlichkeiten können mit einem einfachen Aufruf von `make predict-vorrunde` berechnet werden:

```
VORHERSAGE FÜR DIE VORRUNDE

Berechnet mit Random Forest trainiert mit EM, WM und Qualifikationsspielen

     b_team_home b_team_away HOME_WIN AWAY_WIN predicted_class
6408         BRA         HRV    0.662    0.338        HOME_WIN
6409         CHL         AUS    0.688    0.312        HOME_WIN
6410         ESP         NLD    0.594    0.406        HOME_WIN
6411         MEX         CMR    0.774    0.226        HOME_WIN
6412         CIV         JPN    0.446    0.554        AWAY_WIN
6413         COL         GRC    0.498    0.502        AWAY_WIN
6414         ENG         ITA    0.572    0.428        HOME_WIN
6415         URY         CRI    0.752    0.248        HOME_WIN
6416         ARG         BIH    0.498    0.502        AWAY_WIN
6417         CHE         ECU    0.712    0.288        HOME_WIN
6418         FRA         HND    0.802    0.198        HOME_WIN
6419         DEU         PRT    0.462    0.538        AWAY_WIN
6420         GHA         USA    0.266    0.734        AWAY_WIN
6421         IRN         NGA    0.746    0.254        HOME_WIN
6422         BEL         DZA    0.732    0.268        HOME_WIN
6423         BRA         MEX    0.696    0.304        HOME_WIN
6424         RUS         KOR    0.690    0.310        HOME_WIN
6425         AUS         NLD    0.482    0.518        AWAY_WIN
6426         CMR         HRV    0.366    0.634        AWAY_WIN
6427         ESP         CHL    0.656    0.344        HOME_WIN
6428         COL         CIV    0.622    0.378        HOME_WIN
6429         JPN         GRC    0.360    0.640        AWAY_WIN
6430         URY         ENG    0.508    0.492        HOME_WIN
6431         CHE         FRA    0.508    0.492        HOME_WIN
6432         HND         ECU    0.502    0.498        HOME_WIN
6433         ITA         CRI    0.602    0.398        HOME_WIN
6434         ARG         IRN    0.482    0.518        AWAY_WIN
6435         DEU         GHA    0.722    0.278        HOME_WIN
6436         NGA         BIH    0.406    0.594        AWAY_WIN
6437         BEL         RUS    0.604    0.396        HOME_WIN
6438         KOR         DZA    0.510    0.490        HOME_WIN
6439         USA         PRT    0.496    0.504        AWAY_WIN
6440         AUS         ESP    0.474    0.526        AWAY_WIN
6441         CMR         BRA    0.358    0.642        AWAY_WIN
6442         HRV         MEX    0.630    0.370        HOME_WIN
6443         NLD         CHL    0.600    0.400        HOME_WIN
6444         CRI         ENG    0.482    0.518        AWAY_WIN
6445         GRC         CIV    0.730    0.270        HOME_WIN
6446         ITA         URY    0.524    0.476        HOME_WIN
6447         JPN         COL    0.452    0.548        AWAY_WIN
6448         BIH         IRN    0.534    0.466        HOME_WIN
6449         ECU         FRA    0.322    0.678        AWAY_WIN
6450         HND         CHE    0.412    0.588        AWAY_WIN
6451         NGA         ARG    0.414    0.586        AWAY_WIN
6452         DZA         RUS    0.428    0.572        AWAY_WIN
6453         KOR         BEL    0.452    0.548        AWAY_WIN
6454         PRT         GHA    0.808    0.192        HOME_WIN
6455         USA         DEU    0.544    0.456        HOME_WIN
```

# Vorhersage für die KO-Phase

Die folgenden Wahrscheinlichkeiten können mit einem einfachen Aufruf von `make predict-ko` berechnet werden:

```
SIEGWAHRSCHEINLICHKEITEN FÜR HEIMSPIELE IN DER KO-RUNDE

Berechnet mit Random-Forest trainiert mit allen Spielen
Jede Zelle enthält die Siegwahrscheinlichkeit für die 'Zeilenmannschaft

      FRA   DEU   GRC   USA   NLD   URY   COL   CHL   CHE   BRA   BEL   ARG
FRA 0.500 0.540 0.600 0.608 0.592 0.630 0.626 0.614 0.624 0.578 0.632 0.658
DEU 0.568 0.500 0.598 0.598 0.604 0.622 0.606 0.610 0.616 0.578 0.624 0.634
GRC 0.522 0.520 0.500 0.562 0.544 0.566 0.564 0.564 0.560 0.532 0.580 0.566
USA 0.524 0.522 0.524 0.500 0.540 0.560 0.566 0.554 0.554 0.528 0.574 0.584
NLD 0.524 0.524 0.518 0.534 0.500 0.554 0.556 0.544 0.540 0.518 0.570 0.546
URY 0.504 0.494 0.512 0.532 0.518 0.500 0.548 0.544 0.538 0.516 0.580 0.564
COL 0.516 0.512 0.518 0.534 0.532 0.556 0.500 0.540 0.542 0.518 0.568 0.554
CHL 0.498 0.498 0.496 0.526 0.512 0.540 0.544 0.500 0.526 0.500 0.556 0.540
CHE 0.500 0.484 0.492 0.510 0.508 0.528 0.530 0.518 0.500 0.496 0.546 0.532
BRA 0.490 0.486 0.492 0.506 0.508 0.520 0.518 0.516 0.510 0.500 0.526 0.526
BEL 0.456 0.458 0.468 0.494 0.470 0.498 0.504 0.490 0.492 0.478 0.500 0.516
ARG 0.436 0.438 0.466 0.480 0.476 0.484 0.500 0.492 0.494 0.474 0.516 0.500
MEX 0.450 0.450 0.438 0.474 0.468 0.460 0.484 0.484 0.466 0.460 0.516 0.530
CRI 0.406 0.410 0.424 0.450 0.440 0.440 0.458 0.464 0.448 0.442 0.482 0.500
DZA 0.386 0.390 0.406 0.422 0.424 0.420 0.442 0.444 0.424 0.424 0.464 0.472
NGA 0.372 0.380 0.402 0.416 0.418 0.404 0.436 0.442 0.428 0.428 0.462 0.476