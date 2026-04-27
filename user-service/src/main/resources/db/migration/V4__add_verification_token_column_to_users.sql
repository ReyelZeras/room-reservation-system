ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255);
-- Garante que novos utilizadores locais nascem inativos
ALTER TABLE users ALTER COLUMN active SET DEFAULT false;