package exception;

/**
 * Base exception class for MotorPH Payroll System
 * Demonstrates proper exception hierarchy (AOOP Requirement)
 */
public abstract class MotorPHException extends Exception {
    private String errorCode;
    private String userMessage;
    private String technicalMessage;
    
    public MotorPHException(String errorCode, String userMessage, String technicalMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage;
    }
    
    public MotorPHException(String errorCode, String userMessage, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage;
    }
    
    public String getErrorCode() { return errorCode; }
    public String getUserMessage() { return userMessage; }
    public String getTechnicalMessage() { return technicalMessage; }
}

/**
 * Database related exceptions
 */
class DatabaseException extends MotorPHException {
    public DatabaseException(String userMessage, String technicalMessage) {
        super("DB_ERROR", userMessage, technicalMessage);
    }
    
    public DatabaseException(String userMessage, String technicalMessage, Throwable cause) {
        super("DB_ERROR", userMessage, technicalMessage, cause);
    }
}

/**
 * Authentication and authorization exceptions
 */
class SecurityException extends MotorPHException {
    public SecurityException(String userMessage, String technicalMessage) {
        super("SEC_ERROR", userMessage, technicalMessage);
    }
}

/**
 * Business logic validation exceptions
 */
class ValidationException extends MotorPHException {
    public ValidationException(String userMessage, String technicalMessage) {
        super("VAL_ERROR", userMessage, technicalMessage);
    }
}

/**
 * Payroll calculation exceptions
 */
class PayrollCalculationException extends MotorPHException {
    public PayrollCalculationException(String userMessage, String technicalMessage) {
        super("PAY_ERROR", userMessage, technicalMessage);
    }
    
    public PayrollCalculationException(String userMessage, String technicalMessage, Throwable cause) {
        super("PAY_ERROR", userMessage, technicalMessage, cause);
    }
}

/**
 * Report generation exceptions
 */
class ReportGenerationException extends MotorPHException {
    public ReportGenerationException(String userMessage, String technicalMessage) {
        super("RPT_ERROR", userMessage, technicalMessage);
    }
    
    public ReportGenerationException(String userMessage, String technicalMessage, Throwable cause) {
        super("RPT_ERROR", userMessage, technicalMessage, cause);
    }
}

/**
 * File I/O exceptions
 */
class FileOperationException extends MotorPHException {
    public FileOperationException(String userMessage, String technicalMessage) {
        super("FILE_ERROR", userMessage, technicalMessage);
    }
    
    public FileOperationException(String userMessage, String technicalMessage, Throwable cause) {
        super("FILE_ERROR", userMessage, technicalMessage, cause);
    }
}