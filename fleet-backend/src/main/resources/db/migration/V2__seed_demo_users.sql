-- V2 : Clean DB and seed demo users (admin + driver)
-- Supprime toutes les données dans l'ordre inverse des dépendances FK

SET search_path TO fleetdb;

DELETE FROM MONTHLY_MILEAGE_HISTORY;
DELETE FROM MONTHLY_MILEAGE;
DELETE FROM VEHICLE_ASSIGNMENT;
DELETE FROM VEHICLE;
DELETE FROM APP_USER;

-- ── Admin ──────────────────────────────────────────────────────────────────
-- Email    : admin@example.com
-- Password : Admin123!
INSERT INTO APP_USER (EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, ROLE, ACTIVE)
VALUES (
    'admin@example.com',
    '$2a$10$mJsK85tiR7kDfIWF0g8PI.aLM8vtW9OtM5Oa0ygXmXWETw.9bOo2G',
    'System',
    'Admin',
    'ROLE_ADMIN',
    TRUE
);

-- ── Driver ─────────────────────────────────────────────────────────────────
-- Email    : demo.hpa@fleet.local
-- Password : Demo123!
INSERT INTO APP_USER (EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, ROLE, ACTIVE)
VALUES (
    'demo.hpa@fleet.local',
    '$2a$10$SFaE5bnBFGY4Pn3S7qObw.3rVmC12xz9ytvTjWLOalZRt/9Ip6rQK',
    'Demo',
    'Driver',
    'ROLE_DRIVER',
    TRUE
);
