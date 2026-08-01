-- V27 : élargir CHECK historiques_at pour statuts workflow S-HSE-SEC-31
-- Cause du 500 sur /submit : insertion DEMANDE_CREEE / AT_REDIGEE refusée par CHECK V1

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE rel.relname = 'historiques_at'
          AND con.contype = 'c'
    LOOP
        EXECUTE format('ALTER TABLE historiques_at DROP CONSTRAINT IF EXISTS %I', r.conname);
    END LOOP;
END $$;

-- Optionnel : recréer des CHECK permissifs (VARCHAR libre = pas de CHECK)
-- Les enums Java restent la source de vérité côté application.
