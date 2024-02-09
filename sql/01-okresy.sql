BEGIN;

ALTER TABLE ruian_okres ADD COLUMN nuts_lau varchar;
ALTER TABLE ruian_okres ADD COLUMN plati_od varchar;
ALTER TABLE ruian_okres ADD COLUMN plati_do varchar;
ALTER TABLE ruian_okres ADD COLUMN datum_vzniku varchar;
ALTER TABLE ruian_okres ALTER COLUMN nazev_kraje DROP NOT NULL; 
COMMENT ON TABLE ruian_okres IS 'Import: Okresy RUIAN';
ALTER TABLE ruian_vazby_cr ALTER COLUMN kod_okresu DROP NOT NULL;

COMMIT;

-- vim: et ts=2 sw=2 
