package com.servertest.mod.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ErrorHandler
 */
public class ErrorHandlerTest {
    
    @Mock
    private SystemExitHandler mockExitHandler;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ErrorHandler.setExitHandler(mockExitHandler);
    }
    
    @AfterEach
    void tearDown() {
        ErrorHandler.resetExitHandler();
    }
    
    @Test
    void testHandleCriticalError_WithThrowable() {
        // Given
        ErrorHandler.ErrorType errorType = ErrorHandler.ErrorType.SERVER_STARTUP;
        String message = "Test error message";
        RuntimeException throwable = new RuntimeException("Test exception");
        
        // When
        ErrorHandler.handleCriticalError(errorType, message, throwable);
        
        // Then
        verify(mockExitHandler).exit(ErrorHandler.SERVER_STARTUP_ERROR);
    }
    
    @Test
    void testHandleCriticalError_WithoutThrowable() {
        // Given
        ErrorHandler.ErrorType errorType = ErrorHandler.ErrorType.INFO_COLLECTION;
        String message = "Test error message";
        
        // When
        ErrorHandler.handleCriticalError(errorType, message, null);
        
        // Then
        verify(mockExitHandler).exit(ErrorHandler.INFO_COLLECTION_ERROR);
    }
    
    @Test
    void testHandleNonCriticalError_DoesNotExit() {
        // Given
        ErrorHandler.ErrorType errorType = ErrorHandler.ErrorType.GENERAL;
        String message = "Non-critical error";
        RuntimeException throwable = new RuntimeException("Test exception");
        
        // When
        ErrorHandler.handleNonCriticalError(errorType, message, throwable);
        
        // Then
        verifyNoInteractions(mockExitHandler);
    }
    
    @Test
    void testValidateNotNull_WithValidObject() {
        // Given
        String validObject = "test";
        
        // When & Then (should not throw or exit)
        assertDoesNotThrow(() -> {
            ErrorHandler.validateNotNull(validObject, "testParam", ErrorHandler.ErrorType.GENERAL);
        });
        
        verifyNoInteractions(mockExitHandler);
    }
    
    @Test
    void testValidateNotNull_WithNullObject() {
        // Given
        Object nullObject = null;
        
        // When
        ErrorHandler.validateNotNull(nullObject, "testParam", ErrorHandler.ErrorType.ENVIRONMENT);
        
        // Then
        verify(mockExitHandler).exit(ErrorHandler.ENVIRONMENT_ERROR);
    }
    
    @Test
    void testExecuteWithErrorHandling_Success() {
        // Given
        ErrorHandler.RiskyOperation successOperation = () -> {
            // Do nothing - success case
        };
        
        // When
        boolean result = ErrorHandler.executeWithErrorHandling(
            successOperation, 
            ErrorHandler.ErrorType.GENERAL, 
            "Test operation"
        );
        
        // Then
        assertTrue(result);
        verifyNoInteractions(mockExitHandler);
    }
    
    @Test
    void testExecuteWithErrorHandling_Failure() {
        // Given
        ErrorHandler.RiskyOperation failingOperation = () -> {
            throw new RuntimeException("Operation failed");
        };
        
        // When
        boolean result = ErrorHandler.executeWithErrorHandling(
            failingOperation, 
            ErrorHandler.ErrorType.INFO_COLLECTION, 
            "Test operation"
        );
        
        // Then
        assertFalse(result);
        verifyNoInteractions(mockExitHandler); // Non-critical error should not exit
    }
    
    @Test
    void testExecuteCriticalOperation_Success() {
        // Given
        ErrorHandler.RiskyOperation successOperation = () -> {
            // Do nothing - success case
        };
        
        // When & Then
        assertDoesNotThrow(() -> {
            ErrorHandler.executeCriticalOperation(
                successOperation, 
                ErrorHandler.ErrorType.GENERAL, 
                "Test operation"
            );
        });
        
        verifyNoInteractions(mockExitHandler);
    }
    
    @Test
    void testExecuteCriticalOperation_Failure() {
        // Given
        ErrorHandler.RiskyOperation failingOperation = () -> {
            throw new RuntimeException("Critical operation failed");
        };
        
        // When
        ErrorHandler.executeCriticalOperation(
            failingOperation, 
            ErrorHandler.ErrorType.SHUTDOWN, 
            "Critical test operation"
        );
        
        // Then
        verify(mockExitHandler).exit(ErrorHandler.SHUTDOWN_ERROR);
    }
    
    @Test
    void testErrorTypeExitCodes() {
        // Test that all error types have correct exit codes
        assertEquals(ErrorHandler.GENERAL_ERROR, ErrorHandler.ErrorType.GENERAL.getExitCode());
        assertEquals(ErrorHandler.ENVIRONMENT_ERROR, ErrorHandler.ErrorType.ENVIRONMENT.getExitCode());
        assertEquals(ErrorHandler.SERVER_STARTUP_ERROR, ErrorHandler.ErrorType.SERVER_STARTUP.getExitCode());
        assertEquals(ErrorHandler.INFO_COLLECTION_ERROR, ErrorHandler.ErrorType.INFO_COLLECTION.getExitCode());
        assertEquals(ErrorHandler.SHUTDOWN_ERROR, ErrorHandler.ErrorType.SHUTDOWN.getExitCode());
    }
    
    @Test
    void testExitStatusConstants() {
        // Verify exit status constants have expected values
        assertEquals(0, ErrorHandler.SUCCESS);
        assertEquals(1, ErrorHandler.GENERAL_ERROR);
        assertEquals(2, ErrorHandler.ENVIRONMENT_ERROR);
        assertEquals(3, ErrorHandler.SERVER_STARTUP_ERROR);
        assertEquals(4, ErrorHandler.INFO_COLLECTION_ERROR);
        assertEquals(5, ErrorHandler.SHUTDOWN_ERROR);
    }
}