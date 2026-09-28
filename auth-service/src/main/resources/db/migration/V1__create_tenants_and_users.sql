CREATE TABLE tenants (
                         id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         name       VARCHAR(200) NOT NULL,
                         created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE users (
                       id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       tenant_id     UUID         NOT NULL REFERENCES tenants (id),
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name     VARCHAR(200) NOT NULL,
                       role          VARCHAR(30)  NOT NULL CHECK (role IN ('ADMIN', 'ENERGY_MANAGER', 'VIEWER')),
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_tenant_id ON users (tenant_id);

