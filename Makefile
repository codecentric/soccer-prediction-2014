all: merge-data clean-data compute-features

merge-data:
	$(MAKE) -C 1-merge-data

clean-data:
	$(MAKE) -C 2-clean-data

compute-features:
	$(MAKE) -C 3-compute-features

clean:
	rm -rf output/*
	$(MAKE) -C 1-merge-data clean
	$(MAKE) -C 2-clean-data clean
	$(MAKE) -C 3-compute-features clean
