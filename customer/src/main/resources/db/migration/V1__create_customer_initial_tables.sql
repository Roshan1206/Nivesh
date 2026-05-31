SET SEARCH_PATH TO customer;
CREATE SEQUENCE seq_customer_number START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;

CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE', 'OTHER');
CREATE TYPE kyc_status_enum AS ENUM ('PENDING', 'IN_PROGRESS', 'VERIFIED', 'REJECTED', 'EXPIRED');
CREATE TYPE contact_type AS ENUM ('PRIMARY', 'SECONDARY');
CREATE TYPE risk_profile_enum AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH', 'BLOCKED');
CREATE TYPE address_type_enum AS ENUM ('PERMANENT', 'CURRENT', 'CORRESPONDENCE', 'ALTERNATE');
CREATE TYPE address_status_enum AS ENUM ('UNVERIFIED', 'PROOF_SUBMITTED', 'VERIFICATION_IN_PROGRESS',
    'VERIFIED', 'VERIFICATION_FAILED', 'REVERIFICATION_REQUIRED');

CREATE TABLE customers(
    customer_id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    customer_number VARCHAR(8) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    ifsc_code VARCHAR(11),
    gender gender_enum NOT NULL,
    kyc_status kyc_status_enum NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50)
);


CREATE TABLE contacts (
    contact_id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    mobile_number VARCHAR(10) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    contact_type contact_type,
    is_verified BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    CONSTRAINT fk_contact_customer FOREIGN KEY(customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE addresses(
    address_id UUID PRIMARY KEY,
    street_line_1 VARCHAR(100) NOT NULL,
    street_line_2 VARCHAR(100),
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pin_code VARCHAR(6) NOT NULL,
    country VARCHAR(50) NOT NULL,
    address_type address_type_enum,
    address_status address_status_enum,
    customer_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TYPE document_type_enum AS ENUM ('PAN_CARD', 'AADHAR_CARD', 'PASSPORT', 'VOTER_ID');
CREATE TYPE kyc_verification_enum AS ENUM ('SYSTEM_UIDAI', 'SYSTEM_NSDL', 'SYSTEM_VIDEO_KYC', 'SYSTEM_DIGI_LOCKER', 'EMPLOYEE');

CREATE TABLE kyc_documents(
    document_id UUID PRIMARY KEY,
    document_type document_type_enum NOT NULL,
    document_number VARCHAR(20) NOT NULL UNIQUE,
    file_path VARCHAR(100),
    verified_by kyc_verification_enum,
    emp_no VARCHAR(10),
    customer_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    CONSTRAINT fk_document_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TYPE relation_enum AS ENUM ('MOTHER', 'FATHER', 'SPOUSE', 'SON', 'DAUGHTER');

CREATE TABLE nominees(
    nominee_id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    relation relation_enum NOT NULL,
    gender gender_enum NOT NULL,
    date_of_birth DATE NOT NULL,
    share_percentage NUMERIC(5,2) NOT NULL,
    guardian_name VARCHAR(50),
    customer_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(50),
    CONSTRAINT fk_nominee_customer FOREIGN KEY(customer_id) REFERENCES customers(customer_id)
);