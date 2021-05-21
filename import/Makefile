SHELL:=$(shell which bash)
TAG=$(shell date -d "$$(date +%Y-%m-01) -1 day" +%Y%m%d)
WGET=wget
PSQL=psql -h localhost -U ruian -v ON_ERROR_STOP=on
UNZIP=unzip
OKRESY_URL="https://vdp.cuzk.cz/vdp/ruian/okresy/export?vc.kod=&ok.nazev=&ok.kod=&ohrada.id=&okg.sort=UZEMI&export=CSV"
ADDR_URL=https://vdp.cuzk.cz/vymenny_format/csv/$(TAG)_OB_ADR_csv.zip
STRUCT_URL=https://vdp.cuzk.cz/vymenny_format/csv/$(TAG)_strukt_ADR.csv.zip
T=tmp

wdir=$T/.dir
okresy_csv=$T/okresy.csv
okresy_imp=$T/okresy.imp
addr_zip=$T/addr.zip
addr_cmd=$T/addr.cmd
addr_imp=$T/addr.imp
struct_zip=$T/struct.zip
struct_imp=$T/struct.imp

.PHONY: all download import refresh clean

all: download import refresh

download: $(okresy_csv) $(addr_zip) $(struct_zip)

import: $(okresy_imp) $(struct_imp) $(addr_imp)

refresh:
	@echo "BEGIN; \
	REFRESH MATERIALIZED VIEW kraj; \
	REFRESH MATERIALIZED VIEW okres; \
	REFRESH MATERIALIZED VIEW obec; \
	REFRESH MATERIALIZED VIEW cast_obce; \
	REFRESH MATERIALIZED VIEW ulice; \
	REFRESH MATERIALIZED VIEW momc; \
	REFRESH MATERIALIZED VIEW mop; \
	REFRESH MATERIALIZED VIEW adresni_misto; \
	COMMIT;" | \
	$(PSQL)

$(addr_zip): $(wdir)
	$(WGET) -O $@.tmp $(ADDR_URL)
	mv $@.tmp $@ && touch $@

$(addr_cmd): $(addr_zip)
	rm -fr $T/CSV
	$(UNZIP) -d $T $<
	@echo "BEGIN;" > $@.tmp
	@echo "TRUNCATE TABLE ruian_adresni_misto;" >> $@.tmp
	for i in $T/CSV/*.csv; do \
		echo "\copy ruian_adresni_misto FROM '$$i' \
		WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE, ENCODING 'windows-1250');"; \
	done >> $@.tmp
	@echo "COMMIT; " >> $@.tmp
	mv $@.tmp $@

$(addr_imp): $(addr_cmd) $(struct_imp)
	$(PSQL) -f $<
	touch $@

$(struct_zip): $(wdir)
	$(WGET) -O $@.tmp $(STRUCT_URL)
	mv $@.tmp $@ && touch $@

$(struct_imp): $(struct_zip)
	rm -fr $T/strukturovane-CSV
	$(UNZIP) -d $T $<
	@echo "BEGIN; \
	TRUNCATE TABLE ruian_vazby_cr; \
	\copy ruian_vazby_cr FROM '$T/strukturovane-CSV/vazby-cr.csv' \
	WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE, ENCODING 'windows-1250'); \
	COMMIT; " | \
	$(PSQL) 
	touch $@

$(okresy_csv): $(wdir)
	$(WGET) -O $@.tmp $(OKRESY_URL)
	expr `cat $@.tmp | wc -l` '>' 70 
	mv $@.tmp $@ && touch $@

$(okresy_imp): $(okresy_csv)
	@echo "BEGIN; \
	TRUNCATE TABLE ruian_okres; \
	\copy ruian_okres FROM '$<' \
	WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE, ENCODING 'utf-8'); \
	COMMIT; " | \
	$(PSQL) 
	touch $@

$(wdir):
	mkdir -p $(T)
	touch $@

clean:
	@rm -fr $T
