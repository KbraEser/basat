CREATE MATERIALIZED VIEW readings_15m
WITH (timescaledb.continuous, timescaledb.materialized_only = false) AS
SELECT meter_id,
       time_bucket(INTERVAL '15 minutes', measured_at) AS bucket,
       sum(energy_kwh) AS energy_kwh,
       count(*)        AS reading_count
FROM readings
GROUP BY meter_id, bucket;

CREATE MATERIALIZED VIEW readings_hourly
WITH (timescaledb.continuous, timescaledb.materialized_only = false) AS
SELECT meter_id,
       time_bucket(INTERVAL '1 hour', bucket) AS bucket,
       sum(energy_kwh)    AS energy_kwh,
       sum(reading_count) AS reading_count
FROM readings_15m
GROUP BY meter_id, time_bucket(INTERVAL '1 hour', bucket);

CREATE MATERIALIZED VIEW readings_daily
WITH (timescaledb.continuous, timescaledb.materialized_only = false) AS
SELECT meter_id,
       time_bucket(INTERVAL '1 day', bucket, 'Europe/Istanbul') AS bucket,
       sum(energy_kwh)    AS energy_kwh,
       sum(reading_count) AS reading_count
FROM readings_hourly
GROUP BY meter_id, time_bucket(INTERVAL '1 day', bucket, 'Europe/Istanbul');

SELECT add_continuous_aggregate_policy('readings_15m',
                                       start_offset => INTERVAL '1 day', end_offset => INTERVAL '15 minutes', schedule_interval => INTERVAL '5 minutes');
SELECT add_continuous_aggregate_policy('readings_hourly',
                                       start_offset => INTERVAL '3 days', end_offset => INTERVAL '1 hour', schedule_interval => INTERVAL '30 minutes');
SELECT add_continuous_aggregate_policy('readings_daily',
                                       start_offset => INTERVAL '30 days', end_offset => INTERVAL '1 day', schedule_interval => INTERVAL '1 hour');