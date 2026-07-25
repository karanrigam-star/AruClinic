-- src/main/resources/db/migration/V4__sync_schema_updates.sql
-- Production schema synchronization for AruClinic

ALTER TABLE doctors MODIFY user_id BIGINT NULL;
ALTER TABLE patients MODIFY user_id BIGINT NULL;
ALTER TABLE receptionists MODIFY user_id BIGINT NULL;

ALTER TABLE appointments ADD COLUMN IF NOT EXISTS appointment_date_time DATETIME NULL;

ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS symptoms TEXT NULL;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS diagnosis TEXT NULL;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS advice TEXT NULL;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS follow_up_date DATE NULL;

ALTER TABLE prescription_items ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE prescription_items ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
