-- FIXED: Aligned schema with application models and DAO column names
CREATE DATABASE IF NOT EXISTS expense_splitter_db;
USE expense_splitter_db;

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)        NOT NULL,
    email         VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS groups_table (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS group_members (
    group_id INT,
    user_id  INT,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES groups_table(id),
    FOREIGN KEY (user_id)  REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS categories (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS expenses (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    group_id    INT,
    description VARCHAR(200),
    amount      DECIMAL(10,2),
    paid_by     INT,
    category_id INT,
    split_type  ENUM('EQUAL','CUSTOM','PERCENTAGE') DEFAULT 'EQUAL',
    date        DATE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id)    REFERENCES groups_table(id),
    FOREIGN KEY (paid_by)     REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS expense_splits (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    expense_id INT,
    user_id    INT,
    amount     DECIMAL(10,2),
    FOREIGN KEY (expense_id) REFERENCES expenses(id),
    FOREIGN KEY (user_id)    REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS settlements (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    group_id   INT,
    payer_id   INT,
    payee_id   INT,
    amount     DECIMAL(10,2),
    date       DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups_table(id),
    FOREIGN KEY (payer_id) REFERENCES users(id),
    FOREIGN KEY (payee_id) REFERENCES users(id)
);

INSERT IGNORE INTO categories (name) VALUES
    ('Infrastructure'),('Meals'),('Software'),
    ('Travel'),('Hardware'),('Office'),('Events'),('Other');
