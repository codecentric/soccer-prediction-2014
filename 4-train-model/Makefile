all: train plot predict-vorrunde predict-all

train:
	Rscript scripts/train-and-evaluate-soccer.R

plot:
	Rscript scripts/plot-model-comparison.R

predict-vorrunde:
	Rscript scripts/predict-wm-vorrunde-2014.R

predict-all:
	Rscript scripts/predict-wm-2014.R

clean:
	rm -rf output/*
