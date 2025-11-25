package com.petadoption.model;

import java.time.LocalDateTime;

/**
 * User Entity Class (POJO)
 * 
 * Represents a user in the Pet Adoption System with role-based design.
 * Supports three user types: ADMIN, SHELTER, and ADOPTER
 * 
 * Encapsulation Principles Applied:
 * - All fields are private
 * - Public getters and setters for controlled access
 * - Validation in setters where applicable
 * - Immutable user_id after creation
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class User {
    
    // Enumerations for user roles
    public enum UserType {
        ADMIN("Admin"),
        SHELTER("Shelter"),
        ADOPTER("Adopter");
        
        private final String displayName;
        
        UserType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Private fields - encapsulated
    private int userId;                    // Unique identifier (auto-generated)
    private String username;               // Unique login username
    private String email;                  // Unique email address
    private String passwordHash;           // Hashed password (never stored in plain text)
    private String fullName;               // User's full name
    private String phoneNumber;            // Contact phone number
    private UserType userType;             // Role: ADMIN, SHELTER, ADOPTER
    private String address;                // Physical address
    private String city;                   // City of residence
    private String state;                  // State/Province
    private String postalCode;             // ZIP/Postal code
    private String country;                // Country
    private boolean verified;              // Email verification status
    private boolean active;                // Account active/inactive status
    private LocalDateTime registrationDate; // Account creation date
    private LocalDateTime lastLogin;       // Last login timestamp
    private LocalDateTime createdAt;       // Record creation timestamp
    private LocalDateTime updatedAt;       // Record last update timestamp
    
    /**
     * Default constructor
     * Initializes all fields to default values
     */
    public User() {
        this.verified = false;
        this.active = true;
        this.registrationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential user information
     * 
     * @param username unique username
     * @param email unique email address
     * @param passwordHash hashed password
     * @param fullName user's full name
     * @param userType user's role (ADMIN, SHELTER, ADOPTER)
     */
    public User(String username, String email, String passwordHash, String fullName, UserType userType) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.userType = userType;
    }
    
    // ===== Getters and Setters with Encapsulation =====
    
    /**
     * Get user ID (immutable - set only during creation)
     * @return unique user identifier
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Set user ID (typically called after database insertion)
     * @param userId the auto-generated user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    /**
     * Get username with validation
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Set username with validation
     * @param username must be non-empty and at least 3 characters
     * @throws IllegalArgumentException if username is invalid
     */
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
        this.username = username.trim();
    }
    
    /**
     * Get email address
     * @return email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Set email with validation
     * @param email must be valid email format
     * @throws IllegalArgumentException if email format is invalid
     */
    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.toLowerCase();
    }
    
    /**
     * Get password hash (never expose actual password)
     * @return hashed password
     */
    public String getPasswordHash() {
        return passwordHash;
    }
    
    /**
     * Set password hash (expects pre-hashed password)
     * @param passwordHash hashed password from authentication service
     */
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        this.passwordHash = passwordHash;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        this.fullName = fullName.trim();
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.matches("^\\+?[0-9\\-\\s()]{7,}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.phoneNumber = phoneNumber;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        if (userType == null) {
            throw new IllegalArgumentException("User type cannot be null");
        }
        this.userType = userType;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Generate string representation of User
     * @return formatted user information
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType=" + userType +
                ", active=" + active +
                ", verified=" + verified +
                '}';
    }
}
