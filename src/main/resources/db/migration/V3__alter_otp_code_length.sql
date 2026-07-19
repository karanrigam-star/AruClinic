-- src/main/resources/db/migration/V3__alter_otp_code_length.sql
ALTER TABLE otp_verifications MODIFY COLUMN otp_code VARCHAR(100) NOT NULL;
