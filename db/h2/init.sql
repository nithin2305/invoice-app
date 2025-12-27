-- H2 Database Initialization Script for Invoice Application
-- This script creates the tables and primary keys for development

-- =====================================================
-- Table: clients
-- Description: Stores client/party information
-- =====================================================
CREATE TABLE IF NOT EXISTS clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    gst_number VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_clients_name ON clients(name);
CREATE INDEX IF NOT EXISTS idx_clients_gst_number ON clients(gst_number);

-- =====================================================
-- Table: invoices
-- Description: Stores invoice header information
-- =====================================================
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_no VARCHAR(255) NOT NULL UNIQUE,
    invoice_date DATE,
    party_id BIGINT,
    party_name VARCHAR(255),
    party_address VARCHAR(500),
    party_gst VARCHAR(20),
    halting_charges DECIMAL(12, 2) DEFAULT 0.00,
    loading_charges DECIMAL(12, 2) DEFAULT 0.00,
    unloading_charges DECIMAL(12, 2) DEFAULT 0.00,
    total_amount DECIMAL(12, 2) DEFAULT 0.00,
    amount_in_words VARCHAR(500),
    remarks VARCHAR(1000),
    CONSTRAINT fk_invoices_party FOREIGN KEY (party_id) REFERENCES clients(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_invoices_invoice_no ON invoices(invoice_no);
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_date ON invoices(invoice_date);
CREATE INDEX IF NOT EXISTS idx_invoices_party_id ON invoices(party_id);

-- =====================================================
-- Table: invoice_items
-- Description: Stores invoice line items (LR - Lorry Receipts)
-- =====================================================
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    lr_no VARCHAR(50),
    lr_date DATE,
    from_location VARCHAR(255),
    to_location VARCHAR(255),
    goods_description VARCHAR(500),
    package_type VARCHAR(50),
    package_count INT,
    vehicle_number VARCHAR(20),
    vehicle_type VARCHAR(50),
    amount DECIMAL(12, 2) DEFAULT 0.00,
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice_id ON invoice_items(invoice_id);
CREATE INDEX IF NOT EXISTS idx_invoice_items_lr_no ON invoice_items(lr_no);

-- =====================================================
-- Summary of Primary Keys:
-- =====================================================
-- clients.id          - AUTO_INCREMENT BIGINT (Primary Key)
-- invoices.id         - AUTO_INCREMENT BIGINT (Primary Key)
-- invoice_items.id    - AUTO_INCREMENT BIGINT (Primary Key)
--
-- Foreign Key Relationships:
-- invoices.party_id -> clients.id (ON DELETE SET NULL)
-- invoice_items.invoice_id -> invoices.id (ON DELETE CASCADE)
-- =====================================================
