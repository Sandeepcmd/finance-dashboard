-- ============================================================
-- Finance Dashboard Backend — MySQL Schema
-- ============================================================
-- Run this script to create the database and tables manually
-- if you prefer not to use Hibernate auto-DDL.
-- ============================================================

CREATE DATABASE IF NOT EXISTS finance_dashboard
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE finance_dashboard;

-- ── Users Table ──────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(20)     NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Financial Records Table ──────────────────────────────────

CREATE TABLE IF NOT EXISTS financial_records (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    amount      DECIMAL(15,2)   NOT NULL,
    type        VARCHAR(10)     NOT NULL,
    category    VARCHAR(100)    NOT NULL,
    date        DATE            NOT NULL,
    description VARCHAR(500)    NULL,
    created_by  BIGINT          NOT NULL,
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_records_user FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_records_type (type),
    INDEX idx_records_category (category),
    INDEX idx_records_date (date),
    INDEX idx_records_created_by (created_by),
    INDEX idx_records_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
