all: merge-data compute-features

merge-data:
	$(MAKE) -C 1-merge-data

compute-features:
	$(MAKE) -C 2-compute-features

clean:
	rm -rf output/*
	$(MAKE) -C 1-merge-data clean
	$(MAKE) -C 2-compute-features clean
