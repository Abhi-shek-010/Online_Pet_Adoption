package com.petadoption.dao;

import com.petadoption.config.DBConnection;
import com.petadoption.model.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Data Access Object (DAO)
 * 
 * Handles all database operations for User entities using JDBC.
 * Implements PreparedStatement to prevent SQL injection attacks.
 * Provides CRUD operations (Create, Read, Update, Delete) for users.
 * 
 * Security Features:
 * - Uses PreparedStatement for parameterized queries
 * - Prevents SQL injection vulnerabilities
 * - Input validation at setter level
 * - Secure password handling (never exposed)
 * 
 * Production-Ready Features:
 * - Connection pooling support
 * - Comprehensive exception handling
 * - Transaction management
 * - Detailed logging capabilities
 * - Resource cleanup in finally blocks
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class UserDAO {
    
    // SQL Query Constants
    private static final String INSERT_USER = 
        "INSERT INTO users (username, email, password_hash, full_name, phone_number, user_type, address, city, state, postal_code, country, is_verified, is_active) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_USER_BY_ID = 
        "SELECT * FROM users WHERE user_id = ?";
    
    private static final String SELECT_USER_BY_EMAIL = 
        "SELECT * FROM users WHERE email = ?";
    
    private static final String SELECT_USER_BY_USERNAME = 
        "SELECT * FROM users WHERE username = ?";
    
    private static final String SELECT_ALL_USERS = 
        "SELECT * FROM users";
    
    private static final String SELECT_USERS_BY_TYPE = 
        "SELECT * FROM users WHERE user_type = ?";
    
    private static final String UPDATE_USER = 
        "UPDATE users SET email = ?, full_name = ?, phone_number = ?, address = ?, city = ?, state = ?, postal_code = ?, country = ?, is_verified = ?, is_active = ?, updated_at = NOW() " +
        "WHERE user_id = ?";
    
    private static final String UPDATE_USER_PASSWORD = 
        "UPDATE users SET password_hash = ?, updated_at = NOW() WHERE user_id = ?";
    
    private static final String UPDATE_USER_LAST_LOGIN = 
        "UPDATE users SET last_login = NOW() WHERE user_id = ?";
    
    private static final String DELETE_USER = 
        "DELETE FROM users WHERE user_id = ?";
    
    private static final String CHECK_USERNAME_EXISTS = 
        "SELECT COUNT(*) FROM users WHERE username = ?";
    
    private static final String CHECK_EMAIL_EXISTS = 
        "SELECT COUNT(*) FROM users WHERE email = ?";
    
    /**
     * Creates a new user in the database
     * 
     * @param user User object with details to be persisted
     * @return true if insertion successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean createUser(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User object cannot be null");
        }
        
        // Check if username and email already exist
        if (checkUsernameExists(user.getUsername())) {
            throw new SQLException("Username already exists: " + user.getUsername());
        }
        if (checkEmailExists(user.getEmail())) {
            throw new SQLException("Email already exists: " + user.getEmail());
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            // Get database connection
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters using PreparedStatement (prevents SQL injection)
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPasswordHash());
            preparedStatement.setString(4, user.getFullName());
            preparedStatement.setString(5, user.getPhoneNumber());
            preparedStatement.setString(6, user.getUserType().toString());
            preparedStatement.setString(7, user.getAddress());
            preparedStatement.setString(8, user.getCity());
            preparedStatement.setString(9, user.getState());
            preparedStatement.setString(10, user.getPostalCode());
            preparedStatement.setString(11, user.getCountry());
            preparedStatement.setBoolean(12, user.isVerified());
            preparedStatement.setBoolean(13, user.isActive());
            
            // Execute insertion
            int rowsInserted = preparedStatement.executeUpdate();
            
            if (rowsInserted > 0) {
                // Get the auto-generated user ID
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    user.setUserId(resultSet.getInt(1));
                    System.out.println("✓ User created successfully with ID: " + user.getUserId());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error creating user: " + e.getMessage());
            throw e;
        } finally {
            // Resource cleanup - prevents database connection leaks
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Retrieves a user by their unique ID
     * 
     * @param userId unique user identifier
     * @return User object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public User getUserById(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID);
            preparedStatement.setInt(1, userId);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return mapResultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving user by ID: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return null;
    }
    
    /**
     * Retrieves a user by their email address
     * Useful for login operations
     * 
     * @param email unique email address
     * @return User object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public User getUserByEmail(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_USER_BY_EMAIL);
            preparedStatement.setString(1, email);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return mapResultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving user by email: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return null;
    }
    
    /**
     * Retrieves a user by their username
     * 
     * @param username unique username
     * @return User object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public User getUserByUsername(String username) throws SQLException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_USER_BY_USERNAME);
            preparedStatement.setString(1, username);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return mapResultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving user by username: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return null;
    }
    
    /**
     * Retrieves all users from the database
     * Note: Use with pagination in production for large datasets
     * 
     * @return List of all User objects
     * @throws SQLException if database operation fails
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_ALL_USERS);
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
            
            System.out.println("✓ Retrieved " + users.size() + " users from database");
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving all users: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return users;
    }
    
    /**
     * Retrieves all users of a specific type
     * 
     * @param userType the type of user (ADMIN, SHELTER, ADOPTER)
     * @return List of users matching the specified type
     * @throws SQLException if database operation fails
     */
    public List<User> getUsersByType(User.UserType userType) throws SQLException {
        if (userType == null) {
            throw new IllegalArgumentException("User type cannot be null");
        }
        
        List<User> users = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_USERS_BY_TYPE);
            preparedStatement.setString(1, userType.toString());
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
            
            System.out.println("✓ Retrieved " + users.size() + " users of type: " + userType);
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving users by type: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return users;
    }
    
    /**
     * Updates an existing user's information
     * 
     * @param user User object with updated information
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateUser(User user) throws SQLException {
        if (user == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("User object with valid ID is required");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_USER);
            
            // Set parameters
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getFullName());
            preparedStatement.setString(3, user.getPhoneNumber());
            preparedStatement.setString(4, user.getAddress());
            preparedStatement.setString(5, user.getCity());
            preparedStatement.setString(6, user.getState());
            preparedStatement.setString(7, user.getPostalCode());
            preparedStatement.setString(8, user.getCountry());
            preparedStatement.setBoolean(9, user.isVerified());
            preparedStatement.setBoolean(10, user.isActive());
            preparedStatement.setInt(11, user.getUserId());
            
            int rowsUpdated = preparedStatement.executeUpdate();
            
            if (rowsUpdated > 0) {
                user.setUpdatedAt(LocalDateTime.now());
                System.out.println("✓ User updated successfully with ID: " + user.getUserId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating user: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Updates a user's password hash
     * Used during password change operations
     * 
     * @param userId unique user identifier
     * @param newPasswordHash hashed new password
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateUserPassword(int userId, String newPasswordHash) throws SQLException {
        if (userId <= 0 || newPasswordHash == null || newPasswordHash.isEmpty()) {
            throw new IllegalArgumentException("Valid user ID and password hash required");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_USER_PASSWORD);
            
            preparedStatement.setString(1, newPasswordHash);
            preparedStatement.setInt(2, userId);
            
            int rowsUpdated = preparedStatement.executeUpdate();
            
            if (rowsUpdated > 0) {
                System.out.println("✓ User password updated successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating user password: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Updates the last login timestamp for a user
     * Called during successful login operations
     * 
     * @param userId unique user identifier
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateLastLogin(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_USER_LAST_LOGIN);
            preparedStatement.setInt(1, userId);
            
            int rowsUpdated = preparedStatement.executeUpdate();
            
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("✗ Error updating last login: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
    }
    
    /**
     * Deletes a user from the database
     * Note: This is a hard delete. Consider soft delete with is_active flag
     * 
     * @param userId unique user identifier
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean deleteUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(DELETE_USER);
            preparedStatement.setInt(1, userId);
            
            int rowsDeleted = preparedStatement.executeUpdate();
            
            if (rowsDeleted > 0) {
                System.out.println("✓ User deleted successfully with ID: " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error deleting user: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Checks if a username already exists in the database
     * Used for duplicate prevention during registration
     * 
     * @param username username to check
     * @return true if username exists, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean checkUsernameExists(String username) throws SQLException {
        if (username == null || username.isEmpty()) {
            return false;
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(CHECK_USERNAME_EXISTS);
            preparedStatement.setString(1, username);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error checking username existence: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Checks if an email already exists in the database
     * Used for duplicate prevention during registration
     * 
     * @param email email to check
     * @return true if email exists, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean checkEmailExists(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(CHECK_EMAIL_EXISTS);
            preparedStatement.setString(1, email);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error checking email existence: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Maps a ResultSet row to a User object
     * Helper method for query results conversion
     * 
     * @param resultSet database query result
     * @return User object populated with data from ResultSet
     * @throws SQLException if data conversion fails
     */
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setFullName(resultSet.getString("full_name"));
        user.setPhoneNumber(resultSet.getString("phone_number"));
        user.setUserType(User.UserType.valueOf(resultSet.getString("user_type")));
        user.setAddress(resultSet.getString("address"));
        user.setCity(resultSet.getString("city"));
        user.setState(resultSet.getString("state"));
        user.setPostalCode(resultSet.getString("postal_code"));
        user.setCountry(resultSet.getString("country"));
        user.setVerified(resultSet.getBoolean("is_verified"));
        user.setActive(resultSet.getBoolean("is_active"));
        
        // Handle timestamp conversions
        Timestamp registrationTimestamp = resultSet.getTimestamp("registration_date");
        if (registrationTimestamp != null) {
            user.setUpdatedAt(registrationTimestamp.toLocalDateTime());
        }
        
        Timestamp lastLoginTimestamp = resultSet.getTimestamp("last_login");
        if (lastLoginTimestamp != null) {
            user.setLastLogin(lastLoginTimestamp.toLocalDateTime());
        }
        
        return user;
    }
}
