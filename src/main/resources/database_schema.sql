-- =====================================================
-- Online Pet Adoption Platform - Database Schema
-- =====================================================
-- This script creates the complete database schema for the pet adoption system
-- supporting Admin, Shelter, and Adopter users with pets and applications

-- Create Database
CREATE DATABASE IF NOT EXISTS pet_adoption_system;
USE pet_adoption_system;

-- =====================================================
-- USERS TABLE
-- =====================================================
-- Stores all user information including Admin, Shelter, and Adopter
-- Uses discriminator pattern with 'user_type' field
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15),
    user_type ENUM('ADMIN', 'SHELTER', 'ADOPTER') NOT NULL,
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_user_type (user_type)
);

-- =====================================================
-- SHELTER ADDITIONAL INFO TABLE
-- =====================================================
-- Stores additional information specific to shelter users
CREATE TABLE IF NOT EXISTS shelter_info (
    shelter_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    shelter_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    contact_person VARCHAR(100),
    capacity INT,
    current_animals INT DEFAULT 0,
    website VARCHAR(255),
    description TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_shelter_name (shelter_name),
    INDEX idx_license (license_number)
);

-- =====================================================
-- ADOPTER ADDITIONAL INFO TABLE
-- =====================================================
-- Stores additional information specific to adopter users
CREATE TABLE IF NOT EXISTS adopter_info (
    adopter_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    employment_status VARCHAR(50),
    home_type VARCHAR(50),
    has_other_pets BOOLEAN DEFAULT FALSE,
    other_pets_description TEXT,
    annual_income INT,
    rent_or_own VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- =====================================================
-- PETS TABLE
-- =====================================================
-- Stores information about available pets for adoption
CREATE TABLE IF NOT EXISTS pets (
    pet_id INT AUTO_INCREMENT PRIMARY KEY,
    shelter_id INT NOT NULL,
    pet_name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100),
    age_years INT,
    age_months INT,
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') NOT NULL,
    weight_kg DECIMAL(5, 2),
    color VARCHAR(100),
    health_status VARCHAR(100),
    vaccination_status ENUM('NOT_VACCINATED', 'PARTIAL', 'COMPLETE') DEFAULT 'NOT_VACCINATED',
    neutered_spayed BOOLEAN DEFAULT FALSE,
    microchip_number VARCHAR(50),
    description TEXT,
    special_needs TEXT,
    adoption_status ENUM('AVAILABLE', 'PENDING', 'ADOPTED', 'ARCHIVED') DEFAULT 'AVAILABLE',
    adoption_fee DECIMAL(10, 2),
    image_url VARCHAR(255),
    intake_date DATE,
    adoption_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shelter_id) REFERENCES shelter_info(shelter_id) ON DELETE CASCADE,
    INDEX idx_adoption_status (adoption_status),
    INDEX idx_species (species),
    INDEX idx_shelter_id (shelter_id),
    INDEX idx_pet_name (pet_name)
);

-- =====================================================
-- ADOPTION APPLICATIONS TABLE
-- =====================================================
-- Stores adoption application records submitted by adopters
CREATE TABLE IF NOT EXISTS adoption_applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    pet_id INT NOT NULL,
    adopter_id INT NOT NULL,
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'WITHDRAWN') DEFAULT 'PENDING',
    application_text TEXT,
    reason_for_adoption TEXT,
    household_members INT,
    has_yard BOOLEAN,
    yard_type VARCHAR(100),
    previous_pet_experience TEXT,
    veterinary_reference VARCHAR(255),
    personal_reference VARCHAR(255),
    approval_date TIMESTAMP,
    approval_notes TEXT,
    reviewed_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(pet_id) ON DELETE CASCADE,
    FOREIGN KEY (adopter_id) REFERENCES adopter_info(adopter_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id),
    INDEX idx_status (status),
    INDEX idx_adopter_id (adopter_id),
    INDEX idx_pet_id (pet_id),
    INDEX idx_application_date (application_date),
    UNIQUE KEY uk_pet_adopter (pet_id, adopter_id)
);

-- =====================================================
-- AUDIT LOG TABLE
-- =====================================================
-- Tracks user actions for security and audit purposes
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_entity (entity_type, entity_id)
);

-- =====================================================
-- Create Indexes for Performance
-- =====================================================
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_pets_available ON pets(adoption_status, shelter_id);
CREATE INDEX idx_applications_pending ON adoption_applications(status, application_date);

-- =====================================================
-- Insert Sample Admin User
-- =====================================================
INSERT INTO users (username, email, password_hash, full_name, user_type, is_verified, is_active)
VALUES ('admin', 'admin@petadoption.com', 'admin@123', 'System Administrator', 'ADMIN', TRUE, TRUE);

