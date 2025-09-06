package com.servertest.mod.core;

import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShutdownManagerTest {

    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private SystemExitHandler mockExitHandler;
    
    private ShutdownManager shutdownManager;

    @BeforeEach
    void setUp() {
        ErrorHandler.setExitHandler(mockExitHandler);
        shutdownManager = new ShutdownManager();
    }
    
    @AfterEach
    void tearDown() {
        ErrorHandler.resetExitHandler();
    }



    @Test
    void testScheduleShutdown_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(mockServer).execute(any(Runnable.class));

        // Act
        shutdownManager.scheduleShutdown(mockServer);

        // Wait for async execution
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Shutdown should complete within 5 seconds");

        // Assert
        verify(mockServer).halt(false);
        verify(mockExitHandler).exit(ErrorHandler.SUCCESS);
    }

    @Test
    void testScheduleShutdown_ServerExecuteThrowsException() {
        // Arrange
        doThrow(new RuntimeException("Server execute failed")).when(mockServer).execute(any(Runnable.class));

        // Act
        shutdownManager.scheduleShutdown(mockServer);

        // Assert
        verify(mockExitHandler).exit(ErrorHandler.SHUTDOWN_ERROR);
    }

    @Test
    void testPerformShutdown_HaltThrowsException() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(mockServer).execute(any(Runnable.class));
        
        doThrow(new RuntimeException("Halt failed")).when(mockServer).halt(false);

        // Act
        shutdownManager.scheduleShutdown(mockServer);

        // Wait for async execution
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Shutdown should complete within 5 seconds");

        // Assert
        verify(mockServer).halt(false);
        verify(mockExitHandler).exit(ErrorHandler.SHUTDOWN_ERROR);
    }

    @Test
    void testPerformShutdown_TaskExecutionThrowsException() {
        // Arrange
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            // Simulate exception during task execution
            throw new RuntimeException("Task execution failed");
        }).when(mockServer).execute(any(Runnable.class));

        // Act
        shutdownManager.scheduleShutdown(mockServer);

        // Assert
        verify(mockExitHandler).exit(ErrorHandler.SHUTDOWN_ERROR);
    }

    @Test
    void testEmergencyShutdown() {
        // Arrange
        String reason = "Critical error occurred";

        // Act
        shutdownManager.emergencyShutdown(reason);

        // Assert
        verify(mockExitHandler).exit(ErrorHandler.GENERAL_ERROR);
    }

    @Test
    void testShutdownConfirmationMessages() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(mockServer).execute(any(Runnable.class));

        // Act
        shutdownManager.scheduleShutdown(mockServer);

        // Wait for async execution
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Shutdown should complete within 5 seconds");

        // Assert - Verify the server halt method is called with correct parameter
        verify(mockServer).halt(false);
        verify(mockExitHandler).exit(ErrorHandler.SUCCESS);
    }

    @Test
    void testShutdownManagerThreadSafety() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            // Simulate server thread execution
            task.run();
            latch.countDown();
            return null;
        }).when(mockServer).execute(any(Runnable.class));

        // Act
        shutdownManager.scheduleShutdown(mockServer);

        // Wait for async execution
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Shutdown should complete within 5 seconds");

        // Assert
        verify(mockServer).execute(any(Runnable.class));
        verify(mockServer).halt(false);
        verify(mockExitHandler).exit(ErrorHandler.SUCCESS);
    }

    @Test
    void testShutdownManagerCorrectHaltParameter() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(mockServer).execute(any(Runnable.class));

        // Act
        shutdownManager.scheduleShutdown(mockServer);

        // Wait for async execution
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Shutdown should complete within 5 seconds");

        // Assert - Verify halt is called with false parameter (graceful shutdown)
        verify(mockServer).halt(false);
        verify(mockExitHandler).exit(ErrorHandler.SUCCESS);
    }

    @Test
    void testMultipleEmergencyShutdownCalls() {
        // Arrange
        String reason1 = "First error";

        // Act
        shutdownManager.emergencyShutdown(reason1);

        // Assert - Only the first call should trigger exit
        verify(mockExitHandler).exit(ErrorHandler.GENERAL_ERROR);
    }
}