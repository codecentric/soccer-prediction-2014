all: vorrunde-confusionmatrix fifa-predictor-confusionmatrix

vorrunde-confusionmatrix:
	Rscript scripts/vorrunde-confusion-matrix.R

fifa-predictor-confusionmatrix:
	Rscript scripts/evaluate-fifa-ranking-predictor.R

clean:
	rm -rf output/*
