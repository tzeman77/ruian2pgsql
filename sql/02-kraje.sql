BEGIN;

CREATE TABLE ruian_kraj (
  kod_kraje               integer NOT NULL,  -- Kód Kraje (VÚSC)
  nazev_kraje             varchar NOT NULL,  -- Název Kraje (VÚSC)
  regsoudr_kod            varchar NOT NULL,  -- Kód regionu soudržnosti [UI_REGION_SOUDRZNOSTI]
  nuts_lau                varchar,           -- Kód NUTS/LAU
  plati_od                varchar,           -- Datum aktualizace VÚSC
  plati_do                varchar,           -- Datum zániku VÚSC
  datum_vzniku            varchar,           -- Datum vzniku VÚSC
  PRIMARY KEY (kod_kraje)
);

COMMENT ON TABLE ruian_okres IS 'Import: Kraje RUIAN';

COMMIT;

-- vim: et ts=2 sw=2 
