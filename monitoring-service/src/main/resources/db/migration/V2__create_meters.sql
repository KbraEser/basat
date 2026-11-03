CREATE TABLE meters (
                        id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        site_id       UUID         NOT NULL REFERENCES sites (id),
                        name          VARCHAR(200) NOT NULL,
                        serial_number VARCHAR(100) NOT NULL UNIQUE,
                        meter_type    VARCHAR(30)  NOT NULL CHECK (meter_type IN ('ELECTRICITY', 'SOLAR')),
                        created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_meters_site_id ON meters (site_id);