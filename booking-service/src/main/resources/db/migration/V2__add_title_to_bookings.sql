-- Adiciona a coluna title.
-- O DEFAULT 'Reunião' garante que as reservas antigas não quebrem por falta de título.
ALTER TABLE bookings ADD COLUMN title VARCHAR(255) NOT NULL DEFAULT 'Reunião';