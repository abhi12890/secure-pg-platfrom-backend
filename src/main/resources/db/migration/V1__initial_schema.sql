CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(72) NOT NULL,
    role VARCHAR(16) NOT NULL,
    account_status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE rooms (
    id UUID PRIMARY KEY,
    room_number VARCHAR(32) NOT NULL UNIQUE,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    current_occupants_count INTEGER NOT NULL DEFAULT 0 CHECK (current_occupants_count >= 0 AND current_occupants_count <= capacity)
);

CREATE TABLE tenant_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    room_id UUID NOT NULL REFERENCES rooms(id),
    encrypted_name TEXT NOT NULL,
    encrypted_phone TEXT NOT NULL,
    encrypted_id_proof TEXT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE,
    monthly_rent_rate NUMERIC(12,2) NOT NULL CHECK (monthly_rent_rate >= 0),
    status VARCHAR(16) NOT NULL
);
CREATE INDEX idx_tenant_status ON tenant_profiles(status);

CREATE TABLE monthly_invoices (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant_profiles(id),
    billing_month INTEGER NOT NULL CHECK (billing_month BETWEEN 1 AND 12),
    billing_year INTEGER NOT NULL CHECK (billing_year >= 2000),
    base_rent NUMERIC(12,2) NOT NULL CHECK (base_rent >= 0),
    total_amount_due NUMERIC(12,2) NOT NULL CHECK (total_amount_due >= 0),
    amount_paid NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (amount_paid >= 0),
    due_date DATE NOT NULL,
    payment_status VARCHAR(16) NOT NULL,
    last_paid_date TIMESTAMPTZ,
    razorpay_order_id VARCHAR(128),
    CONSTRAINT uk_tenant_billing_period UNIQUE (tenant_id, billing_year, billing_month)
);
CREATE INDEX idx_invoice_tenant ON monthly_invoices(tenant_id);
CREATE INDEX idx_invoice_billing_period ON monthly_invoices(billing_year, billing_month);
CREATE INDEX idx_invoice_payment_status ON monthly_invoices(payment_status);
CREATE INDEX idx_invoice_last_paid_date ON monthly_invoices(last_paid_date);
CREATE UNIQUE INDEX uk_invoice_razorpay_order_id ON monthly_invoices(razorpay_order_id) WHERE razorpay_order_id IS NOT NULL;

CREATE TABLE guest_concerns (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant_profiles(id),
    category VARCHAR(32) NOT NULL,
    encrypted_description TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_concern_tenant ON guest_concerns(tenant_id);
CREATE INDEX idx_concern_status ON guest_concerns(status);

CREATE TABLE system_reminders (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant_profiles(id),
    invoice_id UUID NOT NULL REFERENCES monthly_invoices(id),
    days_until_due INTEGER NOT NULL CHECK (days_until_due BETWEEN 1 AND 3),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_reminder_tenant ON system_reminders(tenant_id);
CREATE INDEX idx_reminder_active ON system_reminders(active);

CREATE TABLE owner_notifications (
    id UUID PRIMARY KEY,
    message VARCHAR(512) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_owner_notification_created ON owner_notifications(created_at DESC);