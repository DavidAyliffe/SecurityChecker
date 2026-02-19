-- SecurityChecker database schema
-- Run this file against your MySQL server before starting the application.
--
-- Passwords must be BCrypt-hashed before insertion.
-- Use the HashPassword utility to generate hashes:
--
--   mvn compile exec:java -Dexec.mainClass=HashPassword -Dexec.args="yourpassword"
--
-- Copy the printed hash into the INSERT statements below, then run this file:
--
--   mysql -u <user> -p < schema.sql

CREATE DATABASE IF NOT EXISTS usernamepassword;
USE usernamepassword;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(20)  PRIMARY KEY,
    password VARCHAR(60)  NOT NULL,  -- BCrypt hashes are always 60 characters
    role     VARCHAR(20)  NOT NULL
);

-- Example inserts — replace each <bcrypt-hash> with output from HashPassword:
--
-- INSERT INTO users (username, password, role) VALUES
--     ('admin',   '<bcrypt-hash>', 'admin'),
--     ('person1', '<bcrypt-hash>', 'user');
