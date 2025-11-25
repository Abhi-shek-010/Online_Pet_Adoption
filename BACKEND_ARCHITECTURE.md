# Online Pet Adoption Platform - Backend Architecture Documentation

## Project Overview

The Online Pet Adoption Platform is a robust, production-ready Java web application built with Spring Boot 3.2.0 and MySQL. This document outlines the backend core and database implementation as per the architectural requirements.

---

## 1. Database Schema (MySQL)

### Overview
The database follows a relational model supporting three user types (Admin, Shelter, Adopter) with comprehensive pet adoption workflow management.

### Key Tables

#### Users Table
- **Purpose**: Centralized user management with role-based access
- **Key Fields**:
  - `user_id`: Primary key (auto-increment)
  - `user_type`: ENUM (ADMIN, SHELTER, ADOPTER)
  - `email`: Unique identifier for login
  - `password_hash`: Bcrypt hashed passwords (never plain text)
  - `is_verified`: Email verification status
  - `is_active`: Account active/inactive flag
  - `last_login`: Track user activity
  - Timestamps: `created_at`, `updated_at`, `registration_date`

#### Shelter Info Table
- **Purpose**: Extended information for shelter users
- **Key Fields**:
  - `shelter_id`: Primary key
  - `user_id`: Foreign key to users table
  - `license_number`: Unique shelter license
  - `capacity`: Maximum animal capacity
  - `current_animals`: Real-time count

#### Adopter Info Table
- **Purpose**: Extended information for adopter users
- **Key Fields**:
  - `adopter_id`: Primary key
  - `user_id`: Foreign key to users table
  - `employment_status`: Job status
  - `home_type`: Type of residence
  - `has_other_pets`: Whether household has other pets

#### Pets Table
- **Purpose**: Central pet inventory with full adoption tracking
- **Key Fields**:
  - `pet_id`: Primary key (auto-increment)
  - `shelter_id`: Foreign key to shelter_info
  - `species`: Type of animal (Dog, Cat, etc.)
  - `breed`: Specific breed
  - `age_years`, `age_months`: Age tracking
  - `gender`: ENUM (MALE, FEMALE, UNKNOWN)
  - `vaccination_status`: ENUM (NOT_VACCINATED, PARTIAL, COMPLETE)
  - `neutered_spayed`: Boolean surgical status
  - `adoption_status`: ENUM (AVAILABLE, PENDING, ADOPTED, ARCHIVED)
  - `adoption_fee`: Cost to adopt
  - `health_status`: Medical assessment
  - `microchip_number`: Pet identification
  - `special_needs`: Medical or behavioral requirements

#### Adoption Applications Table
- **Purpose**: Track adoption requests and approvals
- **Key Fields**:
  - `application_id`: Primary key
  - `pet_id`: Foreign key to pets
  - `adopter_id`: Foreign key to adopter_info
  - `status`: ENUM (PENDING, APPROVED, REJECTED, WITHDRAWN)
  - `application_date`: When submitted
  - `approval_date`: When decided
  - `reviewed_by`: User ID of reviewer
  - Unique constraint on (pet_id, adopter_id) to prevent duplicate applications

#### Audit Logs Table
- **Purpose**: Security and compliance tracking
- **Key Fields**:
  - Action logging with old/new values as JSON
  - IP address and user agent tracking
  - Complete audit trail

### Database File
- **Location**: `src/main/resources/database_schema.sql`
- **Features**:
  - Complete CREATE TABLE statements
  - Proper indexing for performance
  - Foreign key constraints with cascading deletes
  - Sample admin user insertion

---

## 2. JDBC Connection Management

### DBConnection Class
- **Location**: `src/main/java/com/petadoption/config/DBConnection.java`
- **Pattern**: Singleton pattern with thread-safe initialization
- **Features**:
  - Lazy initialization of connection
  - Connection pooling ready
  - Comprehensive error handling
  - Resource cleanup utilities

### Key Methods
```java
// Get singleton instance
DBConnection.getInstance()

// Get database connection
Connection conn = DBConnection.getInstance().getConnection()

// Safe resource cleanup
DBConnection.closeConnection(connection)
DBConnection.closePreparedStatement(statement)
DBConnection.closeResultSet(resultSet)

// Test connection
testConnection()
```

### Configuration
```
DB_DRIVER = com.mysql.cj.jdbc.Driver
DB_URL = jdbc:mysql://localhost:3306/pet_adoption_system
DB_USER = root
DB_PASSWORD = root (Change in production!)
CONNECTION_TIMEOUT = 10 seconds
```

### Production Notes
- Move credentials to environment variables or configuration files
- Implement connection pooling with HikariCP or C3P0
- Use SSL connections for database
- Enable query logging for debugging

---

## 3. POJO Models with Encapsulation

### User Model
- **Location**: `src/main/java/com/petadoption/model/User.java`
- **Encapsulation Features**:
  - All fields private
  - Public getters/setters with validation
  - Immutable `userId` after creation
  - Email validation with regex
  - Username minimum length validation
  - Phone number format validation
  - Password hash protection (never exposed)

### Pet Model
- **Location**: `src/main/java/com/petadoption/model/Pet.java`
- **Encapsulation Features**:
  - All fields private
  - Enumerations for status fields (Gender, VaccinationStatus, AdoptionStatus)
  - BigDecimal for monetary values (weight, adoption fee)
  - Calculated property: `getFormattedAge()` for human-readable display
  - LocalDate/LocalDateTime for temporal data
  - Complete validation in setters

### AdoptionApplication Model
- **Location**: `src/main/java/com/petadoption/model/AdoptionApplication.java`
- **Encapsulation Features**:
  - All fields private
  - ApplicationStatus enumeration
  - Helper methods: `isPending()`, `isApproved()`
  - Complete audit trail fields
  - Immutable `applicationId` after creation

---

## 4. Data Access Objects (DAO)

### UserDAO
- **Location**: `src/main/java/com/petadoption/dao/UserDAO.java`
- **CRUD Operations**:
  - `createUser(User)`: Insert new user
  - `getUserById(int)`: Retrieve by ID
  - `getUserByEmail(String)`: Retrieve by email (login)
  - `getUserByUsername(String)`: Retrieve by username
  - `getAllUsers()`: Retrieve all users
  - `getUsersByType(UserType)`: Filter by role
  - `updateUser(User)`: Update existing user
  - `updateUserPassword(int, String)`: Change password
  - `updateLastLogin(int)`: Track login activity
  - `deleteUser(int)`: Remove user
  - `checkUsernameExists(String)`: Duplicate prevention
  - `checkEmailExists(String)`: Duplicate prevention

### PetDAO
- **Location**: `src/main/java/com/petadoption/dao/PetDAO.java`
- **CRUD Operations**:
  - `createPet(Pet)`: Insert new pet
  - `getPetById(int)`: Retrieve by ID
  - `getAllAvailablePets()`: Adoption portal listing
  - `getPetsByShelterId(int)`: Shelter inventory
  - `getPetsByStatus(AdoptionStatus)`: Filter by status
  - `getPetsBySpecies(String)`: Search by species
  - `getPetsByBreed(String)`: Search by breed
  - `searchPets(String)`: Full-text search
  - `updatePet(Pet)`: Update pet information
  - `updateAdoptionStatus(int, AdoptionStatus, LocalDate)`: Process adoption
  - `deletePet(int)`: Remove pet
  - `countAvailablePetsByShelterId(int)`: Capacity monitoring

### Security Features
- **SQL Injection Prevention**: All queries use PreparedStatement with parameterized queries
- **Input Validation**: Type checking and range validation
- **Resource Management**: Try-finally blocks ensure connection cleanup
- **Transaction Safety**: Individual operation transaction handling

### Example Usage
```java
// Create user
User newUser = new User("johndoe", "john@email.com", "hashed_password", "John Doe", User.UserType.ADOPTER);
UserDAO userDAO = new UserDAO();
boolean success = userDAO.createUser(newUser);

// Retrieve user
User user = userDAO.getUserByEmail("john@email.com");

// Get available pets
PetDAO petDAO = new PetDAO();
List<Pet> availablePets = petDAO.getAllAvailablePets();

// Search pets
List<Pet> dogs = petDAO.searchPets("Labrador");
```

---

## 5. Production-Ready Features

### Error Handling
- Comprehensive SQLException handling
- Meaningful error messages
- Logging of errors for debugging
- Graceful degradation

### Resource Management
- Connection pooling support
- Prepared statement reuse capability
- Result set memory management
- Finally blocks guarantee cleanup

### Performance Optimization
- Strategic database indexing
- Query optimization with WHERE clauses
- Status-based filtering for availability
- Shelter-based partitioning support

### Security
- PreparedStatement usage throughout
- Password hashing required (integration point)
- Email and username uniqueness constraints
- User status flags for deactivation
- Audit logging infrastructure

### Scalability
- Designed for connection pooling
- Transaction-per-operation model
- Pagination-ready query structures
- Indexed columns for large datasets

---

## 6. Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- Spring Boot 3.2.0

### Database Setup
1. Create MySQL database:
   ```sql
   CREATE DATABASE pet_adoption_system;
   USE pet_adoption_system;
   ```

2. Run the schema script:
   ```bash
   mysql -u root -p pet_adoption_system < src/main/resources/database_schema.sql
   ```

### Project Compilation
```bash
mvn clean compile
```

### Running Tests
```bash
mvn test
```

### Build Project
```bash
mvn clean install
```

---

## 7. Configuration Updates

### application.properties (To be created)
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/pet_adoption_system
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool Configuration
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

---

## 8. Next Steps

1. **Service Layer**: Create service classes for business logic
2. **REST Controllers**: Implement REST APIs for CRUD operations
3. **Authentication & Authorization**: Implement Spring Security
4. **Validation**: Add Bean Validation annotations
5. **Exception Handling**: Global exception handler
6. **API Documentation**: Swagger/SpringDoc OpenAPI
7. **Unit Tests**: JUnit and Mockito test suites
8. **Integration Tests**: Database integration tests

---

## 9. File Structure
```
src/main/java/com/petadoption/
├── config/
│   └── DBConnection.java
├── model/
│   ├── User.java
│   ├── Pet.java
│   └── AdoptionApplication.java
├── dao/
│   ├── UserDAO.java
│   └── PetDAO.java
└── service/ (To be created)

src/main/resources/
├── database_schema.sql
└── application.properties (To be created)
```

---

## 10. Code Quality Standards

- **Encapsulation**: All POJOs follow proper encapsulation principles
- **Comments**: Extensive JavaDoc comments for all public methods
- **Error Handling**: Try-catch-finally patterns with resource cleanup
- **Naming Conventions**: Clear, descriptive class and method names
- **SQL Security**: PreparedStatement usage throughout
- **Type Safety**: Strong typing with enumerations for status fields

---

## Summary

This backend implementation provides:
✓ Comprehensive MySQL database schema supporting multi-user adoption platform
✓ Production-ready JDBC connection management
✓ Properly encapsulated POJO models with validation
✓ Complete Data Access Objects with SQL injection prevention
✓ Scalable architecture ready for service and controller layers
✓ Extensive documentation and logging capabilities

All code follows enterprise Java standards and is ready for integration with Spring Boot services and REST controllers.
