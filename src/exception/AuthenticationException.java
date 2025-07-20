/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception;

/**
 * Authentication Exception - for login and security related errors
 */
public class AuthenticationException extends Exception {
    private String errorCode;
    private int remainingAttempts;
    
    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
        this.remainingAttempts = -1;
    }
    
    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.remainingAttempts = -1;
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
        this.remainingAttempts = -1;
    }
    
    public AuthenticationException(String message, String errorCode, int remainingAttempts) {
        super(message);
        this.errorCode = errorCode;
        this.remainingAttempts = remainingAttempts;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getRemainingAttempts() {
        return remainingAttempts;
    }
    
    public boolean hasRemainingAttempts() {
        return remainingAttempts > 0;
    }
}

/**
 * Authorization Exception - for access control errors
 */
class AuthorizationException extends Exception {
    private String requiredRole;
    private String userRole;
    
    public AuthorizationException(String message) {
        super(message);
    }
    
    public AuthorizationException(String message, String requiredRole, String userRole) {
        super(message);
        this.requiredRole = requiredRole;
        this.userRole = userRole;
    }
    
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getRequiredRole() {
        return requiredRole;
    }
    
    public String getUserRole() {
        return userRole;
    }
}

/**
 * Session Exception - for session management errors
 */
class SessionException extends Exception {
    private long sessionId;
    private boolean isExpired;
    
    public SessionException(String message) {
        super(message);
        this.isExpired = false;
    }
    
    public SessionException(String message, long sessionId, boolean isExpired) {
        super(message);
        this.sessionId = sessionId;
        this.isExpired = isExpired;
    }
    
    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public long getSessionId() {
        return sessionId;
    }
    
    public boolean isExpired() {
        return isExpired;
    }
}