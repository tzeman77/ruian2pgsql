TAG=$(shell date -d "$$(date +%Y-%m-01) -1 day" +%Y%m%d)
WGET=wget
PSQL=psql -h localhost -U ruian
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

.PHONY: all download import clean

all: download import

download: $(okresy_csv) $(addr_zip) $(struct_zip)

import: $(okresy_imp) $(struct_imp) $(addr_imp)

$(addr_zip): $(wdir)
	$(WGET) -O $@.tmp $(ADDR_URL)
	mv $@.tmp $@

$(addr_cmd): $(addr_zip)
	rm -fr $T/CSV
	$(UNZIP) -d $T $<
	@echo "BEGIN;" > $@.tmp
	@echo "TRUNCATE TABLE ruian_adresy;" >> $@.tmp
	for i in $T/CSV/*.csv; do \
		echo "\copy ruian_adresy FROM '$$i' \
		WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE, ENCODING 'windows-1250');"; \
	done >> $@.tmp
	@echo "REFRESH MATERIALIZED VIEW v_obec; \
	REFRESH MATERIALIZED VIEW v_ulice; \
	REFRESH MATERIALIZED VIEW v_adresa; \
	COMMIT; " >> $@.tmp
	mv $@.tmp $@

$(addr_imp): $(addr_cmd) $(struct_imp)
	$(PSQL) -f $<
	touch $@

$(struct_zip): $(wdir)
	$(WGET) -O $@.tmp $(STRUCT_URL)
	mv $@.tmp $@

$(struct_imp): $(struct_zip)
	rm -fr $T/strukturovane-CSV
	$(UNZIP) -d $T $<
	@echo "BEGIN; \
	TRUNCATE TABLE ruian_vazby_cr; \
	\copy ruian_vazby_cr FROM '$T/strukturovane-CSV/vazby-cr.csv' \
	WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE, ENCODING 'windows-1250'); \
	REFRESH MATERIALIZED VIEW v_obec_okres; \
	COMMIT; " | \
	$(PSQL) 
	touch $@

$(okresy_csv): $(wdir)
	$(WGET) -O $@.tmp $(OKRESY_URL)
	mv $@.tmp $@

$(okresy_imp): $(okresy_csv)
	@echo "BEGIN; \
	TRUNCATE TABLE ruian_okresy; \
	\copy ruian_okresy FROM '$<' \
	WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE, ENCODING 'utf-8'); \
	REFRESH MATERIALIZED VIEW v_okres; \
	REFRESH MATERIALIZED VIEW v_kraj; \
	COMMIT; " | \
	$(PSQL) 
	touch $@

$(wdir):
	mkdir -p $(T)
	touch $@

clean:
	@rm -fr $T
