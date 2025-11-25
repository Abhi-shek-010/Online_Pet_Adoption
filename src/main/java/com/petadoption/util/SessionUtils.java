package com.petadoption.util;

import javax.servlet.http.HttpSession;
import com.petadoption.model.User;

/**
 * Session Utility Class
 * 
 * Provides helper methods for session management including:
 * - User authentication tracking
 * - Role-based access control
 * - Session validation
 * 
 * Security Features:
 * - Session timeout checks
 * - Role verification
 * - Secure session data handling
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class SessionUtils {
    
    // Session attribute keys
    public static final String USER_SESSION_KEY = "loggedInUser";
    public static final String USER_ID_KEY = "userId";
    public static final String USER_TYPE_KEY = "userType";
    public static final String SESSION_TIMEOUT_MINUTES = "sessionTimeout";
    
    // Default session timeout (30 minutes)
    private static final int DEFAULT_TIMEOUT = 30;
    
    /**
     * Set user in session after successful login
     * 
     * @param session HTTP session
     * @param user authenticated user object
     */
    public static void setUserInSession(HttpSession session, User user) {
        if (session != null && user != null) {
            session.setAttribute(USER_SESSION_KEY, user);
            session.setAttribute(USER_ID_KEY, user.getUserId());
            session.setAttribute(USER_TYPE_KEY, user.getUserType().toString());
            session.setMaxInactiveInterval(DEFAULT_TIMEOUT * 60); // Convert to seconds
        }
    }
    
    /**
     * Get logged-in user from session
     * 
     * @param session HTTP session
     * @return User object if logged in, null otherwise
     */
    public static User getUserFromSession(HttpSession session) {
        if (session != null) {
            return (User) session.getAttribute(USER_SESSION_KEY);
        }
        return null;
    }
    
    /**
     * Get user ID from session
     * 
     * @param session HTTP session
     * @return user ID if logged in, -1 otherwise
     */
    public static int getUserIdFromSession(HttpSession session) {
        if (session != null) {
            Object userId = session.getAttribute(USER_ID_KEY);
            if (userId != null) {
                return (Integer) userId;
            }
        }
        return -1;
    }
    
    /**
     * Get user type from session
     * 
     * @param session HTTP session
     * @return user type string (ADMIN, SHELTER, ADOPTER) or null
     */
    public static String getUserTypeFromSession(HttpSession session) {
        if (session != null) {
            return (String) session.getAttribute(USER_TYPE_KEY);
        }
        return null;
    }
    
    /**
     * Check if user is logged in
     * 
     * @param session HTTP session
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isUserLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(USER_SESSION_KEY) != null;
    }
    
    /**
     * Check if user has specific role
     * 
     * @param session HTTP session
     * @param requiredRole role to check (ADMIN, SHELTER, ADOPTER)
     * @return true if user has the required role
     */
    public static boolean hasRole(HttpSession session, String requiredRole) {
        if (!isUserLoggedIn(session)) {
            return false;
        }
        String userType = getUserTypeFromSession(session);
        return requiredRole != null && requiredRole.equalsIgnoreCase(userType);
    }
    
    /**
     * Check if user has any of the specified roles
     * 
     * @param session HTTP session
     * @param roles array of roles to check
     * @return true if user has any of the specified roles
     */
    public static boolean hasAnyRole(HttpSession session, String... roles) {
        if (!isUserLoggedIn(session) || roles == null || roles.length == 0) {
            return false;
        }
        String userType = getUserTypeFromSession(session);
        for (String role : roles) {
            if (role != null && role.equalsIgnoreCase(userType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clear user session (logout)
     * 
     * @param session HTTP session
     */
    public static void clearUserSession(HttpSession session) {
        if (session != null) {
            session.removeAttribute(USER_SESSION_KEY);
            session.removeAttribute(USER_ID_KEY);
            session.removeAttribute(USER_TYPE_KEY);
            session.invalidate();
        }
    }
}
