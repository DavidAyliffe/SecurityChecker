# SecurityChecker

A Java console application that authenticates users against a MySQL database.
Passwords are stored as BCrypt hashes — plaintext passwords are never held in memory beyond the initial input comparison.

---

## Requirements

- Java 22+
- Maven 3.6+
- MySQL 8+ server

---

## Setup

### 1. Configure environment variables

The application reads database credentials from the environment rather than hardcoding them.
Set the following variables before running:

```bash
export DB_URL="jdbc:mysql://<host>:<port>/usernamepassword"
export DB_USER="<mysql-username>"
export DB_PASSWORD="<mysql-password>"
```

### 2. Initialise the database schema

Run `schema.sql` against your MySQL server:

```bash
mysql -u <user> -p < schema.sql
```

### 3. Add users

Generate a BCrypt hash for each user's password using the included `HashPassword` utility:

```bash
mvn compile exec:java -Dexec.mainClass=HashPassword -Dexec.args="yourpassword"
```

Copy the printed hash into an INSERT statement:

```sql
INSERT INTO users (username, password, role) VALUES ('alice', '<hash>', 'admin');
```

---

## Build

```bash
mvn package
```

This produces `target/SecurityChecker-1.0-SNAPSHOT.jar`.

---

## Run

```bash
java -jar target/SecurityChecker-1.0-SNAPSHOT.jar
```

You will be prompted for a username and password. The application prints whether login succeeded and, if so, the user's role.

---

## Project structure

```
SecurityChecker/
├── src/main/java/
│   ├── Main.java           # Application entry point
│   └── HashPassword.java   # Utility for generating BCrypt hashes
├── schema.sql              # Database schema and setup instructions
├── pom.xml                 # Maven build and dependency configuration
└── .gitignore
```

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| mysql-connector-j | 9.0.0 | JDBC driver for MySQL |
| jbcrypt | 0.4 | BCrypt password hashing |
