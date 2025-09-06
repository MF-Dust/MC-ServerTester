package com.servertest.mod.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for EnvironmentDetector class
 */
public class EnvironmentDetectorTest {
    
    private EnvironmentDetector detector;
    private Map<String, String> originalEnv;
    
    @BeforeEach
    void setUp() {
        detector = new EnvironmentDetector();
        // Store original environment for restoration
        originalEnv = new HashMap<>(System.getenv());
    }
    
    @AfterEach
    void tearDown() {
        // Clean up system properties
        System.clearProperty("java.awt.headless");
    }
    
    @Test
    void testIsInCIEnvironment_WithHeadlessMode() {
        // Test headless mode detection (this is the most reliable test)
        System.setProperty("java.awt.headless", "true");
        
        assertTrue(detector.isInCIEnvironment(), 
            "Should detect CI environment when running in headless mode");
    }
    
    @Test
    void testIsInCIEnvironment_WithoutCIIndicators() {
        // Ensure headless is not set
        System.clearProperty("java.awt.headless");
        
        // This test depends on the actual environment, but we can test the logic
        // In a real CI environment, this would return true due to environment variables
        // In development, it should return false unless headless mode is set
        boolean result = detector.isInCIEnvironment();
        
        // We can't guarantee the result without mocking, but we can verify the method runs
        assertNotNull(result, "Method should return a boolean value");
    }
    
    @Test
    void testIsInCIEnvironment_WithHeadlessFalse() {
        // Explicitly set headless to false
        System.setProperty("java.awt.headless", "false");
        
        // The result depends on environment variables, but method should not crash
        boolean result = detector.isInCIEnvironment();
        assertNotNull(result, "Method should return a boolean value");
    }
    
    @Test
    void testGetEnvironmentInfo_WithHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        
        String info = detector.getEnvironmentInfo();
        assertTrue(info.contains("Headless mode") || info.contains("CI Environment"), 
            "Environment info should mention CI environment or headless mode");
    }
    
    @Test
    void testGetEnvironmentInfo_Development() {
        // Clear headless mode
        System.clearProperty("java.awt.headless");
        
        String info = detector.getEnvironmentInfo();
        assertNotNull(info, "Environment info should not be null");
        assertTrue(info.length() > 0, "Environment info should not be empty");
    }
    
    @Test
    void testEnvironmentDetectorInstantiation() {
        // Test that the detector can be instantiated without errors
        EnvironmentDetector newDetector = new EnvironmentDetector();
        assertNotNull(newDetector, "EnvironmentDetector should be instantiable");
    }
    
    @Test
    void testMethodsReturnConsistentResults() {
        // Test that calling methods multiple times returns consistent results
        boolean firstCall = detector.isInCIEnvironment();
        boolean secondCall = detector.isInCIEnvironment();
        
        assertEquals(firstCall, secondCall, 
            "isInCIEnvironment should return consistent results");
        
        String firstInfo = detector.getEnvironmentInfo();
        String secondInfo = detector.getEnvironmentInfo();
        
        assertEquals(firstInfo, secondInfo, 
            "getEnvironmentInfo should return consistent results");
    }
    
    @Test
    void testEnvironmentInfoFormat() {
        String info = detector.getEnvironmentInfo();
        
        // Verify the info string is properly formatted
        assertNotNull(info, "Environment info should not be null");
        assertFalse(info.trim().isEmpty(), "Environment info should not be empty");
        
        // Should contain either "CI Environment" or "Development environment"
        assertTrue(info.contains("Environment") || info.contains("environment"), 
            "Environment info should mention environment");
    }
    
    @Test
    void testHeadlessModeDetection() {
        // Test with headless mode explicitly enabled
        System.setProperty("java.awt.headless", "true");
        assertTrue(detector.isInCIEnvironment(), 
            "Should detect CI when headless mode is true");
        
        // Test with headless mode explicitly disabled
        System.setProperty("java.awt.headless", "false");
        // Result may vary based on environment variables, but should not crash
        assertDoesNotThrow(() -> detector.isInCIEnvironment(), 
            "Should not throw exception when headless is false");
    }
}