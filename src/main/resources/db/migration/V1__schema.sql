-- Uzima schema. Runs unchanged on H2 (MODE=MySQL) and MySQL 8.
-- Conventions: money DECIMAL(14,2); metres DECIMAL(8,2); instants DATETIME(3) in UTC;
-- enums stored as their Java names. Catalog rows are retired (active = FALSE), never deleted
-- once referenced; payments are append-only.

CREATE TABLE users (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    username              VARCHAR(32)  NOT NULL,
    display_name          VARCHAR(80)  NOT NULL,
    role                  VARCHAR(16)  NOT NULL,
    password_hash         VARCHAR(100) NOT NULL,
    active                BOOLEAN      NOT NULL DEFAULT TRUE,
    must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          DATETIME(3)  NULL,
    theme                 VARCHAR(8)   NOT NULL DEFAULT 'LIGHT',
    created_at            DATETIME(3)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE clients (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    code         VARCHAR(20)   NOT NULL,
    name         VARCHAR(120)  NOT NULL,
    category     VARCHAR(16)   NOT NULL,
    phone        VARCHAR(16)   NOT NULL,
    email        VARCHAR(254)  NULL,
    county       VARCHAR(32)   NOT NULL,
    address      VARCHAR(255)  NOT NULL DEFAULT '',
    latitude     DECIMAL(9,6)  NULL,
    longitude    DECIMAL(9,6)  NULL,
    active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   DATETIME(3)   NOT NULL,
    created_by   BIGINT        NOT NULL,
    CONSTRAINT pk_clients PRIMARY KEY (id),
    CONSTRAINT uq_clients_code UNIQUE (code),
    CONSTRAINT fk_clients_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);
CREATE INDEX ix_clients_name ON clients (name);
CREATE INDEX ix_clients_phone ON clients (phone);
CREATE INDEX ix_clients_category ON clients (category, active);
CREATE INDEX ix_clients_county ON clients (county);
CREATE INDEX ix_clients_created_at ON clients (created_at);

-- Catalog -----------------------------------------------------------------------------------

CREATE TABLE drilling_types (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(80)   NOT NULL,
    down_payment  DECIMAL(14,2) NOT NULL,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_drilling_types PRIMARY KEY (id),
    CONSTRAINT uq_drilling_types_name UNIQUE (name)
);

CREATE TABLE pump_types (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    name       VARCHAR(80)   NOT NULL,
    base_cost  DECIMAL(14,2) NOT NULL,
    active     BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_pump_types PRIMARY KEY (id),
    CONSTRAINT uq_pump_types_name UNIQUE (name)
);

CREATE TABLE tank_types (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    capacity_litres  INT           NOT NULL,
    price            DECIMAL(14,2) NOT NULL,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_tank_types PRIMARY KEY (id),
    CONSTRAINT uq_tank_types_capacity UNIQUE (capacity_litres)
);

CREATE TABLE pipe_types (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    name            VARCHAR(80)   NOT NULL,
    cost_per_metre  DECIMAL(14,2) NOT NULL,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_pipe_types PRIMARY KEY (id),
    CONSTRAINT uq_pipe_types_name UNIQUE (name)
);

-- The live tier table. Shape (contiguous, last open-ended) is validated in the domain on save.
CREATE TABLE rate_tiers (
    position        INT           NOT NULL,
    from_metres     DECIMAL(8,2)  NOT NULL,
    to_metres       DECIMAL(8,2)  NULL,
    rate_per_metre  DECIMAL(14,2) NOT NULL,
    CONSTRAINT pk_rate_tiers PRIMARY KEY (position)
);

CREATE TABLE category_fees (
    category       VARCHAR(16)   NOT NULL,
    survey_fee     DECIMAL(14,2) NOT NULL,
    authority_fee  DECIMAL(14,2) NOT NULL,
    CONSTRAINT pk_category_fees PRIMARY KEY (category)
);

-- Jobs --------------------------------------------------------------------------------------
-- A job stores what was asked for (spec), the prices it was quoted with (price basis), and the
-- priced lines and totals (quote). Paid amount, balance and payment status are NOT stored; see
-- the job_accounts view.

CREATE TABLE jobs (
    id                          BIGINT        NOT NULL AUTO_INCREMENT,
    job_number                  VARCHAR(20)   NOT NULL,
    client_id                   BIGINT        NOT NULL,
    status                      VARCHAR(16)   NOT NULL,

    drilling_type_id            BIGINT        NULL,
    drilling_type_name          VARCHAR(80)   NULL,
    drilling_down_payment       DECIMAL(14,2) NULL,
    depth_metres                DECIMAL(8,2)  NULL,

    pump_type_id                BIGINT        NULL,
    pump_type_name              VARCHAR(80)   NULL,
    pump_base_cost              DECIMAL(14,2) NULL,
    lift_height_metres          DECIMAL(8,2)  NULL,

    tank_type_id                BIGINT        NULL,
    tank_capacity_litres        INT           NULL,
    tank_price                  DECIMAL(14,2) NULL,

    plumbing_mode               VARCHAR(10)   NULL,
    plumbing_outlets            INT           NULL,
    plumbing_manual_amount      DECIMAL(14,2) NULL,
    plumbing_manual_description VARCHAR(200)  NULL,

    priced_category             VARCHAR(16)   NOT NULL,
    survey_fee                  DECIMAL(14,2) NOT NULL,
    authority_fee               DECIMAL(14,2) NOT NULL,
    tax_rate                    DECIMAL(6,4)  NOT NULL,

    subtotal                    DECIMAL(14,2) NOT NULL,
    tax_amount                  DECIMAL(14,2) NOT NULL,
    grand_total                 DECIMAL(14,2) NOT NULL,

    notes                       VARCHAR(2000) NOT NULL DEFAULT '',
    created_at                  DATETIME(3)   NOT NULL,
    created_by                  BIGINT        NOT NULL,
    updated_at                  DATETIME(3)   NOT NULL,
    CONSTRAINT pk_jobs PRIMARY KEY (id),
    CONSTRAINT uq_jobs_number UNIQUE (job_number),
    CONSTRAINT fk_jobs_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_jobs_drilling_type FOREIGN KEY (drilling_type_id) REFERENCES drilling_types (id),
    CONSTRAINT fk_jobs_pump_type FOREIGN KEY (pump_type_id) REFERENCES pump_types (id),
    CONSTRAINT fk_jobs_tank_type FOREIGN KEY (tank_type_id) REFERENCES tank_types (id),
    CONSTRAINT fk_jobs_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);
CREATE INDEX ix_jobs_client ON jobs (client_id);
CREATE INDEX ix_jobs_status ON jobs (status);
CREATE INDEX ix_jobs_created_at ON jobs (created_at);

-- Snapshot of the tier table a job was priced with.
CREATE TABLE job_rate_tiers (
    job_id          BIGINT        NOT NULL,
    position        INT           NOT NULL,
    from_metres     DECIMAL(8,2)  NOT NULL,
    to_metres       DECIMAL(8,2)  NULL,
    rate_per_metre  DECIMAL(14,2) NOT NULL,
    CONSTRAINT pk_job_rate_tiers PRIMARY KEY (job_id, position),
    CONSTRAINT fk_job_rate_tiers_job FOREIGN KEY (job_id) REFERENCES jobs (id)
);

-- Priced quote lines. Invoices and reports read these, never the live catalog.
CREATE TABLE job_lines (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    job_id       BIGINT        NOT NULL,
    position     INT           NOT NULL,
    category     VARCHAR(16)   NOT NULL,
    description  VARCHAR(200)  NOT NULL,
    quantity     DECIMAL(10,2) NOT NULL,
    unit         VARCHAR(8)    NOT NULL,
    unit_price   DECIMAL(14,2) NOT NULL,
    line_total   DECIMAL(14,2) NOT NULL,
    CONSTRAINT pk_job_lines PRIMARY KEY (id),
    CONSTRAINT uq_job_lines_position UNIQUE (job_id, position),
    CONSTRAINT fk_job_lines_job FOREIGN KEY (job_id) REFERENCES jobs (id)
);
CREATE INDEX ix_job_lines_category ON job_lines (category);

-- Itemised plumbing runs, with the pipe price snapshotted.
CREATE TABLE plumbing_lines (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    job_id          BIGINT        NOT NULL,
    position        INT           NOT NULL,
    pipe_type_id    BIGINT        NOT NULL,
    pipe_name       VARCHAR(80)   NOT NULL,
    cost_per_metre  DECIMAL(14,2) NOT NULL,
    diameter_mm     INT           NOT NULL,
    length_metres   DECIMAL(8,2)  NOT NULL,
    CONSTRAINT pk_plumbing_lines PRIMARY KEY (id),
    CONSTRAINT uq_plumbing_lines_position UNIQUE (job_id, position),
    CONSTRAINT fk_plumbing_lines_job FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT fk_plumbing_lines_pipe FOREIGN KEY (pipe_type_id) REFERENCES pipe_types (id)
);

CREATE TABLE job_status_history (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    job_id       BIGINT       NOT NULL,
    from_status  VARCHAR(16)  NULL,
    to_status    VARCHAR(16)  NOT NULL,
    reason       VARCHAR(500) NOT NULL DEFAULT '',
    changed_at   DATETIME(3)  NOT NULL,
    changed_by   BIGINT       NOT NULL,
    CONSTRAINT pk_job_status_history PRIMARY KEY (id),
    CONSTRAINT fk_job_status_history_job FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT fk_job_status_history_user FOREIGN KEY (changed_by) REFERENCES users (id)
);
CREATE INDEX ix_job_status_history_job ON job_status_history (job_id, changed_at);

-- Payments (append-only) --------------------------------------------------------------------
-- Positive rows are payments received; negative rows are reversals linked by reversal_of.

CREATE TABLE payments (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    receipt_number  VARCHAR(20)   NOT NULL,
    job_id          BIGINT        NOT NULL,
    amount          DECIMAL(14,2) NOT NULL,
    payment_date    DATE          NOT NULL,
    method          VARCHAR(16)   NOT NULL,
    reference       VARCHAR(64)   NOT NULL DEFAULT '',
    received_by     BIGINT        NOT NULL,
    notes           VARCHAR(500)  NOT NULL DEFAULT '',
    reversal_of     BIGINT        NULL,
    reason          VARCHAR(500)  NULL,
    created_at      DATETIME(3)   NOT NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uq_payments_receipt UNIQUE (receipt_number),
    CONSTRAINT fk_payments_job FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT fk_payments_received_by FOREIGN KEY (received_by) REFERENCES users (id),
    CONSTRAINT fk_payments_reversal_of FOREIGN KEY (reversal_of) REFERENCES payments (id),
    CONSTRAINT ck_payments_sign CHECK (
        (reversal_of IS NULL AND amount > 0) OR (reversal_of IS NOT NULL AND amount < 0))
);
CREATE INDEX ix_payments_job ON payments (job_id);
CREATE INDEX ix_payments_date ON payments (payment_date);
CREATE INDEX ix_payments_method ON payments (method, payment_date);

-- Derived payment state per job. A cancelled job owes nothing; whatever it still holds is a
-- refund due.
CREATE VIEW job_accounts AS
SELECT j.id                                         AS job_id,
       j.status                                     AS status,
       j.grand_total                                AS grand_total,
       COALESCE(SUM(p.amount), 0)                   AS amount_paid,
       CASE WHEN j.status = 'CANCELLED' THEN 0
            ELSE j.grand_total - COALESCE(SUM(p.amount), 0) END AS balance,
       CASE WHEN COALESCE(SUM(p.amount), 0) = 0 THEN 'NOT_PAID'
            WHEN COALESCE(SUM(p.amount), 0) < j.grand_total THEN 'PARTIALLY_PAID'
            ELSE 'PAID' END                         AS payment_status,
       CASE WHEN j.status = 'CANCELLED' AND COALESCE(SUM(p.amount), 0) > 0
            THEN COALESCE(SUM(p.amount), 0) ELSE 0 END AS refund_due
FROM jobs j
LEFT JOIN payments p ON p.job_id = j.id
GROUP BY j.id, j.status, j.grand_total;

-- Cross-cutting -----------------------------------------------------------------------------

CREATE TABLE audit_log (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    occurred_at  DATETIME(3)   NOT NULL,
    user_id      BIGINT        NULL,
    username     VARCHAR(64)   NOT NULL,
    action       VARCHAR(40)   NOT NULL,
    entity_type  VARCHAR(32)   NOT NULL DEFAULT '',
    entity_id    VARCHAR(40)   NOT NULL DEFAULT '',
    details      VARCHAR(4000) NOT NULL DEFAULT '',
    CONSTRAINT pk_audit_log PRIMARY KEY (id),
    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX ix_audit_log_occurred_at ON audit_log (occurred_at);
CREATE INDEX ix_audit_log_action ON audit_log (action, occurred_at);
CREATE INDEX ix_audit_log_user ON audit_log (user_id, occurred_at);
CREATE INDEX ix_audit_log_entity ON audit_log (entity_type, entity_id);

CREATE TABLE settings (
    setting_key    VARCHAR(64)   NOT NULL,
    setting_value  VARCHAR(1000) NOT NULL,
    CONSTRAINT pk_settings PRIMARY KEY (setting_key)
);

-- Document number counters, e.g. 'CLIENT-DOM', 'JOB-2026', 'RCP-2026'. Rows are locked with
-- SELECT ... FOR UPDATE inside the inserting transaction, so numbers are unique and gap-free.
CREATE TABLE sequences (
    name        VARCHAR(32) NOT NULL,
    next_value  BIGINT      NOT NULL,
    CONSTRAINT pk_sequences PRIMARY KEY (name)
);
