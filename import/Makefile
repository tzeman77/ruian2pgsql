SHELL:=$(shell which bash)
TAG=$(shell date -d "$$(date +%Y-%m-01) -1 day" +%Y%m%d)
WGET=wget
PSQL=psql -h localhost -U ruian -v ON_ERROR_STOP=on
UNZIP=unzip
OKRESY_URL=https://services.cuzk.cz/sestavy/cis/UI_OKRES.zip
KRAJE_URL=https://services.cuzk.cz/sestavy/cis/UI_VUSC.zip
ADDR_URL=https://vdp.cuzk.cz/vymenny_format/csv/$(TAG)_OB_ADR_csv.zip
STRUCT_URL=https://vdp.cuzk.cz/vymenny_format/csv/$(TAG)_strukt_ADR.csv.zip
T=tmp

wdir=$T/.dir
kraje_zip=$T/UI_VUSC.zip
kraje_csv=$T/UI_VUSC.csv
kraje_imp=$T/UI_VUSC.imp
okresy_zip=$T/UI_OKRES.zip
okresy_csv=$T/UI_OKRES.csv
okresy_imp=$T/UI_OKRES.imp
addr_zip=$T/addr.zip
addr_cmd=$T/addr.cmd
addr_imp=$T/addr.imp
struct_zip=$T/struct.zip
struct_imp=$T/struct.imp

.PHONY: all download import refresh clean

all: download import refresh

download: $(kraje_csv) $(okresy_csv) $(addr_zip) $(struct_zip)

import: $(kraje_imp) $(okresy_imp) $(struct_imp) $(addr_imp)

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

$(kraje_zip): $(wdir)
	$(WGET) -O $@.tmp $(KRAJE_URL)
	mv $@.tmp $@ && touch $@

$(kraje_csv): $(kraje_zip)
	$(UNZIP) -o -d $T $<
	expr `cat $@ | wc -l` '>' 10 
	touch $@

$(kraje_imp): $(kraje_csv)
	@echo "BEGIN; \
	TRUNCATE TABLE ruian_kraj; \
	\copy ruian_kraj \
	FROM '$<' WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE, ENCODING 'windows-1250'); \
	COMMIT; " | \
	$(PSQL) 
	touch $@

$(okresy_zip): $(wdir)
	$(WGET) -O $@.tmp $(OKRESY_URL)
	mv $@.tmp $@ && touch $@

$(okresy_csv): $(okresy_zip)
	$(UNZIP) -o -d $T $<
	expr `cat $@ | wc -l` '>' 70 
	touch $@

$(okresy_imp): $(okresy_csv)
	@echo "BEGIN; \
	TRUNCATE TABLE ruian_okres; \
	\copy ruian_okres \
	(kod_okresu, nazev_okresu, kod_kraje, nuts_lau, plati_od, plati_do, datum_vzniku) \
	FROM '$<' WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE, ENCODING 'windows-1250'); \
	UPDATE ruian_okres o \
	SET nazev_kraje = k.nazev_kraje \
	FROM ruian_kraj k WHERE o.kod_kraje = k.kod_kraje; \
	COMMIT; " | \
	$(PSQL) 
	touch $@

$(wdir):
	mkdir -p $(T)
	touch $@

clean:
	@rm -fr $T
