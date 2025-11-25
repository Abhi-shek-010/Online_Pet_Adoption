# 🗄️ SQL Configuration & Database Setup Guide

**Project**: PawMatch - Online Pet Adoption System  
**Database**: MySQL 8.0  
**Database Name**: `pet_adoption_system`

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [MySQL Installation](#mysql-installation)
3. [Database Creation](#database-creation)
4. [Schema Import](#schema-import)
5. [Verify Database Setup](#verify-database-setup)
6. [Database Structure](#database-structure)
7. [Troubleshooting](#troubleshooting)

---

## 📦 Prerequisites

- MySQL 8.0 or higher (for better performance)
- MySQL client tools
- Administrator access to your system
- Project files: `database_schema.sql` in `src/main/resources/`

---

## 🔧 MySQL Installation

### Windows Installation

**Step 1: Download MySQL**
1. Visit: https://dev.mysql.com/downloads/mysql/
2. Select Windows (x86, 64-bit)
3. Download "MySQL Community Server"

**Step 2: Run Installer**
```bash
# Double-click the downloaded MSI file
mysql-installer-community-8.0.x-winx64.msi
```

**Step 3: Follow Setup Wizard**
- Choose "Developer Default" (recommended)
- Accept defaults
- Configure MySQL Server:
  - Port: `3306` (default)
  - MySQL X Protocol Port: `33060`
  - Windows Service: Check "Configure MySQL Server as a Windows Service"

**Step 4: MySQL Server Configuration**
- Authentication Method: "Use Strong Password Encryption"
- MySQL Root Password: Set your password (remember it!)
- MySQL User Accounts: Skip for now (we'll create users in code)

**Step 5: Complete Installation**
- Click "Execute" to apply configuration
- Complete the installation wizard

**Verify Installation**:
```bash
# Open Command Prompt and test
mysql --version
mysql -u root -p
# Enter your root password
```

### macOS Installation

**Using Homebrew** (easiest):
```bash
brew install mysql
brew services start mysql

# Verify installation
mysql --version

# Connect to MySQL
mysql -u root
```

**Using DMG Installer**:
1. Download from: https://dev.mysql.com/downloads/mysql/
2. Double-click the DMG file
3. Follow the installer
4. Start MySQL Server from System Preferences

### Linux Installation

**Ubuntu/Debian**:
```bash
sudo apt-get update
sudo apt-get install mysql-server mysql-client

# Verify installation
mysql --version

# Start MySQL
sudo systemctl start mysql

# Connect
mysql -u root -p
```

**CentOS/RHEL**:
```bash
sudo yum install mysql-server
sudo systemctl start mysqld

# Verify installation
mysql --version
```

---

## 🗄️ Database Creation

### Step 1: Connect to MySQL

**Option A: Using Command Line**
```bash
# Connect as root
mysql -u root -p

# When prompted, enter your root password
# You should see the MySQL prompt: mysql>
```

**Option B: Using MySQL Workbench**
1. Open MySQL Workbench
2. Click "+" to create new connection
3. Enter:
   - Connection Name: "PawMatch Local"
   - Hostname: `127.0.0.1`
   - Port: `3306`
   - Username: `root`
4. Click "Test Connection"
5. Click "OK" and then double-click to connect

### Step 2: Create Database

**Command Line**:
```sql
-- Create the database
CREATE DATABASE IF NOT EXISTS pet_adoption_system;

-- Verify it was created
SHOW DATABASES;
-- You should see: pet_adoption_system

-- Select the database
USE pet_adoption_system;
```

**MySQL Workbench**:
1. Right-click "Schemas" in left panel
2. Click "Create Schema"
3. Name: `pet_adoption_system`
4. Click "Apply"

---

## 📥 Schema Import

### Method 1: Import Using Command Line (Recommended)

**Windows**:
```bash
# Navigate to project directory
cd c:\Users\Abhi\OneDrive\Desktop\Online_Pet_Adoption

# Import schema
mysql -u root -p pet_adoption_system < src\main\resources\database_schema.sql

# When prompted, enter your root password
```

**macOS/Linux**:
```bash
# Navigate to project directory
cd ~/Online_Pet_Adoption

# Import schema
mysql -u root -p pet_adoption_system < src/main/resources/database_schema.sql

# When prompted, enter your root password
```

**Expected Output**:
```
# No output means success!
# If there are errors, check the troubleshooting section
```

### Method 2: Import Using MySQL Workbench

1. Open MySQL Workbench
2. Connect to your MySQL instance
3. Go to: `File` → `Open SQL Script`
4. Navigate to: `src/main/resources/database_schema.sql`
5. Select the file and click "Open"
6. Click the lightning bolt icon (⚡) to execute
7. You should see tables created in the left panel

### Method 3: Manual SQL Entry

If the above methods fail, you can copy and paste the schema:

**Step 1: Open Schema File**
- Open: `src/main/resources/database_schema.sql`
- Copy all content

**Step 2: Execute in MySQL**
```bash
# Connect to MySQL
mysql -u root -p

# Enter password, then at MySQL prompt:
USE pet_adoption_system;

# Paste the SQL content and press Enter
```

---

## ✅ Verify Database Setup

### Check Database Created

```bash
# Connect to MySQL
mysql -u root -p

# List all databases
SHOW DATABASES;

# You should see:
# - information_schema
# - mysql
# - performance_schema
# - pet_adoption_system    ← Your new database
```

### Check Tables Created

```bash
# Use the database
USE pet_adoption_system;

# Show all tables
SHOW TABLES;

# You should see 4 tables:
# - users
# - pets
# - adoption_applications
# - adoptions
```

### Verify Table Structure

**Users Table**:
```sql
DESCRIBE users;
-- Expected columns: user_id, email, password_hash, full_name, phone, user_type, is_active, created_at
```

**Pets Table**:
```sql
DESCRIBE pets;
-- Expected columns: pet_id, name, species, breed, age, gender, description, adoption_status, shelter_id, created_at
```

**Adoption Applications Table**:
```sql
DESCRIBE adoption_applications;
-- Expected columns: app_id, adopter_id, pet_id, status, application_date, updated_at
```

**Adoptions Table**:
```sql
DESCRIBE adoptions;
-- Expected columns: adoption_id, adopter_id, pet_id, adoption_date, contract_signed
```

### View All Records (Initially Empty)

```sql
-- Count users (should be 0 initially)
SELECT COUNT(*) FROM users;

-- Count pets (should be 0 initially)
SELECT COUNT(*) FROM pets;

-- After admin initialization (should be 1 admin)
SELECT * FROM users;
```

---

## 📊 Database Structure Overview

### 1. USERS Table

```sql
-- Structure
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20),
    user_type ENUM('ADMIN', 'SHELTER', 'ADOPTER') DEFAULT 'ADOPTER',
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample query: Get all adoptable users
SELECT * FROM users WHERE user_type = 'ADOPTER' AND is_active = 1;
```

**Fields**:
- `user_id`: Unique identifier for each user
- `email`: User's email address (unique)
- `password_hash`: SHA-256 hashed password
- `full_name`: User's full name
- `phone`: Contact phone number
- `user_type`: Role (ADMIN, SHELTER, ADOPTER)
- `is_active`: Account status
- `created_at`: Registration timestamp

---

### 2. PETS Table

```sql
-- Structure
CREATE TABLE pets (
    pet_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(100),
    breed VARCHAR(100),
    age INT,
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN'),
    description TEXT,
    adoption_status ENUM('AVAILABLE', 'ADOPTED', 'PENDING') DEFAULT 'AVAILABLE',
    shelter_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shelter_id) REFERENCES users(user_id)
);

-- Sample query: Get all available pets
SELECT * FROM pets WHERE adoption_status = 'AVAILABLE' ORDER BY created_at DESC;

-- Sample query: Get dogs only
SELECT * FROM pets WHERE species = 'Dog' AND adoption_status = 'AVAILABLE';
```

**Fields**:
- `pet_id`: Unique identifier for each pet
- `name`: Pet's name
- `species`: Type of pet (Dog, Cat, Bird, etc.)
- `breed`: Specific breed
- `age`: Pet's age
- `gender`: Male, Female, or Unknown
- `description`: Pet's personality and details
- `adoption_status`: Current status (Available/Adopted/Pending)
- `shelter_id`: Reference to the shelter managing the pet
- `created_at`: When pet was added

---

### 3. ADOPTION_APPLICATIONS Table

```sql
-- Structure
CREATE TABLE adoption_applications (
    app_id INT AUTO_INCREMENT PRIMARY KEY,
    adopter_id INT NOT NULL,
    pet_id INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED') DEFAULT 'PENDING',
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (adopter_id) REFERENCES users(user_id),
    FOREIGN KEY (pet_id) REFERENCES pets(pet_id)
);

-- Sample query: Get pending applications
SELECT * FROM adoption_applications 
WHERE status = 'PENDING' 
ORDER BY application_date DESC;

-- Sample query: Get user's applications
SELECT * FROM adoption_applications 
WHERE adopter_id = 5 
ORDER BY application_date DESC;
```

**Fields**:
- `app_id`: Unique application ID
- `adopter_id`: Reference to adopter user
- `pet_id`: Reference to pet being adopted
- `status`: Application status (Pending/Approved/Rejected/Completed)
- `application_date`: When application was submitted
- `updated_at`: Last update timestamp

---

### 4. ADOPTIONS Table

```sql
-- Structure
CREATE TABLE adoptions (
    adoption_id INT AUTO_INCREMENT PRIMARY KEY,
    adopter_id INT NOT NULL,
    pet_id INT NOT NULL,
    adoption_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    contract_signed BOOLEAN DEFAULT 0,
    FOREIGN KEY (adopter_id) REFERENCES users(user_id),
    FOREIGN KEY (pet_id) REFERENCES pets(pet_id)
);

-- Sample query: Get all completed adoptions
SELECT * FROM adoptions ORDER BY adoption_date DESC;

-- Sample query: Get user's adoptions
SELECT * FROM adoptions WHERE adopter_id = 5;
```

**Fields**:
- `adoption_id`: Unique adoption record ID
- `adopter_id`: Reference to adopter
- `pet_id`: Reference to adopted pet
- `adoption_date`: When adoption was completed
- `contract_signed`: Whether legal contract was signed

---

## 🔑 Database Credentials

The application uses these credentials (configured in `DBConnection.java`):

```
Database Host: localhost
Database Port: 3306
Database Name: pet_adoption_system
Database User: root
Database Password: Abhi9608
```

**To change credentials**:
1. Edit: `src/main/java/com/petadoption/config/DBConnection.java`
2. Update these lines:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption_system";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "Abhi9608";
```

---

## 🔍 Common Database Queries

### View All Data

```sql
USE pet_adoption_system;

-- See all users
SELECT * FROM users;

-- See all pets
SELECT * FROM pets;

-- See all applications
SELECT * FROM adoption_applications;

-- See completed adoptions
SELECT * FROM adoptions;
```

### Add Sample Data

```sql
-- Add a test user (adopter)
INSERT INTO users (email, password_hash, full_name, phone, user_type, is_active)
VALUES ('john@example.com', 'hashed_password', 'John Doe', '555-1234', 'ADOPTER', 1);

-- Add a test pet
INSERT INTO pets (name, species, breed, age, gender, description, adoption_status, shelter_id)
VALUES ('Buddy', 'Dog', 'Golden Retriever', 3, 'MALE', 'Friendly and energetic', 'AVAILABLE', 1);
```

### Admin Initialization

When the application starts, it automatically creates:

```sql
-- Auto-created admin account
INSERT INTO users (email, password_hash, full_name, user_type, is_active)
VALUES ('admin', '[SHA256_HASH_OF_admin@123]', 'Administrator', 'ADMIN', 1);
```

---

## 🔒 Security Best Practices

### 1. Change Root Password

**Windows/macOS/Linux**:
```bash
# After MySQL installation, change root password
mysql -u root
-- Inside MySQL prompt:
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword123!';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Create Application User (Optional)

```sql
-- Instead of using root, create a dedicated user
CREATE USER 'petadoption'@'localhost' IDENTIFIED BY 'SecurePassword123!';

-- Grant permissions to the database
GRANT ALL PRIVILEGES ON pet_adoption_system.* TO 'petadoption'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Then update DBConnection.java to use this user
```

### 3. Enable MySQL Backups

**Create automatic backups**:

```bash
# Windows - Create a backup file
mysqldump -u root -p pet_adoption_system > backup.sql

# macOS/Linux
mysqldump -u root -p pet_adoption_system > ~/backups/pet_adoption_backup.sql
```

**Restore from backup**:
```bash
mysql -u root -p pet_adoption_system < backup.sql
```

---

## 🆘 Troubleshooting

### Issue 1: "Access denied for user 'root'@'localhost'"

**Problem**: Wrong password or user not created

**Solution**:
```bash
# Try without password
mysql -u root

# Or reset root password (Windows)
# Open Command Prompt as Administrator:
mysqld --skip-grant-tables

# In another terminal:
mysql -u root
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'NewPassword';
EXIT;
```

---

### Issue 2: "Can't connect to MySQL server on 'localhost'"

**Problem**: MySQL server not running

**Solution**:

**Windows**:
```bash
# Start MySQL service
net start MySQL80

# Or use Services app:
# Press Win+R → services.msc → Find MySQL → Right-click → Start
```

**macOS**:
```bash
brew services start mysql
# Or
mysql.server start
```

**Linux**:
```bash
sudo systemctl start mysql
sudo systemctl status mysql
```

---

### Issue 3: "Database already exists"

**Problem**: Database was already created previously

**Solution**:
```sql
-- Drop existing database (careful!)
DROP DATABASE IF EXISTS pet_adoption_system;

-- Then import fresh schema
CREATE DATABASE pet_adoption_system;
USE pet_adoption_system;
-- [Import schema from file]
```

---

### Issue 4: "Table doesn't exist" Error

**Problem**: Schema not imported correctly

**Solution**:
```bash
# Check if tables exist
mysql -u root -p pet_adoption_system -e "SHOW TABLES;"

# If empty, re-import schema
mysql -u root -p pet_adoption_system < src/main/resources/database_schema.sql

# Verify
mysql -u root -p pet_adoption_system -e "SHOW TABLES;"
```

---

### Issue 5: "Connection refused" in Java Application

**Problem**: Java cannot connect to database

**Solution**:
1. Verify MySQL is running: `mysql -u root -p`
2. Check credentials in `DBConnection.java`
3. Verify database exists: `SHOW DATABASES;`
4. Check MySQL is listening on port 3306: `netstat -an | findstr :3306`
5. Try to connect with same credentials: `mysql -u root -p pet_adoption_system`

---

## 📈 Performance Tips

### 1. Add Indexes

```sql
-- For faster user lookups by email
CREATE INDEX idx_email ON users(email);

-- For faster pet type filtering
CREATE INDEX idx_species ON pets(species);

-- For faster adoption status filtering
CREATE INDEX idx_adoption_status ON pets(adoption_status);

-- View indexes
SHOW INDEXES FROM users;
```

### 2. Monitor Database

```sql
-- Check database size
SELECT table_name, ROUND((data_length + index_length) / 1024 / 1024, 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'pet_adoption_system';

-- Check slow queries (if enabled)
SELECT * FROM mysql.slow_log LIMIT 10;
```

### 3. Enable Query Cache

```sql
-- Check if query cache is enabled
SHOW VARIABLES LIKE 'query_cache_size';

-- Set query cache size (in my.cnf or my.ini)
# query_cache_size = 268435456  # 256MB
# query_cache_type = 1  # ON
```

---

## 📝 Quick Reference

**Connection String**:
```
jdbc:mysql://localhost:3306/pet_adoption_system
```

**Root Credentials**:
```
Username: root
Password: Abhi9608
```

**Database Name**:
```
pet_adoption_system
```

**Port**:
```
3306
```

**Default Tables**:
```
- users
- pets
- adoption_applications
- adoptions
```

---

## ✨ Next Steps

After completing SQL setup:

1. ✅ Verify all 4 tables created
2. ✅ Test database connection from Java
3. ✅ Run application and check admin auto-initialization
4. ✅ Create test users and pets
5. ✅ Verify all CRUD operations work

---

**Happy Database Setup! 🎉**
