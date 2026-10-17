-- Reference data from the business rules (spec section 5). Prices are editable later in the
-- catalog screen; jobs snapshot whatever was current when they were quoted.

INSERT INTO drilling_types (name, down_payment, active) VALUES
    ('Symmetric',     130000.00, TRUE),
    ('Core',          225000.00, TRUE),
    ('Geo-technical', 335000.00, TRUE);

INSERT INTO pump_types (name, base_cost, active) VALUES
    ('Submersible electric', 90000.00, TRUE),
    ('Solar',                65000.00, TRUE),
    ('Hand',                 30000.00, TRUE);

INSERT INTO tank_types (capacity_litres, price, active) VALUES
    (1000,   15000.00, TRUE),
    (2000,   28000.00, TRUE),
    (5000,   65000.00, TRUE),
    (10000, 125000.00, TRUE);

INSERT INTO pipe_types (name, cost_per_metre, active) VALUES
    ('PVC',         500.00, TRUE),
    ('HDPE',        800.00, TRUE),
    ('Galvanized', 1200.00, TRUE);

INSERT INTO rate_tiers (position, from_metres, to_metres, rate_per_metre) VALUES
    (1,   0.00, 100.00, 1000.00),
    (2, 100.00, 200.00, 1500.00),
    (3, 200.00, 300.00, 2000.00),
    (4, 300.00,   NULL, 2500.00);

INSERT INTO category_fees (category, survey_fee, authority_fee) VALUES
    ('INDUSTRIAL', 20000.00, 50000.00),
    ('COMMERCIAL', 15000.00, 30000.00),
    ('DOMESTIC',    7000.00, 10000.00);

INSERT INTO settings (setting_key, setting_value) VALUES
    ('company.name',        'Uzima Borehole Drilling Company'),
    ('company.address',     ''),
    ('company.phone',       ''),
    ('company.email',       ''),
    ('company.kra_pin',     ''),
    ('tax.rate',            '0.1600'),
    ('session.idle_lock_minutes', '15'),
    ('export.default_folder', '');
