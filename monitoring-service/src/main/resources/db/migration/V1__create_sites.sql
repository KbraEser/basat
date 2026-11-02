CREATE TABLE sites (
                       id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       tenant_id     UUID         NOT NULL,
                       name          VARCHAR(200) NOT NULL,
                       site_type     VARCHAR(30)  NOT NULL CHECK (site_type IN ('OFFICE', 'FACTORY', 'HOTEL', 'SCHOOL', 'RETAIL', 'HOSPITAL', 'SOLAR_PLANT', 'OTHER')),
                       floor_area_m2 NUMERIC(10, 2) CHECK (floor_area_m2 > 0),
                       city          VARCHAR(100),
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       CONSTRAINT uk_sites_tenant_name UNIQUE (tenant_id, name)
);