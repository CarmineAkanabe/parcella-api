CREATE TABLE locations (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    address VARCHAR(500) NOT NULL,
    point   geography(Point, 4326) NOT NULL
);

CREATE TABLE users (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role                     VARCHAR(20)  NOT NULL,
    full_name                VARCHAR(150) NOT NULL,
    email                    VARCHAR(255) NOT NULL,
    password_hash            VARCHAR(100) NOT NULL,
    payment_phone_number     VARCHAR(12),
    preferred_contact_number VARCHAR(12),
    registered_location_id   UUID REFERENCES locations (id),
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- ADMIN only
    staff_id                 VARCHAR(50),

    -- DRIVER only
    verification_status      VARCHAR(20),
    availability_status      VARCHAR(20),
    license_image            VARCHAR(500),
    average_rating           NUMERIC(3, 2),
    trust_score              NUMERIC(5, 2),

    CONSTRAINT ck_users_role
        CHECK (role IN ('CUSTOMER', 'DRIVER', 'ADMIN')),

    CONSTRAINT ck_users_verification_status
        CHECK (verification_status IN ('PENDING', 'APPROVED', 'REJECTED')),

    CONSTRAINT ck_users_availability_status
        CHECK (availability_status IN ('AVAILABLE', 'BUSY', 'OFFLINE')),

    -- customers and drivers need contact details and a home location; admins don't
    CONSTRAINT ck_users_non_admin_profile
        CHECK (role = 'ADMIN' OR (registered_location_id IS NOT NULL
                                  AND payment_phone_number IS NOT NULL
                                  AND preferred_contact_number IS NOT NULL)),

    -- drivers must carry their driver-only fields
    CONSTRAINT ck_users_driver_fields
        CHECK (role <> 'DRIVER' OR (verification_status IS NOT NULL
                                    AND availability_status IS NOT NULL
                                    AND average_rating IS NOT NULL
                                    AND trust_score IS NOT NULL))
);

CREATE UNIQUE INDEX uq_users_email ON users (lower(email));
CREATE INDEX idx_users_role ON users (role);