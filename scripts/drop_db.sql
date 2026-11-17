-- Run psql -U <username> -d <username> -f scripts/drop_db.sql in your terminal
-- Use when flyway fails at migration
DROP DATABASE IF EXISTS parcella;
DROP DATABASE IF EXISTS parcella_test;