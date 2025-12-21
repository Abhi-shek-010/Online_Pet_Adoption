package com.petadoption.service;

import com.petadoption.dao.UserDAO;
import com.petadoption.model.User;
import com.petadoption.model.User.UserType;
import com.petadoption.util.PasswordUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * User Service Layer
 * 
 * Handles business logic for user-related operations including
 * authentication, registration, and user management.
 * 
 * MVC Separation:
 * - Servlets handle HTTP requests/responses
 * - Service handles business logic, validation, and authentication
 * - DAO handles database operations
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class UserService {

    private final UserDAO userDAO;

    /**
     * Default constructor initializing DAO dependency
     */
    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates a user with email and password
     * 
     * Business logic:
     * - Validates credentials
     * - Checks password hash
     * - Verifies account is active
     * - Updates last login time
     * 
     * @param email    user's email
     * @param password plain text password
     * @return User object if authentication successful, null otherwise
     * @throws SQLException if database operation fails
     */
    public User authenticateUser(String email, String password) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Retrieve user by email
        User user = userDAO.getUserByEmail(email);

        if (user == null) {
            System.out.println("✗ Authentication failed: User not found");
            return null;
        }

        // Check if account is active
        if (!user.isActive()) {
            System.out.println("✗ Authentication failed: Account is inactive");
            return null;
        }

        // Verify password
        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            System.out.println("✗ Authentication failed: Invalid password");
            return null;
        }

        // Update last login timestamp
        try {
            userDAO.updateLastLogin(user.getUserId());
        } catch (SQLException e) {
            System.err.println("✗ Warning: Could not update last login time");
        }

        System.out.println("✅ Authentication successful for user: " + email);
        return user;
    }

    /**
     * Registers a new user
     * 
     * Business logic:
     * - Validates user data
     * - Checks for duplicate username/email
     * - Hashes password before storage
     * - Sets default values
     * 
     * @param user          User object with registration data
     * @param plainPassword plain text password to hash
     * @return true if registration successful
     * @throws SQLException if database operation fails
     */
    public boolean registerUser(User user, String plainPassword) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Validate user data
        validateUserData(user, plainPassword);

        // Check for duplicate username
        if (userDAO.checkUsernameExists(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check for duplicate email
        if (userDAO.checkEmailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        user.setPasswordHash(hashedPassword);

        // Set default values
        if (user.getUserType() == null) {
            user.setUserType(UserType.ADOPTER); // Default to adopter
        }

        user.setActive(true);
        user.setVerified(false); // Email verification can be added

        // Create user
        boolean created = userDAO.createUser(user);

        if (created) {
            System.out.println("✅ User registered successfully: " + user.getEmail());
        }

        return created;
    }

    /**
     * Retrieves a user by ID
     * 
     * @param userId unique user identifier
     * @return User object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public User getUserById(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        return userDAO.getUserById(userId);
    }

    /**
     * Retrieves a user by email
     * 
     * @param email user's email
     * @return User object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public User getUserByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        return userDAO.getUserByEmail(email);
    }

    /**
     * Retrieves a user by username
     * 
     * @param username user's username
     * @return User object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public User getUserByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        return userDAO.getUserByUsername(username);
    }

    /**
     * Retrieves all users of a specific type
     * 
     * @param userType type of users to retrieve
     * @return List of users of the specified type
     * @throws SQLException if database operation fails
     */
    public List<User> getUsersByType(UserType userType) throws SQLException {
        if (userType == null) {
            throw new IllegalArgumentException("User type cannot be null");
        }

        return userDAO.getUsersByType(userType);
    }

    /**
     * Updates user profile
     * 
     * @param user User object with updated data
     * @return true if update successful
     * @throws SQLException if database operation fails
     */
    public boolean updateUserProfile(User user) throws SQLException {
        if (user == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("Valid user with ID required");
        }

        // Validate updated data (excluding password)
        validateUserBasicData(user);

        return userDAO.updateUser(user);
    }

    /**
     * Changes user password
     * 
     * @param userId      unique user identifier
     * @param oldPassword current password for verification
     * @param newPassword new password to set
     * @return true if password changed successfully
     * @throws SQLException if database operation fails
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Valid user ID required");
        }

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Current password is required");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        // Get current user
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Verify old password
        if (!PasswordUtils.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Hash new password
        String newHashedPassword = PasswordUtils.hashPassword(newPassword);

        // Update password
        return userDAO.updateUserPassword(userId, newHashedPassword);
    }

    /**
     * Deactivates a user account
     * 
     * @param userId unique user identifier
     * @return true if deactivation successful
     * @throws SQLException if database operation fails
     */
    public boolean deactivateUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Valid user ID required");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setActive(false);
        return userDAO.updateUser(user);
    }

    /**
     * Activates a user account
     * 
     * @param userId unique user identifier
     * @return true if activation successful
     * @throws SQLException if database operation fails
     */
    public boolean activateUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Valid user ID required");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setActive(true);
        return userDAO.updateUser(user);
    }

    /**
     * Validates complete user data including password
     * 
     * @param user          User object to validate
     * @param plainPassword password to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserData(User user, String plainPassword) throws IllegalArgumentException {
        validateUserBasicData(user);

        // Password validation
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    /**
     * Validates basic user data (without password)
     * 
     * @param user User object to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserBasicData(User user) throws IllegalArgumentException {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (user.getUsername().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }

    /**
     * Validates email format
     * 
     * @param email email to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Checks if username is available
     * 
     * @param username username to check
     * @return true if username is available
     * @throws SQLException if database operation fails
     */
    public boolean isUsernameAvailable(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        return !userDAO.checkUsernameExists(username);
    }

    /**
     * Checks if email is available
     * 
     * @param email email to check
     * @return true if email is available
     * @throws SQLException if database operation fails
     */
    public boolean isEmailAvailable(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return !userDAO.checkEmailExists(email);
    }
}
