all: compile run

run:
	java -ea -jar target/soccer_feature-1.0-SNAPSHOT-jar-with-dependencies.jar ../1-fifa-ranking/output/games-with-fifa-rankings.csv output/soccerDataWithFeatures.csv output/featureDescriptions.txt

install-jar:
	mvn install:install-file -DgroupId=net.sf.opencsv -DartifactId=opencsv -Dversion=2.4 -Dpackaging=jar -Dfile=lib/opencsv-2.4.jar

compile: install-jar
	mvn clean compile assembly:single

clean:
	mvn clean
	rm -rf output/*
