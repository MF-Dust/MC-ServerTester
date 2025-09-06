package com.servertest.mod.core;

import com.servertest.mod.ServerTestMod;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServerMonitor class
 */
@ExtendWith(MockitoExtension.class)
class ServerMonitorTest {
    
    @Mock
    private EnvironmentDetector mockEnvironmentDetector;
    
    @Mock
    private InfoCollector mockInfoCollector;
    
    @Mock
    private ShutdownManager mockShutdownManager;
    
    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private ServerStartedEvent mockServerStartedEvent;
    
    @Mock
    private Thread mockServerThread;
    
    @Mock
    private SystemExitHandler mockExitHandler;
    
    private ServerMonitor serverMonitor;
    
    @BeforeEach
    void setUp() {
        ErrorHandler.setExitHandler(mockExitHandler);
        serverMonitor = new ServerMonitor(mockEnvironmentDetector, mockInfoCollector, mockShutdownManager);
    }
    
    @AfterEach
    void tearDown() {
        ErrorHandler.resetExitHandler();
    }
    
    @Test
    void testConstructor() {
        assertNotNull(serverMonitor);
        assertFalse(serverMonitor.isTestSequenceExecuted());
    }
    
    @Test
    void testOnServerStarted_SuccessfulExecution() {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        assertTrue(serverMonitor.isTestSequenceExecuted());
        verify(mockInfoCollector).collectAndOutputServerInfo(mockServer);
        verify(mockShutdownManager).scheduleShutdown(mockServer);
        verify(mockEnvironmentDetector).getEnvironmentInfo();
    }
    
    @Test
    void testOnServerStarted_ServerNotRunning() {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(false);
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        assertFalse(serverMonitor.isTestSequenceExecuted());
        verify(mockExitHandler).exit(ErrorHandler.SERVER_STARTUP_ERROR);
        verify(mockInfoCollector, never()).collectAndOutputServerInfo(any());
    }
    
    @Test
    void testOnServerStarted_ServerThreadNotAlive() {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(false);
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        assertFalse(serverMonitor.isTestSequenceExecuted());
        verify(mockExitHandler).exit(ErrorHandler.SERVER_STARTUP_ERROR);
        verify(mockInfoCollector, never()).collectAndOutputServerInfo(any());
    }
    
    @Test
    void testOnServerStarted_DuplicateExecution() {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        
        // Act - First execution
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Reset mocks for second call
        reset(mockInfoCollector, mockShutdownManager);
        
        // Act - Second execution (should be ignored)
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        assertTrue(serverMonitor.isTestSequenceExecuted());
        verify(mockInfoCollector, never()).collectAndOutputServerInfo(any());
        verify(mockShutdownManager, never()).scheduleShutdown(any());
    }
    
    @Test
    void testOnServerStarted_InfoCollectorThrowsException() {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        
        RuntimeException testException = new RuntimeException("Test exception");
        doThrow(testException).when(mockInfoCollector).collectAndOutputServerInfo(mockServer);
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        assertTrue(serverMonitor.isTestSequenceExecuted());
        verify(mockInfoCollector).collectAndOutputServerInfo(mockServer);
        verify(mockExitHandler).exit(ErrorHandler.INFO_COLLECTION_ERROR);
        verify(mockShutdownManager, never()).scheduleShutdown(any());
    }
    
    @Test
    void testOnServerStarted_ServerReadinessCheckThrowsException() {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenThrow(new RuntimeException("Server check failed"));
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        assertFalse(serverMonitor.isTestSequenceExecuted());
        verify(mockExitHandler).exit(ErrorHandler.SERVER_STARTUP_ERROR);
        verify(mockInfoCollector, never()).collectAndOutputServerInfo(any());
    }
    
    @Test
    void testOnServerStarted_GeneralException() {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenThrow(new RuntimeException("General exception"));
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        assertFalse(serverMonitor.isTestSequenceExecuted());
        verify(mockExitHandler).exit(ErrorHandler.SERVER_STARTUP_ERROR);
        verify(mockInfoCollector, never()).collectAndOutputServerInfo(any());
    }
    
    @Test
    void testResetTestSequenceFlag() {
        // Arrange - Execute test sequence first
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        
        serverMonitor.onServerStarted(mockServerStartedEvent);
        assertTrue(serverMonitor.isTestSequenceExecuted());
        
        // Act
        serverMonitor.resetTestSequenceFlag();
        
        // Assert
        assertFalse(serverMonitor.isTestSequenceExecuted());
    }
    
    @Test
    void testIsTestSequenceExecuted_InitialState() {
        // Assert
        assertFalse(serverMonitor.isTestSequenceExecuted());
    }
}