-- Create the database
CREATE DATABASE IF NOT EXISTS freelance_work_arena;
USE freelance_work_arena;

-- Create 'categories' table
CREATE TABLE categories (
    category_id INT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    PRIMARY KEY (category_id)
);

-- Create 'contracts' table
CREATE TABLE contracts (
    contract_id INT NOT NULL AUTO_INCREMENT,
    job_id INT,
    freelancer_id INT,
    client_id INT,
    start_date DATE,
    end_date DATE,
    payment_terms TEXT,
    status ENUM('pending', 'active', 'completed', 'cancelled') NOT NULL,
    work_submitted TINYINT(1) DEFAULT 0,
    payment_requested TINYINT(1) DEFAULT 0,
    PRIMARY KEY (contract_id)
);

-- Create 'jobs' table
CREATE TABLE jobs (
    job_id INT NOT NULL AUTO_INCREMENT,
    client_id INT,
    category_id INT,
    title VARCHAR(100),
    description TEXT,
    budget DECIMAL(10, 2),
    posted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'open', 'closed') DEFAULT 'pending',
    PRIMARY KEY (job_id)
);

-- Create 'messages' table
CREATE TABLE messages (
    message_id INT NOT NULL AUTO_INCREMENT,
    sender_id INT,
    receiver_id INT,
    message_text TEXT,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attachment TEXT,
    PRIMARY KEY (message_id)
);

-- Create 'payments' table
CREATE TABLE payments (
    payment_id INT NOT NULL AUTO_INCREMENT,
    contract_id INT,
    amount DECIMAL(10, 2),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('credit_card', 'paypal', 'bank_transfer', 'other') NOT NULL,
    request_date DATE,
    status ENUM('pending', 'completed', 'failed') NOT NULL,
    PRIMARY KEY (payment_id)
);

-- Create 'profiles' table
CREATE TABLE profiles (
    profile_id INT NOT NULL AUTO_INCREMENT,
    freelancer_id INT NOT NULL,
    bio TEXT,
    portfolio TEXT,
    PRIMARY KEY (profile_id)
);

-- Create 'proposals' table
CREATE TABLE proposals (
    proposal_id INT NOT NULL AUTO_INCREMENT,
    job_id INT,
    freelancer_id INT,
    proposal_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cover_letter TEXT,
    bid_amount DECIMAL(10, 2),
    status ENUM('pending', 'accepted', 'rejected') NOT NULL,
    PRIMARY KEY (proposal_id)
);

-- Create 'reviews' table
CREATE TABLE reviews (
    review_id INT NOT NULL AUTO_INCREMENT,
    contract_id INT,
    reviewer_id INT,
    rating INT,
    review_text TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (review_id)
);

-- Create 'users' table
CREATE TABLE users (
    user_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    user_type ENUM('client', 'freelancer', 'admin'),
    join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    phone_no VARCHAR(15),
    PRIMARY KEY (user_id)
);

TRIGGER:
DELIMITER //

CREATE TRIGGER update_job_status_after_contract
AFTER INSERT ON contracts
FOR EACH ROW
BEGIN
    UPDATE jobs
    SET status = 'in progress'
    WHERE job_id = NEW.job_id;
END;

//

DELIMITER ;

VIEW:
CREATE VIEW freelancer_job_applications AS
SELECT
    u.username AS freelancer_name,
    j.title AS job_title,
    p.status AS proposal_status,
    IFNULL(c.status, 'No Contract') AS contract_status
FROM
    users u
JOIN proposals p ON u.user_id = p.freelancer_id
JOIN jobs j ON p.job_id = j.job_id
LEFT JOIN contracts c ON j.job_id = c.job_id
WHERE u.user_type = 'freelancer';

BASIC INSERTION:

INSERT INTO users (username, password, email, user_type, join_date, phone_no) VALUES (...)
INSERT INTO categories (category_name, description) VALUES (...)
INSERT INTO jobs (job_id, client_id, category_id, title, description, budget, posted_date, status) VALUES (...)

SUBQUERY:
 Inserting a Proposal

INSERT INTO proposals (job_id, freelancer_id, cover_letter, bid_amount) 
SELECT ?, user_id, ?, ? FROM users WHERE username = ?

Accepting a Proposal and Creating a Contract

INSERT INTO contracts (job_id, freelancer_id, client_id, start_date, status)
VALUES (?, ?, (SELECT user_id FROM users WHERE username = ?), CURDATE(), 'In Progress')


JOINS:

Selecting Jobs and Categories

SELECT j.job_id, j.title, j.description, j.budget, c.category_name
FROM jobs j JOIN categories c ON j.category_id = c.category_id

Selecting Proposals for a Freelancer

SELECT p.proposal_id, j.title, p.bid_amount, p.cover_letter
FROM proposals p JOIN jobs j ON p.job_id = j.job_id
JOIN users u ON p.freelancer_id = u.user_id WHERE u.username = ?

UPDATE:

UPDATE contracts SET status = 'Work in Progress' WHERE contract_id = ?
UPDATE contracts SET payment_requested = 1 WHERE contract_id = ?


