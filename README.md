# 🐾 PawMatch - Online Pet Adoption System

A modern, full-stack web application for adopting pets online. **PawMatch** connects pet adopters with animals from shelters and rescue organizations, making the adoption process simple, transparent, and joyful.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue?style=flat-square)
![HTML5](https://img.shields.io/badge/HTML5-Latest-red?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-purple?style=flat-square)

---

## 📋 Project Overview

**PawMatch** is a complete pet adoption platform with:

- 🏠 **Beautiful Landing Page** - Attractive hero section with featured pets
- 🔐 **User Authentication** - Secure login and registration with role-based access
- 🐕 **Pet Listings** - Browse available pets with filters and search
- 📋 **Adoption Applications** - Submit and track adoption applications
- 👤 **User Dashboard** - View adopted pets and application history
- 👨‍💼 **Role-Based Access** - Admin, Shelter, and Adopter roles
- 🎨 **Responsive Design** - Works on desktop, tablet, and mobile
- ✨ **Smooth Animations** - Pet-themed animations and transitions

---

## 🚀 Quick Start (5 Minutes)

### Prerequisites
- **Java 17+** installed
- **Maven 3.9+** installed  
- **MySQL 8.0+** installed and running
- **Git** (optional, for cloning)

### Installation Steps

#### 1. **Clone the Repository**
```bash
git clone https://github.com/yourusername/pawmatch.git
cd pawmatch
```

#### 2. **Configure Database**

**Option A: Using MySQL CLI**
```bash
# Login to MySQL
mysql -u root -p

# Create database and import schema
CREATE DATABASE pet_adoption_system;
USE pet_adoption_system;
SOURCE src/main/resources/database_schema.sql;

# Verify tables
SHOW TABLES;
```

**Option B: Using MySQL Workbench**
1. Open MySQL Workbench
2. Click **File → Open SQL Script**
3. Select `src/main/resources/database_schema.sql`
4. Click Execute (⚡)
5. Verify 4 tables created: `users`, `pets`, `adoption_applications`, `adoptions`

#### 3. **Configure Database Connection**

Edit `application.properties` (create if not exists):

**Create file: `src/main/resources/application.properties`**
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/pet_adoption_system
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Server Configuration
server.port=8080
spring.application.name=pet-adoption-system
```

#### 4. **Build and Run**

```bash
# Clean build
mvn clean compile

# Package application
mvn package

# Start the server (Choose one)
mvn spring-boot:run

# Or run the JAR file
java -jar target/pet-adoption-system-0.0.1-SNAPSHOT.jar
```

#### 5. **Access the Application**

- **Home Page**: http://localhost:8080/
- **Login Page**: http://localhost:8080/login.html
- **Register Page**: http://localhost:8080/register.html
- **Pet Listing**: http://localhost:8080/pets-listing.html
- **Dashboard**: http://localhost:8080/dashboard.html (after login)

---

## 👤 Test Credentials

### Admin Account (Full Access)
```
Email: admin
Password: admin@123
```

### Demo Accounts
- **Adopter**: adopter@example.com / Adopter@123
- **Shelter**: shelter@example.com / Shelter@123

---

## 📁 Project Structure

```
Online_Pet_Adoption/
├── src/
│   ├── main/
│   │   ├── java/com/petadoption/
│   │   │   ├── config/
│   │   │   │   ├── DBConnection.java        # Database connection
│   │   │   │   └── SecurityConfig.java      # Spring Security configuration
│   │   │   ├── dao/
│   │   │   │   ├── PetDAO.java              # Pet database operations
│   │   │   │   └── UserDAO.java             # User database operations
│   │   │   ├── model/
│   │   │   │   ├── Pet.java                 # Pet entity
│   │   │   │   ├── User.java                # User entity
│   │   │   │   └── AdoptionApplication.java # Adoption application entity
│   │   │   ├── servlet/
│   │   │   │   ├── AuthServlet.java         # Authentication (login/register)
│   │   │   │   └── PetServlet.java          # Pet operations
│   │   │   ├── util/
│   │   │   │   ├── PasswordUtils.java       # Password hashing
│   │   │   │   └── SessionUtils.java        # Session management
│   │   │   ├── exception/
│   │   │   │   └── PetAdoptionException.java # Custom exception
│   │   │   └── PetAdoptionApplication.java  # Spring Boot entry point
│   │   │
│   │   ├── resources/
│   │   │   ├── application.properties       # Application configuration
│   │   │   └── database_schema.sql          # Database schema
│   │   │
│   │   └── webapp/
│   │       ├── index.html                   # Home/landing page
│   │       ├── login.html                   # Login page
│   │       ├── register.html                # Registration page
│   │       ├── pets-listing.html            # Pet browsing page
│   │       ├── dashboard.html               # User dashboard
│   │       └── WEB-INF/
│   │           └── web.xml                  # Web application configuration
│   │
│   └── test/
│       └── java/                            # Unit tests (ready to add)
│
├── pom.xml                                  # Maven dependencies
└── README.md                                # This file
```

---

## 🗄️ Database Schema

### 4 Main Tables

#### `users`
- User ID, Email, Username, Password Hash, Full Name
- Phone Number, User Type (ADMIN/SHELTER/ADOPTER)
- Creation timestamp, Last login

#### `pets`
- Pet ID, Name, Breed, Age, Description
- Shelter ID (foreign key), Status (available/adopted)
- Image URL, Special needs info

#### `adoption_applications`
- Application ID, User ID, Pet ID
- Application status, Personal info
- Submission date, Decision date

#### `adoptions`
- Adoption ID, User ID, Pet ID
- Adoption date, Contract number
- Fees and terms

---

## 🔐 Security Features

### Authentication & Authorization
- **Email-based Login** - Secure authentication
- **Password Hashing** - SHA-256 with salt
- **Session Management** - HTTP sessions for state management
- **Role-Based Access Control** - ADMIN, SHELTER, ADOPTER roles
- **CSRF Protection** - Cross-site request forgery prevention

### Data Security
- **Parameterized Queries** - Protection against SQL injection
- **Input Validation** - Frontend and backend validation
- **CORS Configuration** - Secure cross-origin requests

---

## 🎨 Frontend Features

### Pages
1. **index.html** - Beautiful landing page with pet animations
2. **login.html** - Modern login form with demo buttons
3. **register.html** - User registration with role selection
4. **pets-listing.html** - Browse and filter available pets
5. **dashboard.html** - User profile and adoption tracking

### Technologies
- **HTML5** - Semantic markup
- **Tailwind CSS** - Modern styling framework
- **JavaScript ES6+** - Dynamic interactions
- **Font Awesome 6.4** - Icon library
- **Fetch API** - AJAX requests

### Animations
- 🐾 **Floating Paw Prints** - Gentle floating animation
- 🐕 **Pet Emojis** - Animated pet characters
- ✨ **Smooth Transitions** - Hover and load effects
- 📍 **Bounce Effects** - Interactive elements

---

## ⚙️ Backend Architecture

### Framework & Technologies
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication & authorization
- **Hibernate/JPA** - ORM framework
- **MySQL Driver** - Database connectivity
- **JSON** - API data format

### REST API Endpoints

#### Authentication
```
POST /auth/login          - User login
POST /auth/register       - User registration
GET  /auth/logout         - User logout
```

#### Pets
```
GET  /pets                - Get all available pets
GET  /pets/{id}           - Get pet details
POST /pets                - Add new pet (Shelter only)
```

#### Adoption Applications
```
POST /adoptions/apply     - Submit adoption application
GET  /adoptions/status    - Check application status
```

---

## 🛠️ Development Setup

### IDE Setup (IntelliJ IDEA / Eclipse)

#### IntelliJ IDEA
1. **File → Open** → Select project folder
2. **File → Project Structure → SDK** → Set Java 17
3. **File → Settings → Build, Execution, Deployment → Maven** → Configure Maven home
4. Right-click `pom.xml` → **Maven → Reload project**
5. Click **Run → Run 'PetAdoptionApplication'**

#### Eclipse
1. **File → Import → Existing Maven Projects**
2. Select project folder
3. Right-click project → **Maven → Update Project**
4. Right-click **src/main/java → Run As → Java Application** (select `PetAdoptionApplication`)

### VS Code Setup
1. Install extensions: Extension Pack for Java, Spring Boot Extension Pack
2. Open terminal: `mvn spring-boot:run`
3. Navigate to http://localhost:8080/

---

## 📊 API Examples

### Login Request
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin",
    "password": "admin@123"
  }'
```

### Response
```json
{
  "success": true,
  "userId": 1,
  "email": "admin",
  "userName": "Administrator",
  "role": "ADMIN",
  "message": "Login successful"
}
```

### Get Pets
```bash
curl http://localhost:8080/pets
```

---

## 🐛 Troubleshooting

### Issue: Database Connection Failed
**Solution:**
```bash
# Check MySQL is running
mysql -u root -p

# Verify credentials in application.properties
spring.datasource.username=root
spring.datasource.password=root
```

### Issue: Port 8080 Already in Use
**Solution:**
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (Windows)
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=8081
```

### Issue: Maven Build Fails
**Solution:**
```bash
# Clear Maven cache
mvn clean

# Rebuild with verbose output
mvn -X clean compile

# Check Java version
java -version
```

### Issue: "Registration error: Unexpected token '<'"
**Solution:**
- Make sure `Content-Type: application/json` is set
- Check endpoint URL matches your domain
- Clear browser cache (Ctrl+Shift+Delete)

---

## 📚 Additional Resources

### Documentation Files
- **BACKEND_ARCHITECTURE.md** - Detailed backend explanation
- **FRONTEND_DOCUMENTATION.md** - Frontend guide
- **SQL_CONFIGURATION_GUIDE.md** - Database setup guide
- **QUICK_START_DEPLOYMENT.md** - Quick deployment steps

### Learning Resources
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Tailwind CSS](https://tailwindcss.com)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [JavaScript MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/)

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit changes** (`git commit -m 'Add amazing feature'`)
4. **Push to branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

---

## 📝 License

This project is licensed under the **MIT License** - see the LICENSE file for details.

---

## 👨‍💻 Author

Created by **Abhi** - Pet Adoption System Developer

---

## 🌟 Features Roadmap

### v1.0 (Current)
- ✅ User authentication and authorization
- ✅ Pet listing and browsing
- ✅ Adoption applications
- ✅ User dashboard
- ✅ Responsive design

### v2.0 (Future)
- 📋 Email notifications
- 🗺️ Pet filter by location
- 📱 Mobile app
- ⭐ Ratings and reviews
- 💬 In-app messaging
- 📊 Admin analytics

---

## 📞 Support

For issues or questions:
1. Check the **Troubleshooting** section
2. Review existing GitHub issues
3. Create a new GitHub issue with details

---

## ❤️ Acknowledgments

- **Spring Boot Community** - Framework
- **Tailwind CSS** - Styling
- **Font Awesome** - Icons
- **MySQL** - Database

---

**Happy Pet Adoption! 🐾🐕🐈**

Made with ❤️ for pet lovers everywhere.

