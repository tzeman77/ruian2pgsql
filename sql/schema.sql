BEGIN;

DROP MATERIALIZED VIEW IF EXISTS v_adresa;
DROP MATERIALIZED VIEW IF EXISTS v_okres;
DROP MATERIALIZED VIEW IF EXISTS v_kraj;
DROP MATERIALIZED VIEW IF EXISTS v_obec;
DROP MATERIALIZED VIEW IF EXISTS v_obec_okres;
DROP MATERIALIZED VIEW IF EXISTS v_ulice;
DROP TABLE IF EXISTS ruian_adresy;
DROP TABLE IF EXISTS ruian_okresy;
DROP TABLE IF EXISTS ruian_vazby_cr;

CREATE TABLE ruian_adresy (
  id                      integer        NOT NULL,
  obec_id                 integer        NOT NULL,
  nazev_obce              varchar    NOT NULL,
  momc_id                 integer    ,
  nazev_momc              varchar    ,
  mop_id                  integer    ,
  nazev_mop               varchar    ,
  cast_obce_id           integer        ,
  nazev_casti_obce        varchar    ,
  ulice_id                integer    ,
  nazev_ulice             varchar    ,
  typ_so                  varchar(16)    ,
  cislo_domovni           integer        ,
  cislo_orientacni        integer        ,
  znak_cisla_orientacniho varchar(4)     ,
  psc                     integer   ,
  souradnice_y            decimal(12, 2) ,
  souradnice_x            decimal(12, 2) ,
  plati_od                timestamp       ,
  PRIMARY KEY (id)
);

COMMENT ON TABLE ruian_adresy IS 'Import: Adresni mista RUIAN';

CREATE TABLE ruian_okresy (
  okres_id integer NOT NULL,
  nazev_okresu varchar NOT NULL,
  kraj_id integer NOT NULL,
  nazev_kraje varchar NOT NULL,
  PRIMARY KEY (okres_id)
);

COMMENT ON TABLE ruian_okresy IS 'Import: Okresy a kraje RUIAN';

CREATE TABLE ruian_vazby_cr (
  cast_obce_id integer NOT NULL,
  obec_id      integer NOT NULL,
  pou_id     integer NOT NULL,
  orp_id integer NOT NULL,
  okres_id integer NOT NULL,
  kraj_id integer NOT NULL,
  regsoudr_id integer NOT NULL,
  stat_id      integer NOT NULL,
  PRIMARY KEY (cast_obce_id)
);

COMMENT ON TABLE ruian_vazby_cr IS 'Import: Vazby entit RUIAN';

CREATE MATERIALIZED VIEW v_kraj AS
SELECT DISTINCT
  kraj_id AS id
, nazev_kraje AS nazev
FROM ruian_okresy
ORDER BY kraj_id;

COMMENT ON MATERIALIZED VIEW v_kraj IS 'Ciselnik kraju';

CREATE UNIQUE INDEX ON v_kraj (id);

CREATE MATERIALIZED VIEW v_okres AS
SELECT DISTINCT
  okres_id AS id
, nazev_okresu AS nazev
, kraj_id
FROM ruian_okresy
ORDER BY okres_id;

COMMENT ON MATERIALIZED VIEW v_okres IS 'Ciselnik okresu';

CREATE UNIQUE INDEX ON v_okres (id);
CREATE INDEX ON v_okres (kraj_id);

CREATE MATERIALIZED VIEW v_obec_okres AS
SELECT DISTINCT
  obec_id
, okres_id 
FROM ruian_vazby_cr
ORDER BY obec_id;

COMMENT ON MATERIALIZED VIEW v_obec_okres IS 'Vazba obec - okres';

CREATE UNIQUE INDEX ON v_obec_okres (obec_id, okres_id);

CREATE MATERIALIZED VIEW v_obec AS
SELECT DISTINCT
  obec_id AS id
, nazev_obce AS nazev
, okres_id
FROM ruian_adresy
JOIN v_obec_okres USING (obec_id)
ORDER BY obec_id;

COMMENT ON MATERIALIZED VIEW v_obec IS 'Ciselnik obci';

CREATE UNIQUE INDEX ON v_obec (id);
CREATE INDEX ON v_obec (okres_id);

CREATE MATERIALIZED VIEW v_ulice AS
SELECT DISTINCT
  ulice_id AS id
, nazev_ulice AS nazev
, obec_id
FROM ruian_adresy
ORDER BY ulice_id;

COMMENT ON MATERIALIZED VIEW v_ulice IS 'Ciselnik obci';

CREATE UNIQUE INDEX ON v_ulice (id);
CREATE INDEX ON v_ulice (obec_id);

CREATE MATERIALIZED VIEW v_adresa AS
SELECT
  id
, momc_id
, nazev_momc
, mop_id
, nazev_mop
, cast_obce_id
, nazev_casti_obce
, ulice_id
, typ_so
, cislo_domovni
, cislo_orientacni
, znak_cisla_orientacniho
, psc
, POINT(souradnice_x, souradnice_y) AS jtsk
, plati_od
FROM ruian_adresy
ORDER BY id;

COMMENT ON MATERIALIZED VIEW v_adresa IS 'Adresni mista';
COMMENT ON COLUMN v_adresa.id IS 'Kod adresniho mista';
COMMENT ON COLUMN v_adresa.momc_id IS 'Kod mestskeho obvodu/mestske casti';
COMMENT ON COLUMN v_adresa.nazev_momc IS 'Nazev mestskeho obvodu/mestske casti';
COMMENT ON COLUMN v_adresa.mop_id IS 'Kod obvodu Prahy';
COMMENT ON COLUMN v_adresa.nazev_mop IS 'Nazev obvodu Prahy';
COMMENT ON COLUMN v_adresa.cast_obce_id IS 'Kod casti obce';
COMMENT ON COLUMN v_adresa.nazev_casti_obce IS 'Nazev casti obce';
COMMENT ON COLUMN v_adresa.ulice_id IS 'Kod ulice';
COMMENT ON COLUMN v_adresa.typ_so IS 'Typ stavebniho objektu, tj. cislo popisne nebo evidencni';
COMMENT ON COLUMN v_adresa.cislo_domovni IS 'Cislo popisne nebo evidencni';
COMMENT ON COLUMN v_adresa.cislo_orientacni IS 'Cislo orientacni';
COMMENT ON COLUMN v_adresa.znak_cisla_orientacniho IS 'Znak cisla orientacniho, pokud je znak pridelen';
COMMENT ON COLUMN v_adresa.psc IS 'PSC';
COMMENT ON COLUMN v_adresa.jtsk IS 'Souradnice v S-JTSK uvedene v [m]';
COMMENT ON COLUMN v_adresa.plati_od IS 'Datum platnosti adresniho mista';

CREATE UNIQUE INDEX ON v_adresa (id);
CREATE INDEX ON v_adresa (ulice_id);
CREATE INDEX ON v_adresa (psc);
CREATE INDEX ON v_adresa USING GIST (jtsk);

COMMIT;

-- vim: et ts=2 sw=2 
