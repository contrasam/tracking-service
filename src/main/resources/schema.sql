-- Create tracking sequence
CREATE SEQUENCE IF NOT EXISTS tracking_sequence START WITH 1 INCREMENT BY 1;

-- Create tracking_number table
CREATE TABLE IF NOT EXISTS tracking_number (
    id VARCHAR(255) PRIMARY KEY,
    shipper_code VARCHAR(4) NOT NULL,
    tracking_number VARCHAR(16) NOT NULL,
    created_at VARCHAR(30) NOT NULL,
    weight DOUBLE,
    source_country_code VARCHAR(2),
    destination_country_code VARCHAR(2)
);

-- Create index on tracking_number for faster lookups
CREATE INDEX IF NOT EXISTS idx_tracking_number ON tracking_number(tracking_number);
