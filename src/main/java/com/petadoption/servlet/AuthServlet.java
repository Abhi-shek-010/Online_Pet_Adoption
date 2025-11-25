package com.petadoption.servlet;

import com.petadoption.config.DBConnection;
import com.petadoption.dao.UserDAO;
import com.petadoption.exception.PetAdoptionException;
import com.petadoption.model.User;
import com.petadoption.util.PasswordUtils;
import com.petadoption.util.SessionUtils;
import org.json.JSONObject;
import org.json.JSONException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.sql.SQLException;

/**
 * Authentication Servlet
 * 
 * Handles user login and registration with role-based access control.
 * Supports three user types: ADMIN, SHELTER, and ADOPTER.
 * 
 * Endpoints:
 * - POST /auth/login - User login
 * - POST /auth/register - User registration
 * - GET /auth/logout - User logout
 * 
 * Features:
 * - Session management for authenticated users
 * - Role-based access control
 * - Password hashing and verification
 * - Comprehensive exception handling
 * - Input validation
 * 
 * Exception Handling:
 * - SQLException for database errors
 * - IllegalArgumentException for invalid input
 * - PetAdoptionException for business logic errors
 * - Generic Exception for unexpected errors
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
@WebServlet(
    name = "AuthServlet",
    urlPatterns = {"/auth/login", "/auth/register", "/auth/logout"},
    description = "Handles user authentication including login, registration, and logout"
)
public class AuthServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    
    /**
     * Initialize the servlet
     * Called once when servlet is first loaded
     */
    @Override
    public void init() throws ServletException {
        super.init();
        this.userDAO = new UserDAO();
        System.out.println("✓ AuthServlet initialized");
        initializeAdminAccount();
    }
    
    /**
     * Initialize admin account on first startup
     * Admin credentials: email=admin, password=admin@123
     */
    private void initializeAdminAccount() {
        try {
            User adminUser = userDAO.getUserByEmail("admin");
            if (adminUser == null) {
                String adminPassword = "admin@123";
                String salt = "admin";
                String passwordHash = PasswordUtils.hashPassword(adminPassword, salt);
                
                User admin = new User("admin", "admin", passwordHash, "Administrator", User.UserType.ADMIN);
                admin.setPhoneNumber("0000000000");
                
                if (userDAO.createUser(admin)) {
                    System.out.println("✓ Admin account initialized - Email: admin, Password: admin@123");
                }
            }
        } catch (Exception e) {
            System.out.println("✓ Admin account already exists or initialization skipped");
        }
    }
    
    /**
     * Handle HTTP GET requests (logout)
     * 
     * @param request HTTP request object
     * @param response HTTP response object
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String action = request.getPathInfo();
            
            if (action != null && action.contains("logout")) {
                handleLogout(request, response, out);
            } else {
                // GET requests not supported for login/register
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                out.println("{\"success\": false, \"message\": \"GET method not allowed for this endpoint\"}");
            }
        } catch (Exception e) {
            System.err.println("✗ Error in doGet: " + e.getMessage());
            handleError(response, out, "Unexpected error", 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
    
    /**
     * Handle HTTP POST requests (login and register)
     * 
     * @param request HTTP request object
     * @param response HTTP response object
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.contains("login")) {
                handleLogin(request, response, out);
            } else if (pathInfo != null && pathInfo.contains("register")) {
                handleRegister(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"success\": false, \"message\": \"Invalid endpoint\"}");
            }
        } catch (PetAdoptionException e) {
            System.err.println("✗ Business Logic Error: " + e.getMessage());
            handleError(response, out, e.getMessage(), e.getHttpStatusCode());
        } catch (SQLException e) {
            System.err.println("✗ Database Error: " + e.getMessage());
            handleError(response, out, "Database error occurred", 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("✗ Unexpected Error: " + e.getMessage());
            handleError(response, out, "Unexpected error occurred", 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
    
    /**
     * Handle user login
     * 
     * Expected parameters:
     * - username: User's username
     * - password: User's password (plain text)
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param out PrintWriter for output
     * @throws SQLException if database error occurs
     * @throws PetAdoptionException if login validation fails
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response, 
                            PrintWriter out) throws SQLException, PetAdoptionException {
        
        try {
            // Parse JSON request body
            JSONObject jsonRequest = parseJsonRequest(request);
            
            String email = jsonRequest.optString("email", "").trim();
            String password = jsonRequest.optString("password", "").trim();
            
            // Validate input
            if (email.isEmpty()) {
                throw new PetAdoptionException("Email is required", 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            if (password.isEmpty()) {
                throw new PetAdoptionException("Password is required", 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            
            // Retrieve user from database by email
            User user = userDAO.getUserByEmail(email);
            
            if (user == null) {
                throw new PetAdoptionException("Invalid email or password", 
                    HttpServletResponse.SC_UNAUTHORIZED);
            }
            
            // Check if user account is active
            if (!user.isActive()) {
                throw new PetAdoptionException("Account is inactive. Please contact administrator", 
                    HttpServletResponse.SC_FORBIDDEN);
            }
            
            // Verify password
            String salt = user.getUsername();
            if (!PasswordUtils.verifyPassword(password, salt, user.getPasswordHash())) {
                throw new PetAdoptionException("Invalid email or password", 
                    HttpServletResponse.SC_UNAUTHORIZED);
            }
            
            // Update last login timestamp
            userDAO.updateLastLogin(user.getUserId());
            
            // Create session for user
            HttpSession session = request.getSession(true);
            SessionUtils.setUserInSession(session, user);
            
            // Log successful login
            System.out.println("✓ User logged in: " + email + " (Type: " + user.getUserType() + ")");
            
            // Return success response
            response.setStatus(HttpServletResponse.SC_OK);
            String roleStr = user.getUserType().toString();
            // Convert ADMIN to ADMIN, SHELTER to SHELTER, ADOPTER to ADOPTER for consistency
            if ("ADMIN".equals(roleStr)) { roleStr = "ADMIN"; }
            else if ("SHELTER".equals(roleStr)) { roleStr = "SHELTER"; }
            else { roleStr = "ADOPTER"; }
            
            out.println("{" +
                "\"success\": true, " +
                "\"message\": \"Login successful\", " +
                "\"userId\": " + user.getUserId() + ", " +
                "\"username\": \"" + escapeJson(user.getUsername()) + "\", " +
                "\"email\": \"" + escapeJson(user.getEmail()) + "\", " +
                "\"role\": \"" + roleStr + "\", " +
                "\"userRole\": \"" + roleStr + "\", " +
                "\"userName\": \"" + escapeJson(user.getFullName()) + "\", " +
                "\"sessionId\": \"" + session.getId() + "\"" +
                "}");
            
        } catch (PetAdoptionException e) {
            throw e;
        } catch (JSONException e) {
            System.err.println("✗ JSON parsing error: " + e.getMessage());
            throw new PetAdoptionException("Invalid request format", 
                HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("✗ Error during login: " + e.getMessage());
            throw new PetAdoptionException("Login failed: " + e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle user registration
     * 
     * Expected parameters:
     * - username: New username (must be unique)
     * - email: User's email (must be unique)
     * - password: User's password
     * - fullName: User's full name
     * - userType: User role (ADMIN, SHELTER, ADOPTER)
     * - phoneNumber: (optional) Contact number
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param out PrintWriter for output
     * @throws SQLException if database error occurs
     * @throws PetAdoptionException if registration validation fails
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response, 
                               PrintWriter out) throws SQLException, PetAdoptionException {
        
        try {
            // Parse JSON request body
            JSONObject jsonRequest = parseJsonRequest(request);
            
            // Extract parameters from JSON
            String username = jsonRequest.optString("username", "").trim();
            String email = jsonRequest.optString("email", "").trim();
            String password = jsonRequest.optString("password", "").trim();
            String confirmPassword = jsonRequest.optString("confirmPassword", "").trim();
            String fullName = jsonRequest.optString("username", "").trim(); // Use username as fullName if not provided
            String userTypeStr = jsonRequest.optString("role", "ADOPTER").toUpperCase(); // Support 'role' field from frontend
            String phoneNumber = jsonRequest.optString("phone", "").trim(); // Support 'phone' field from frontend
            
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                throw new PetAdoptionException("Username is required", 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            if (email == null || email.trim().isEmpty()) {
                throw new PetAdoptionException("Email is required", 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            if (password.isEmpty()) {
                throw new PetAdoptionException("Password is required", 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
                throw new PetAdoptionException("Passwords do not match", 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            if (fullName.isEmpty()) {
                fullName = username; // Use username as fullName if not provided
            }
            if (userTypeStr == null || userTypeStr.trim().isEmpty()) {
                throw new PetAdoptionException("User type is required", 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            
            // Validate password strength
            if (!PasswordUtils.isStrongPassword(password)) {
                throw new PetAdoptionException(
                    "Password must be at least 8 characters with uppercase, lowercase, digit, and special character", 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            
            // Parse user type
            User.UserType userType;
            try {
                userType = User.UserType.valueOf(userTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new PetAdoptionException("Invalid user type: " + userTypeStr, 
                    HttpServletResponse.SC_BAD_REQUEST);
            }
            
            // Check if username already exists
            if (userDAO.checkUsernameExists(username.trim())) {
                throw new PetAdoptionException("Username already exists", 
                    HttpServletResponse.SC_CONFLICT);
            }
            
            // Check if email already exists
            if (userDAO.checkEmailExists(email.trim())) {
                throw new PetAdoptionException("Email already exists", 
                    HttpServletResponse.SC_CONFLICT);
            }
            
            // Hash password
            String salt = username.trim();
            String passwordHash = PasswordUtils.hashPassword(password, salt);
            
            // Create new user
            User newUser = new User(username.trim(), email.trim(), passwordHash, 
                fullName.trim(), userType);
            newUser.setPhoneNumber(phoneNumber);
            
            // Save to database
            boolean created = userDAO.createUser(newUser);
            
            if (!created) {
                throw new PetAdoptionException("Failed to create user account", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
            System.out.println("✓ New user registered: " + username + " (Type: " + userType + ")");
            
            // Return success response
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.println("{" +
                "\"success\": true, " +
                "\"message\": \"Registration successful. Please log in.\", " +
                "\"userId\": " + newUser.getUserId() + ", " +
                "\"username\": \"" + newUser.getUsername() + "\", " +
                "\"userType\": \"" + newUser.getUserType() + "\"" +
                "}");
            
        } catch (PetAdoptionException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("✗ Error during registration: " + e.getMessage());
            throw new PetAdoptionException("Registration failed: " + e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle user logout
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param out PrintWriter for output
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response, 
                             PrintWriter out) {
        try {
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                String username = SessionUtils.getUserFromSession(session).getUsername();
                SessionUtils.clearUserSession(session);
                System.out.println("✓ User logged out: " + username);
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.println("{\"success\": true, \"message\": \"Logout successful\"}");
            
        } catch (Exception e) {
            System.err.println("✗ Error during logout: " + e.getMessage());
            handleError(response, out, "Logout failed", 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle error responses
     * 
     * @param response HTTP response object
     * @param out PrintWriter for output
     * @param message error message
     * @param statusCode HTTP status code
     */
    private void handleError(HttpServletResponse response, PrintWriter out, 
                            String message, int statusCode) {
        response.setStatus(statusCode);
        out.println("{" +
            "\"success\": false, " +
            "\"message\": \"" + escapeJson(message) + "\", " +
            "\"statusCode\": " + statusCode +
            "}");
    }
    
    /**
     * Parse JSON from request body
     * Supports both JSON and form-encoded requests
     * 
     * @param request HTTP request
     * @return JSONObject parsed from request
     * @throws JSONException if JSON parsing fails
     * @throws IOException if reading request fails
     */
    private JSONObject parseJsonRequest(HttpServletRequest request) throws JSONException, IOException {
        String contentType = request.getContentType();
        
        // Check if it's JSON request
        if (contentType != null && contentType.contains("application/json")) {
            // Read JSON from request body
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            return new JSONObject(jsonString);
        } else {
            // Fallback to form parameters (for backward compatibility)
            JSONObject json = new JSONObject();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    json.put(key, values[0]);
                }
            });
            return json;
        }
    }
    
    /**
     * Escape special characters in JSON strings
     * 
     * @param text text to escape
     * @return escaped text safe for JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
