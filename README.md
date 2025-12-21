# 🐾 PawMatch - Online Pet Adoption System

A full-stack web application for pet adoption built with Spring Boot, Java Servlets, and MySQL.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

---

## 📋 Table of Contents

- [Prerequisites](#-prerequisites)
- [Database Setup](#-database-setup)
- [Configuration](#-configuration)
- [Build & Run](#-build--run)
- [Accessing the Application](#-accessing-the-application)
- [API Endpoints](#-api-endpoints)
- [Project Structure](#-project-structure)
- [Troubleshooting](#-troubleshooting)

---

## 🔧 Prerequisites

Before running this application, ensure you have the following installed:

| Software | Version | Download Link |
|----------|---------|---------------|
| **Java JDK** | 17 or higher | [Download](https://www.oracle.com/java/technologies/downloads/#java17) |
| **Apache Maven** | 3.8+ | [Download](https://maven.apache.org/download.cgi) |
| **MySQL Server** | 8.0+ | [Download](https://dev.mysql.com/downloads/mysql/) |
| **Git** (optional) | Latest | [Download](https://git-scm.com/downloads) |

### Verify Installation

Open a terminal/command prompt and run:

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check MySQL (if added to PATH)
mysql --version
```

---

## 🗄️ Database Setup

### Step 1: Start MySQL Server

Make sure your MySQL server is running. On Windows, you can check via Services or MySQL Workbench.

### Step 2: Create the Database

Open MySQL command line or MySQL Workbench and execute:

```sql
-- Create the database
CREATE DATABASE IF NOT EXISTS pet_adoption_system;

-- Use the database
USE pet_adoption_system;
```

### Step 3: Create Required Tables

Execute the following SQL script to create all necessary tables:

```sql
-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    user_type ENUM('ADMIN', 'SHELTER', 'ADOPTER') NOT NULL DEFAULT 'ADOPTER',
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Shelters table
CREATE TABLE IF NOT EXISTS shelters (
    shelter_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    shelter_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50),
    website VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Pets table
CREATE TABLE IF NOT EXISTS pets (
    pet_id INT AUTO_INCREMENT PRIMARY KEY,
    pet_name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100),
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') NOT NULL,
    age_years INT DEFAULT 0,
    age_months INT DEFAULT 0,
    weight_kg DECIMAL(5,2),
    color VARCHAR(50),
    description TEXT,
    special_needs TEXT,
    vaccination_status ENUM('NOT_VACCINATED', 'PARTIAL', 'COMPLETE') DEFAULT 'NOT_VACCINATED',
    adoption_status ENUM('AVAILABLE', 'PENDING', 'ADOPTED', 'UNAVAILABLE') DEFAULT 'AVAILABLE',
    adoption_fee DECIMAL(10,2),
    shelter_id INT NOT NULL,
    intake_date DATE,
    adoption_date DATE,
    photo_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shelter_id) REFERENCES shelters(shelter_id) ON DELETE CASCADE
);

-- Adoption Applications table
CREATE TABLE IF NOT EXISTS adoption_applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    pet_id INT NOT NULL,
    adopter_id INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'WITHDRAWN') DEFAULT 'PENDING',
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    application_text TEXT,
    reason_for_adoption TEXT,
    household_members INT,
    has_yard BOOLEAN DEFAULT FALSE,
    yard_type VARCHAR(50),
    previous_pet_experience TEXT,
    veterinary_reference VARCHAR(255),
    personal_reference VARCHAR(255),
    approval_date TIMESTAMP NULL,
    approval_notes TEXT,
    reviewed_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(pet_id) ON DELETE CASCADE,
    FOREIGN KEY (adopter_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Insert sample admin user (password: admin123)
INSERT INTO users (username, email, password_hash, full_name, user_type, is_verified, is_active)
VALUES ('admin', 'admin@pawmatch.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqN/J4rQm/X0.wHZOdS6gzDcW9VpO', 'System Admin', 'ADMIN', TRUE, TRUE);

-- Insert sample shelter user (password: shelter123)
INSERT INTO users (username, email, password_hash, full_name, user_type, phone_number, is_verified, is_active)
VALUES ('happypaws', 'shelter@pawmatch.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Happy Paws Shelter', 'SHELTER', '555-0100', TRUE, TRUE);

-- Insert shelter info
INSERT INTO shelters (user_id, shelter_name, description)
VALUES (2, 'Happy Paws Animal Shelter', 'A loving shelter dedicated to finding homes for abandoned pets.');

-- Insert sample pets
INSERT INTO pets (pet_name, species, breed, gender, age_years, age_months, description, vaccination_status, adoption_status, adoption_fee, shelter_id, intake_date)
VALUES 
('Buddy', 'Dog', 'Golden Retriever', 'MALE', 3, 0, 'A friendly and playful golden retriever who loves everyone!', 'COMPLETE', 'AVAILABLE', 150.00, 1, CURDATE()),
('Whiskers', 'Cat', 'Persian', 'FEMALE', 2, 6, 'A gentle and loving cat who enjoys cuddles.', 'COMPLETE', 'AVAILABLE', 100.00, 1, CURDATE()),
('Max', 'Dog', 'German Shepherd', 'MALE', 4, 0, 'Loyal and protective, great for families.', 'COMPLETE', 'AVAILABLE', 200.00, 1, CURDATE()),
('Luna', 'Cat', 'Siamese', 'FEMALE', 1, 3, 'Playful and curious kitten looking for adventure.', 'PARTIAL', 'AVAILABLE', 80.00, 1, CURDATE()),
('Charlie', 'Dog', 'Beagle', 'MALE', 2, 0, 'Energetic and friendly, loves long walks.', 'COMPLETE', 'AVAILABLE', 175.00, 1, CURDATE());
```

---

## ⚙️ Configuration

### Database Configuration

Edit the database connection settings in:
`src/main/java/com/petadoption/config/DBConnection.java`

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption_system";
private static final String DB_USER = "root";           // Your MySQL username
private static final String DB_PASSWORD = "your_password";  // Your MySQL password
```

> ⚠️ **Important**: Change `DB_PASSWORD` to your actual MySQL root password!

### Spring Boot Configuration (Optional)

You can also configure application properties in `src/main/resources/application.properties`:

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/pet_adoption_system
spring.datasource.username=root
spring.datasource.password=your_password
```

---

## 🚀 Build & Run

### Step 1: Open Terminal

Navigate to the project directory:

```bash
cd C:\Users\Abhi\OneDrive\Desktop\Online_Pet_Adoption
```

### Step 2: Install Dependencies

```bash
mvn clean install
```

This downloads all required dependencies and builds the project.

### Step 3: Run the Application

**Option A: Using Maven (Recommended for Development)**
```bash
mvn spring-boot:run
```

**Option B: Using JAR file**
```bash
# First, package the application
mvn clean package -DskipTests

# Then run the JAR
java -jar target/pet-adoption-system-0.0.1-SNAPSHOT.jar
```

### Expected Output

When the application starts successfully, you should see:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

✓ AuthServlet initialized with service layer
✓ PetServlet initialized with service layer
✓ AdoptionServlet initialized with service layer

Started PetAdoptionApplication in X.XXX seconds
```

---

## 🌐 Accessing the Application

Once running, open your browser and navigate to:

| Page | URL |
|------|-----|
| **Home Page** | http://localhost:8080/ |
| **Login** | http://localhost:8080/login.html |
| **Register** | http://localhost:8080/register.html |
| **Pet Listings** | http://localhost:8080/pets-listing.html |
| **Dashboard** | http://localhost:8080/dashboard.html |

### Demo Accounts

Use these accounts to test the application:

#### Admin & Adopter Accounts
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@pawmatch.com | admin123 |
| Adopter | happy@family.com | password123 |

#### Shelter Accounts
| Shelter Name | Email | Password |
|--------------|-------|----------|
| Paws & Claws Shelter | shelter1@paws.com | password123 |
| Safe Haven Rescue | shelter2@haven.com | password123 |
| Happy Tails Sanctuary | shelter3@tails.com | password123 |
| Furry Friends Foundation | shelter4@furry.com | password123 |
| Second Chance Animal Rescue | shelter5@second.com | password123 |
| Guardian Angels Pet Shelter | shelter6@guardian.com | password123 |

> **Note**: You can also create a new account via the registration page.

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | User login |
| POST | `/auth/register` | User registration |
| GET | `/auth/logout` | User logout |

### Pets

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/pets` | Get all available pets |
| GET | `/pets/{id}` | Get pet by ID |
| POST | `/pets` | Create new pet (Shelter/Admin only) |
| GET | `/pets?action=search&q={term}` | Search pets |

### Adoption Applications

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/adoptions/` | Get pending applications |
| POST | `/api/adoptions/?action=submit` | Submit application |
| POST | `/api/adoptions/?action=approve` | Approve application |
| POST | `/api/adoptions/?action=reject` | Reject application |

---

## 📁 Project Structure

```
Online_Pet_Adoption/
├── src/
│   └── main/
│       ├── java/com/petadoption/
│       │   ├── PetAdoptionApplication.java    # Main entry point
│       │   ├── config/
│       │   │   ├── DBConnection.java          # Database connection
│       │   │   └── SecurityConfig.java        # Spring Security config
│       │   ├── dao/
│       │   │   ├── UserDAO.java               # User data access
│       │   │   ├── PetDAO.java                # Pet data access
│       │   │   └── AdoptionApplicationDAO.java
│       │   ├── model/
│       │   │   ├── User.java                  # User entity
│       │   │   ├── Pet.java                   # Pet entity
│       │   │   └── AdoptionApplication.java   # Application entity
│       │   ├── service/
│       │   │   ├── UserService.java           # User business logic
│       │   │   ├── PetService.java            # Pet business logic
│       │   │   └── AdoptionService.java       # Adoption business logic
│       │   ├── servlet/
│       │   │   ├── AuthServlet.java           # Authentication endpoints
│       │   │   ├── PetServlet.java            # Pet endpoints
│       │   │   └── AdoptionServlet.java       # Adoption endpoints
│       │   ├── util/
│       │   │   ├── PasswordUtils.java         # Password hashing
│       │   │   └── SessionUtils.java          # Session management
│       │   └── exception/
│       │       └── PetAdoptionException.java  # Custom exceptions
│       ├── resources/
│       │   └── application.properties
│       └── webapp/
│           ├── index.html                     # Landing page
│           ├── login.html                     # Login page
│           ├── register.html                  # Registration page
│           ├── dashboard.html                 # User dashboard
│           └── pets-listing.html              # Pet listings
├── pom.xml                                    # Maven dependencies
└── README.md                                  # This file
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. "Database connection failed"
- ✅ Ensure MySQL server is running
- ✅ Check database credentials in `DBConnection.java`
- ✅ Verify the database `pet_adoption_system` exists

#### 2. "Port 8080 already in use"
```bash
# Find and kill the process using port 8080 (Windows)
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or change the port in application.properties
server.port=8081
```

#### 3. "API JSON connection errors"
- ✅ Make sure you've rebuilt the project after any code changes: `mvn clean install`
- ✅ Clear browser cache and cookies
- ✅ Check browser console for CORS errors

#### 4. "Class not found" or "NoClassDefFoundError"
```bash
# Clean and rebuild
mvn clean install -U
```

#### 5. Maven build fails
```bash
# Update Maven wrapper
mvn -N wrapper:wrapper

# Skip tests if they're failing
mvn clean install -DskipTests
```

---

## 🛠️ Development

### Hot Reload
Spring Boot DevTools is included. Changes to Java files will trigger automatic restart.

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -Pprod
```

---

## 📞 Support

If you encounter any issues:
1. Check the [Troubleshooting](#-troubleshooting) section
2. Review server logs in the console
3. Check browser developer console for frontend errors

---

## 📄 License

This project is for educational purposes.

---

**Made with ❤️ for pet lovers everywhere** 🐕 🐈
