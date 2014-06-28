all: predict-vorrunde predict-ko predict-all

predict-vorrunde:
	Rscript scripts/predict-wm-vorrunde-2014.R

predict-ko:
	Rscript scripts/predict-wm-ko-2014.R

predict-all:
	Rscript scripts/predict-wm-2014.R

clean:
	rm -rf output/*
