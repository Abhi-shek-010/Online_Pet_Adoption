package com.petadoption.exception;

/**
 * Custom Exception for Pet Adoption System
 * 
 * Provides a standard exception hierarchy for the application.
 * Used to wrap database errors and business logic exceptions.
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class PetAdoptionException extends Exception {
    
    private static final long serialVersionUID = 1L;
    private String errorCode;
    private int httpStatusCode;
    
    /**
     * Constructor with message
     * @param message error message
     */
    public PetAdoptionException(String message) {
        super(message);
        this.httpStatusCode = 500; // Default to internal server error
    }
    
    /**
     * Constructor with message and cause
     * @param message error message
     * @param cause underlying exception
     */
    public PetAdoptionException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = 500;
    }
    
    /**
     * Constructor with message and HTTP status code
     * @param message error message
     * @param httpStatusCode HTTP response code
     */
    public PetAdoptionException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }
    
    /**
     * Constructor with message, error code, and HTTP status
     * @param message error message
     * @param errorCode application-specific error code
     * @param httpStatusCode HTTP response code
     */
    public PetAdoptionException(String message, String errorCode, int httpStatusCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
