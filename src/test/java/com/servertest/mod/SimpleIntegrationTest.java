package com.servertest.mod;

import com.servertest.mod.core.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple integration tests for ServerTestMod components
 * Tests basic component integration without complex mocking
 */
class SimpleIntegrationTest {
    
    @Test
    void testComponentInitialization() {
        // Test that all components can be initialized properly
        EnvironmentDetector detector = new EnvironmentDetector();
        InfoCollector collector = new InfoCollector();
        ShutdownManager manager = new ShutdownManager();
        ServerMonitor monitor = new ServerMonitor(detector, collector, manager);
        
        // Verify components are created
        assertNotNull(detector);
        assertNotNull(collector);
        assertNotNull(manager);
        assertNotNull(monitor);
        
        // Test basic functionality
        assertFalse(monitor.isTestSequenceExecuted());
        assertNotNull(detector.getEnvironmentInfo());
        
        // Test reset functionality
        monitor.resetTestSequenceFlag();
        assertFalse(monitor.isTestSequenceExecuted());
    }
    
    @Test
    void testEnvironmentDetection() {
        EnvironmentDetector detector = new EnvironmentDetector();
        
        // Should return consistent results
        boolean isCI1 = detector.isInCIEnvironment();
        boolean isCI2 = detector.isInCIEnvironment();
        assertEquals(isCI1, isCI2);
        
        // Should provide environment info
        String envInfo = detector.getEnvironmentInfo();
        assertNotNull(envInfo);
        assertFalse(envInfo.isEmpty());
        
        // In test environment, should typically be "Development environment"
        assertTrue(envInfo.contains("environment"));
    }
    
    @Test
    void testServerMonitorState() {
        EnvironmentDetector detector = new EnvironmentDetector();
        InfoCollector collector = new InfoCollector();
        ShutdownManager manager = new ShutdownManager();
        ServerMonitor monitor = new ServerMonitor(detector, collector, manager);
        
        // Initial state
        assertFalse(monitor.isTestSequenceExecuted());
        
        // After reset
        monitor.resetTestSequenceFlag();
        assertFalse(monitor.isTestSequenceExecuted());
    }
    
    @Test
    void testComponentIntegration() {
        // Test that components work together properly
        EnvironmentDetector detector = new EnvironmentDetector();
        InfoCollector collector = new InfoCollector();
        ShutdownManager manager = new ShutdownManager();
        
        // Create monitor with all components
        ServerMonitor monitor = new ServerMonitor(detector, collector, manager);
        
        // Verify integration
        assertNotNull(monitor);
        assertFalse(monitor.isTestSequenceExecuted());
        
        // Test that environment info is accessible
        String envInfo = detector.getEnvironmentInfo();
        assertNotNull(envInfo);
        
        // Test state management
        monitor.resetTestSequenceFlag();
        assertFalse(monitor.isTestSequenceExecuted());
    }
}