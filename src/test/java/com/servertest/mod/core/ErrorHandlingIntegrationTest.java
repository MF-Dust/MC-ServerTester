package com.servertest.mod.core;

import com.servertest.mod.ServerTestMod;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for error handling across all components
 */
@ExtendWith(MockitoExtension.class)
public class ErrorHandlingIntegrationTest {
    
    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private ServerStartedEvent mockServerStartedEvent;
    
    @Mock
    private Thread mockServerThread;
    
    @Mock
    private ModList mockModList;
    
    @Mock
    private IModInfo mockModInfo;
    
    @Mock
    private ArtifactVersion mockVersion;
    
    @Mock
    private SystemExitHandler mockExitHandler;
    
    private EnvironmentDetector environmentDetector;
    private InfoCollector infoCollector;
    private ShutdownManager shutdownManager;
    private ServerMonitor serverMonitor;
    private MockedStatic<ModList> modListMock;
    
    @BeforeEach
    void setUp() {
        ErrorHandler.setExitHandler(mockExitHandler);
        modListMock = mockStatic(ModList.class);
        
        environmentDetector = new EnvironmentDetector();
        infoCollector = new InfoCollector();
        shutdownManager = new ShutdownManager();
        serverMonitor = new ServerMonitor(environmentDetector, infoCollector, shutdownManager);
    }
    
    @AfterEach
    void tearDown() {
        ErrorHandler.resetExitHandler();
        modListMock.close();
    }
    
    @Test
    void testCompleteErrorHandlingFlow_ServerNotReady() {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockServer.isRunning()).thenReturn(false); // Server not ready
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        verify(mockExitHandler).exit(ErrorHandler.SERVER_STARTUP_ERROR);
        assertFalse(serverMonitor.isTestSequenceExecuted());
    }
    
    @Test
    void testCompleteErrorHandlingFlow_InfoCollectionFailure() throws InterruptedException {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        
        // Make server info collection fail
        when(mockServer.getAverageTickTime()).thenThrow(new RuntimeException("TPS calculation failed"));
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        verify(mockExitHandler).exit(ErrorHandler.INFO_COLLECTION_ERROR);
        assertTrue(serverMonitor.isTestSequenceExecuted());
    }
    
    @Test
    void testCompleteErrorHandlingFlow_ModListFailure() throws InterruptedException {
        // Arrange
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        when(mockServer.getAverageTickTime()).thenReturn(50_000_000.0f); // Normal TPS
        
        // Make ModList fail
        modListMock.when(ModList::get).thenThrow(new RuntimeException("ModList access failed"));
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Assert
        verify(mockExitHandler).exit(ErrorHandler.INFO_COLLECTION_ERROR);
        assertTrue(serverMonitor.isTestSequenceExecuted());
    }
    
    @Test
    void testCompleteErrorHandlingFlow_ShutdownFailure() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        when(mockServer.getAverageTickTime()).thenReturn(50_000_000.0f);
        
        // Setup successful mod collection
        modListMock.when(ModList::get).thenReturn(mockModList);
        when(mockModList.getMods()).thenReturn(Arrays.asList(mockModInfo));
        when(mockModInfo.getModId()).thenReturn("testmod");
        when(mockModInfo.getVersion()).thenReturn(mockVersion);
        when(mockVersion.toString()).thenReturn("1.0.0");
        when(mockModInfo.getDisplayName()).thenReturn("Test Mod");
        
        // Make server execution fail during shutdown
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(mockServer).execute(any(Runnable.class));
        
        // Make halt fail
        doThrow(new RuntimeException("Shutdown failed")).when(mockServer).halt(false);
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Wait for async execution
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Shutdown should complete within 5 seconds");
        
        // Assert
        verify(mockExitHandler).exit(ErrorHandler.SHUTDOWN_ERROR);
        assertTrue(serverMonitor.isTestSequenceExecuted());
    }
    
    @Test
    void testCompleteErrorHandlingFlow_SuccessfulExecution() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        when(mockServer.getAverageTickTime()).thenReturn(50_000_000.0f);
        
        // Setup successful mod collection
        modListMock.when(ModList::get).thenReturn(mockModList);
        when(mockModList.getMods()).thenReturn(Arrays.asList(mockModInfo));
        when(mockModInfo.getModId()).thenReturn("testmod");
        when(mockModInfo.getVersion()).thenReturn(mockVersion);
        when(mockVersion.toString()).thenReturn("1.0.0");
        when(mockModInfo.getDisplayName()).thenReturn("Test Mod");
        
        // Setup successful shutdown
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(mockServer).execute(any(Runnable.class));
        
        // Act
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Wait for async execution
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Shutdown should complete within 5 seconds");
        
        // Assert
        verify(mockServer).halt(false);
        verify(mockExitHandler).exit(ErrorHandler.SUCCESS);
        assertTrue(serverMonitor.isTestSequenceExecuted());
    }
    
    @Test
    void testErrorHandlerValidation() {
        // Test null validation
        ErrorHandler.validateNotNull("valid", "testParam", ErrorHandler.ErrorType.GENERAL);
        verifyNoInteractions(mockExitHandler);
        
        // Test null validation failure
        ErrorHandler.validateNotNull(null, "nullParam", ErrorHandler.ErrorType.ENVIRONMENT);
        verify(mockExitHandler).exit(ErrorHandler.ENVIRONMENT_ERROR);
    }
    
    @Test
    void testErrorHandlerRiskyOperations() {
        // Test successful operation
        boolean result = ErrorHandler.executeWithErrorHandling(
            () -> { /* success */ },
            ErrorHandler.ErrorType.GENERAL,
            "Test operation"
        );
        assertTrue(result);
        verifyNoInteractions(mockExitHandler);
        
        // Test failing operation (non-critical)
        boolean failResult = ErrorHandler.executeWithErrorHandling(
            () -> { throw new RuntimeException("Test failure"); },
            ErrorHandler.ErrorType.INFO_COLLECTION,
            "Test operation"
        );
        assertFalse(failResult);
        verifyNoInteractions(mockExitHandler); // Non-critical should not exit
        
        // Test critical operation failure
        ErrorHandler.executeCriticalOperation(
            () -> { throw new RuntimeException("Critical failure"); },
            ErrorHandler.ErrorType.SHUTDOWN,
            "Critical operation"
        );
        verify(mockExitHandler).exit(ErrorHandler.SHUTDOWN_ERROR);
    }
    
    @Test
    void testErrorTypeExitCodes() {
        // Verify all error types have unique exit codes
        assertEquals(0, ErrorHandler.SUCCESS);
        assertEquals(1, ErrorHandler.GENERAL_ERROR);
        assertEquals(2, ErrorHandler.ENVIRONMENT_ERROR);
        assertEquals(3, ErrorHandler.SERVER_STARTUP_ERROR);
        assertEquals(4, ErrorHandler.INFO_COLLECTION_ERROR);
        assertEquals(5, ErrorHandler.SHUTDOWN_ERROR);
        
        // Verify enum mappings
        assertEquals(ErrorHandler.GENERAL_ERROR, ErrorHandler.ErrorType.GENERAL.getExitCode());
        assertEquals(ErrorHandler.ENVIRONMENT_ERROR, ErrorHandler.ErrorType.ENVIRONMENT.getExitCode());
        assertEquals(ErrorHandler.SERVER_STARTUP_ERROR, ErrorHandler.ErrorType.SERVER_STARTUP.getExitCode());
        assertEquals(ErrorHandler.INFO_COLLECTION_ERROR, ErrorHandler.ErrorType.INFO_COLLECTION.getExitCode());
        assertEquals(ErrorHandler.SHUTDOWN_ERROR, ErrorHandler.ErrorType.SHUTDOWN.getExitCode());
    }
    
    @Test
    void testErrorMessageFormatting() {
        // Test critical error handling
        ErrorHandler.handleCriticalError(
            ErrorHandler.ErrorType.SERVER_STARTUP,
            "Test error message",
            new RuntimeException("Test exception")
        );
        
        verify(mockExitHandler).exit(ErrorHandler.SERVER_STARTUP_ERROR);
        
        // Test non-critical error handling (should not exit)
        ErrorHandler.handleNonCriticalError(
            ErrorHandler.ErrorType.INFO_COLLECTION,
            "Non-critical error",
            new RuntimeException("Test exception")
        );
        
        // Should only have one exit call from the critical error above
        verify(mockExitHandler, times(1)).exit(anyInt());
    }
}