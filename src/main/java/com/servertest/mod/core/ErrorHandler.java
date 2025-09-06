package com.servertest.mod.core;

import com.servertest.mod.ServerTestMod;

/**
 * Centralized error handling for the Server Test Mod
 * Provides consistent error formatting, logging, and exit status management
 */
public class ErrorHandler {
    
    // Exit status codes
    public static final int SUCCESS = 0;
    public static final int GENERAL_ERROR = 1;
    public static final int ENVIRONMENT_ERROR = 2;
    public static final int SERVER_STARTUP_ERROR = 3;
    public static final int INFO_COLLECTION_ERROR = 4;
    public static final int SHUTDOWN_ERROR = 5;
    
    private static SystemExitHandler exitHandler = new SystemExitHandler.DefaultSystemExitHandler();
    
    /**
     * Set the system exit handler (mainly for testing)
     * @param handler The exit handler to use
     */
    public static void setExitHandler(SystemExitHandler handler) {
        exitHandler = handler;
    }
    
    /**
     * Reset to default exit handler
     */
    public static void resetExitHandler() {
        exitHandler = new SystemExitHandler.DefaultSystemExitHandler();
    }
    
    /**
     * Handle a critical error that should terminate the application
     * @param errorType The type of error (used for exit code)
     * @param message The error message
     * @param throwable Optional throwable for stack trace
     */
    public static void handleCriticalError(ErrorType errorType, String message, Throwable throwable) {
        String formattedMessage = formatErrorMessage(errorType, message);
        
        if (throwable != null) {
            ServerTestMod.LOGGER.error(formattedMessage, throwable);
        } else {
            ServerTestMod.LOGGER.error(formattedMessage);
        }
        
        // Exit with appropriate status code
        exitHandler.exit(errorType.getExitCode());
    }
    
    /**
     * Handle a non-critical error that should be logged but not terminate the application
     * @param errorType The type of error
     * @param message The error message
     * @param throwable Optional throwable for stack trace
     */
    public static void handleNonCriticalError(ErrorType errorType, String message, Throwable throwable) {
        String formattedMessage = formatErrorMessage(errorType, message);
        
        if (throwable != null) {
            ServerTestMod.LOGGER.warn(formattedMessage, throwable);
        } else {
            ServerTestMod.LOGGER.warn(formattedMessage);
        }
    }
    
    /**
     * Format an error message with consistent structure
     * @param errorType The type of error
     * @param message The error message
     * @return Formatted error message
     */
    private static String formatErrorMessage(ErrorType errorType, String message) {
        return String.format("[SERVER-TEST] ERROR [%s]: %s", errorType.name(), message);
    }
    
    /**
     * Validate that a required object is not null
     * @param object The object to validate
     * @param paramName The name of the parameter (for error message)
     * @param errorType The error type if validation fails
     * @throws IllegalArgumentException if object is null
     */
    public static void validateNotNull(Object object, String paramName, ErrorType errorType) {
        if (object == null) {
            handleCriticalError(errorType, paramName + " cannot be null", null);
        }
    }
    
    /**
     * Execute a risky operation with error handling
     * @param operation The operation to execute
     * @param errorType The error type if operation fails
     * @param errorMessage The error message if operation fails
     * @return true if operation succeeded, false otherwise
     */
    public static boolean executeWithErrorHandling(RiskyOperation operation, ErrorType errorType, String errorMessage) {
        try {
            operation.execute();
            return true;
        } catch (Exception e) {
            handleNonCriticalError(errorType, errorMessage + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Execute a critical operation with error handling that will terminate on failure
     * @param operation The operation to execute
     * @param errorType The error type if operation fails
     * @param errorMessage The error message if operation fails
     */
    public static void executeCriticalOperation(RiskyOperation operation, ErrorType errorType, String errorMessage) {
        try {
            operation.execute();
        } catch (Exception e) {
            handleCriticalError(errorType, errorMessage + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Functional interface for operations that might throw exceptions
     */
    @FunctionalInterface
    public interface RiskyOperation {
        void execute() throws Exception;
    }
    
    /**
     * Enum defining different types of errors and their exit codes
     */
    public enum ErrorType {
        GENERAL(GENERAL_ERROR),
        ENVIRONMENT(ENVIRONMENT_ERROR),
        SERVER_STARTUP(SERVER_STARTUP_ERROR),
        INFO_COLLECTION(INFO_COLLECTION_ERROR),
        SHUTDOWN(SHUTDOWN_ERROR);
        
        private final int exitCode;
        
        ErrorType(int exitCode) {
            this.exitCode = exitCode;
        }
        
        public int getExitCode() {
            return exitCode;
        }
    }
}