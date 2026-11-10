CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE readings (
    meter_id    UUID          NOT NULL REFERENCES meters (id),
    measured_at TIMESTAMPTZ   NOT NULL,
    energy_kwh  NUMERIC(12,4) NOT NULL CHECK (energy_kwh >= 0),
    PRIMARY KEY (meter_id, measured_at)
);

SELECT create_hypertable('readings', by_range('measured_at', INTERVAL '7 days'));