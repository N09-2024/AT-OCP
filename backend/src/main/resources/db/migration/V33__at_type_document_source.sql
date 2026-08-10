ALTER TABLE autorisations_travail ADD COLUMN type_document_source VARCHAR(10);
ALTER TABLE autorisations_travail ADD COLUMN numero_document_source VARCHAR(50);
UPDATE autorisations_travail SET type_document_source = 'DI' WHERE di_id IS NOT NULL;
UPDATE autorisations_travail SET type_document_source = 'OT' WHERE ot_id IS NOT NULL;
UPDATE autorisations_travail SET type_document_source = 'BT' WHERE bt_id IS NOT NULL;
-- di_id / ot_id / bt_id conservées pour l'historique, plus jamais NOT NULL/unique bloquant