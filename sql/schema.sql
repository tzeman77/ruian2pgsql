-- Before creating the schema,
-- execute as superuser:
-- CREATE EXTENSION unaccent;

BEGIN;

DROP MATERIALIZED VIEW IF EXISTS kraj;
DROP MATERIALIZED VIEW IF EXISTS okres;
DROP MATERIALIZED VIEW IF EXISTS obec;
DROP MATERIALIZED VIEW IF EXISTS cast_obce;
DROP MATERIALIZED VIEW IF EXISTS ulice;
DROP MATERIALIZED VIEW IF EXISTS momc;
DROP MATERIALIZED VIEW IF EXISTS mop;
DROP MATERIALIZED VIEW IF EXISTS adresni_misto;
DROP TABLE IF EXISTS ruian_adresni_misto;
DROP TABLE IF EXISTS ruian_okres;
DROP TABLE IF EXISTS ruian_vazby_cr;

-- IMPORT TABLES

CREATE UNLOGGED TABLE ruian_adresni_misto (
  kod_adm                 integer NOT NULL,  -- Kód ADM
  kod_obce                integer NOT NULL,  -- Kód obce
  nazev_obce              varchar NOT NULL,  -- Název obce
  kod_momc                integer,           -- Kód MOMC
  nazev_momc              varchar,           -- Název MOMC
  kod_mop                 integer,           -- Kód obvodu Prahy
  nazev_mop               varchar,           -- Název obvodu Prahy
  kod_casti_obce          integer NOT NULL,  -- Kód části obce
  nazev_casti_obce        varchar NOT NULL,  -- Název části obce
  kod_ulice               integer,           -- Kód ulice
  nazev_ulice             varchar,           -- Název ulice
  typ_so                  varchar(16) NOT NULL, -- Typ SO
  cislo_domovni           integer NOT NULL,  -- Číslo domovní
  cislo_orientacni        integer,           -- Číslo orientační
  znak_cisla_orientacniho varchar(4),        -- Znak čísla orientačního
  psc                     integer NOT NULL,  -- PSČ
  souradnice_y            decimal(12, 2),    -- Souřadnice Y
  souradnice_x            decimal(12, 2),    -- Souřadnice X
  plati_od                timestamp,         -- Platí Od
  PRIMARY KEY (kod_adm)
);

COMMENT ON TABLE ruian_adresni_misto IS 'Import: Adresni mista RUIAN';

CREATE INDEX ON ruian_adresni_misto (kod_obce, nazev_obce);
CREATE INDEX ON ruian_adresni_misto (kod_casti_obce, nazev_casti_obce, kod_obce);
CREATE INDEX ON ruian_adresni_misto (kod_ulice, nazev_ulice, kod_obce);

CREATE TABLE ruian_okres (
  kod_okresu              integer NOT NULL,  -- Kód
  nazev_okresu            varchar NOT NULL,  -- Název Okresu
  kod_kraje               integer NOT NULL,  -- Kód Kraje (VÚSC)
  nazev_kraje             varchar NOT NULL,  -- Název Kraje (VÚSC)
  PRIMARY KEY (kod_okresu)
);

COMMENT ON TABLE ruian_okres IS 'Import: Okresy a kraje RUIAN';

CREATE TABLE ruian_vazby_cr (
  kod_casti_obce          integer NOT NULL,  -- COBCE_KOD
  kod_obce                integer NOT NULL,  -- OBEC_KOD
  kod_pou                 integer NOT NULL,  -- POU_KOD
  kod_orp                 integer NOT NULL,  -- ORP_KOD
  kod_okresu              integer NOT NULL,  -- OKRES_KOD
  kod_kraje               integer NOT NULL,  -- VUSC_KOD
  kod_regsoudr            integer NOT NULL,  -- REGSOUDR_KOD
  kod_statu               integer NOT NULL,  -- STAT_KOD
  PRIMARY KEY (kod_casti_obce)
);

COMMENT ON TABLE ruian_vazby_cr IS 'Import: Vazby entit RUIAN';

CREATE INDEX ON ruian_vazby_cr (kod_obce);

-- KRAJ

CREATE MATERIALIZED VIEW kraj AS
SELECT DISTINCT
  kod_kraje AS kod
, nazev_kraje AS nazev
, TO_TSVECTOR(UNACCENT(nazev_kraje)) AS nazev_tsv
FROM ruian_okres
ORDER BY kod_kraje;

COMMENT ON MATERIALIZED VIEW kraj IS 'Ciselnik kraju';

CREATE UNIQUE INDEX ON kraj (kod);
CREATE INDEX ON kraj USING gin (nazev_tsv);

-- OKRES

CREATE MATERIALIZED VIEW okres AS
SELECT DISTINCT
  kod_okresu AS kod
, nazev_okresu AS nazev
, kod_kraje
, TO_TSVECTOR(UNACCENT(nazev_okresu)) AS nazev_tsv
FROM ruian_okres
ORDER BY kod_okresu;

COMMENT ON MATERIALIZED VIEW okres IS 'Ciselnik okresu';

CREATE UNIQUE INDEX ON okres (kod);
CREATE INDEX ON okres (kod_kraje);
CREATE INDEX ON okres USING gin (nazev_tsv);

-- OBEC

CREATE MATERIALIZED VIEW obec AS
SELECT DISTINCT
  kod_obce AS kod
, nazev_obce AS nazev
, kod_okresu
, kod_kraje
, TO_TSVECTOR(UNACCENT(nazev_obce)) AS nazev_tsv
FROM ruian_adresni_misto
JOIN ruian_vazby_cr USING (kod_obce)
ORDER BY kod_obce;

COMMENT ON MATERIALIZED VIEW obec IS 'Ciselnik obci';

CREATE UNIQUE INDEX ON obec (kod);
CREATE INDEX ON obec (kod_okresu);
CREATE INDEX ON obec (kod_kraje);
CREATE INDEX ON obec USING gin (nazev_tsv);

-- CAST OBCE

CREATE MATERIALIZED VIEW cast_obce AS
SELECT DISTINCT
  kod_casti_obce AS kod
, nazev_casti_obce AS nazev
, kod_obce
, TO_TSVECTOR(UNACCENT(nazev_casti_obce)) AS nazev_tsv
FROM ruian_adresni_misto
WHERE kod_casti_obce IS NOT NULL
ORDER BY kod_casti_obce;

COMMENT ON MATERIALIZED VIEW cast_obce IS 'Ciselnik casti obci';

CREATE UNIQUE INDEX ON cast_obce (kod);
CREATE INDEX ON cast_obce (kod_obce);
CREATE INDEX ON cast_obce USING gin (nazev_tsv);

-- ULICE

CREATE MATERIALIZED VIEW ulice AS
SELECT DISTINCT
  kod_ulice AS kod
, nazev_ulice AS nazev
, kod_obce
, TO_TSVECTOR(UNACCENT(nazev_ulice)) AS nazev_tsv
FROM ruian_adresni_misto
WHERE kod_ulice IS NOT NULL
ORDER BY kod_ulice;

COMMENT ON MATERIALIZED VIEW ulice IS 'Ciselnik ulic';

CREATE UNIQUE INDEX ON ulice (kod);
CREATE INDEX ON ulice (kod_obce);
CREATE INDEX ON ulice USING gin (nazev_tsv);

-- MESTSKY OBVOD / MESTSKA CAST

CREATE MATERIALIZED VIEW momc AS
SELECT DISTINCT
  kod_momc AS kod
, nazev_momc AS nazev
, kod_obce
, TO_TSVECTOR(UNACCENT(nazev_momc)) AS nazev_tsv
FROM ruian_adresni_misto
WHERE kod_momc IS NOT NULL
ORDER BY kod_momc;

COMMENT ON MATERIALIZED VIEW momc IS 'Ciselnik mestskych obvodu/casti';

CREATE UNIQUE INDEX ON momc (kod);
CREATE INDEX ON momc (kod_obce);
CREATE INDEX ON momc USING gin (nazev_tsv);

-- MESTSKY OBVOD PRAHA 

CREATE MATERIALIZED VIEW mop AS
SELECT DISTINCT
  kod_mop AS kod
, nazev_mop AS nazev
, kod_obce
, TO_TSVECTOR(UNACCENT(nazev_mop)) AS nazev_tsv
FROM ruian_adresni_misto
WHERE kod_mop IS NOT NULL
ORDER BY kod_mop;

COMMENT ON MATERIALIZED VIEW mop IS 'Ciselnik mestskych obvodu Prahy';

CREATE UNIQUE INDEX ON mop (kod);
CREATE INDEX ON mop (kod_obce);
CREATE INDEX ON mop USING gin (nazev_tsv);

-- ADRESNI MISTO

CREATE MATERIALIZED VIEW adresni_misto AS
SELECT
  kod_adm AS kod
, kod_obce
, kod_momc
, kod_mop
, kod_casti_obce
, kod_ulice
, typ_so
, cislo_domovni
, cislo_orientacni
, znak_cisla_orientacniho
, psc
, POINT(souradnice_x, souradnice_y) AS jtsk
, plati_od
FROM ruian_adresni_misto
ORDER BY kod_adm;

COMMENT ON MATERIALIZED VIEW adresni_misto IS 'Adresni mista';
COMMENT ON COLUMN adresni_misto.kod IS 'Kod adresniho mista';
COMMENT ON COLUMN adresni_misto.kod_obce IS 'Kod obce';
COMMENT ON COLUMN adresni_misto.kod_momc IS 'Kod mestskeho obvodu/mestske casti';
COMMENT ON COLUMN adresni_misto.kod_mop IS 'Kod obvodu Prahy';
COMMENT ON COLUMN adresni_misto.kod_casti_obce IS 'Kod casti obce';
COMMENT ON COLUMN adresni_misto.kod_ulice IS 'Kod ulice';
COMMENT ON COLUMN adresni_misto.typ_so IS 'Typ stavebniho objektu, tj. cislo popisne nebo evidencni';
COMMENT ON COLUMN adresni_misto.cislo_domovni IS 'Cislo popisne nebo evidencni';
COMMENT ON COLUMN adresni_misto.cislo_orientacni IS 'Cislo orientacni';
COMMENT ON COLUMN adresni_misto.znak_cisla_orientacniho IS 'Znak cisla orientacniho, pokud je znak pridelen';
COMMENT ON COLUMN adresni_misto.psc IS 'PSC';
COMMENT ON COLUMN adresni_misto.jtsk IS 'Souradnice v S-JTSK uvedene v [m]';
COMMENT ON COLUMN adresni_misto.plati_od IS 'Datum platnosti adresniho mista';

CREATE UNIQUE INDEX ON adresni_misto (kod);
CREATE INDEX ON adresni_misto (kod_obce);
CREATE INDEX ON adresni_misto (kod_momc);
CREATE INDEX ON adresni_misto (kod_mop);
CREATE INDEX ON adresni_misto (kod_casti_obce);
CREATE INDEX ON adresni_misto (kod_ulice);
CREATE INDEX ON adresni_misto (psc);
CREATE INDEX ON adresni_misto USING GIST (jtsk);

COMMIT;

-- vim: et ts=2 sw=2 
