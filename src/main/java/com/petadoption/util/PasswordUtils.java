package com.petadoption.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password Utility Class
 * 
 * Provides secure password hashing and verification using SHA-256.
 * Includes salt generation for additional security.
 * 
 * Security Features:
 * - SHA-256 hashing algorithm
 * - Salt generation using SecureRandom
 * - Base64 encoding for storage
 * 
 * Note: In production, use BCrypt or Argon2 for better security
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class PasswordUtils {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * Generate a random salt for password hashing
     * 
     * @return Base64 encoded salt string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hash a password with salt
     * 
     * @param password plain text password
     * @param salt     salt for hashing
     * @return Base64 encoded hash
     * @throws NoSuchAlgorithmException if SHA-256 algorithm not available
     */
    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }

        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(Base64.getDecoder().decode(salt));
        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedPassword);
    }

    /**
     * Verify a password against a hash
     * 
     * @param password plain text password to verify
     * @param salt     original salt used for hashing
     * @param hash     stored password hash
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String salt, String hash) {
        if (password == null || salt == null || hash == null) {
            return false;
        }

        try {
            String computedHash = hashPassword(password, salt);
            return computedHash.equals(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate password strength
     * 
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     * 
     * @param password password to validate
     * @return true if password meets strength requirements
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    /**
     * Hash a password (convenience method that generates salt internally)
     * Returns format: salt:hash
     * 
     * @param password plain text password
     * @return combined salt and hash string (salt:hash format)
     */
    public static String hashPassword(String password) {
        String salt = generateSalt();
        try {
            String hash = hashPassword(password, salt);
            return salt + ":" + hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify a password against a combined salt:hash string
     * 
     * @param password       plain text password to verify
     * @param storedPassword stored password in salt:hash format
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String storedPassword) {
        if (password == null || storedPassword == null) {
            return false;
        }

        String[] parts = storedPassword.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String salt = parts[0];
        String hash = parts[1];

        return verifyPassword(password, salt, hash);
    }
}
