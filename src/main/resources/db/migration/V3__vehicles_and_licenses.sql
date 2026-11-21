CREATE TABLE vehicles (
    id               uuid PRIMARY KEY,
    driver_id        uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type             varchar(20) NOT NULL,
    plate_number     varchar(20) NOT NULL,
    max_package_size varchar(10) NOT NULL,
    active           boolean     NOT NULL DEFAULT false,
    created_at       timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT vehicles_type_chk CHECK (type IN ('BIKE', 'MOTORBIKE', 'CAR', 'VAN')),
    CONSTRAINT vehicles_size_chk CHECK (max_package_size IN ('SMALL', 'MEDIUM', 'LARGE'))
);

CREATE INDEX idx_vehicles_driver ON vehicles (driver_id);

-- plates are unique regardless of casing
CREATE UNIQUE INDEX uq_vehicles_plate ON vehicles (upper(plate_number));

-- partial unique index: at most ONE active vehicle per driver
CREATE UNIQUE INDEX uq_vehicles_one_active_per_driver ON vehicles (driver_id) WHERE active;

-- adding constraint for security reasons
ALTER TABLE users ADD CONSTRAINT users_license_driver_only_chk
    CHECK (role = 'DRIVER' OR license_image IS NULL);