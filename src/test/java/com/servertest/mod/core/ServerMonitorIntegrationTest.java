package com.servertest.mod.core;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for ServerMonitor event handling
 */
@ExtendWith(MockitoExtension.class)
class ServerMonitorIntegrationTest {
    
    @Mock
    private EnvironmentDetector mockEnvironmentDetector;
    
    @Mock
    private InfoCollector mockInfoCollector;
    
    @Mock
    private ShutdownManager mockShutdownManager;
    
    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private Thread mockServerThread;
    
    @Mock
    private ServerStartedEvent mockServerStartedEvent;
    
    private ServerMonitor serverMonitor;
    
    @BeforeEach
    void setUp() {
        serverMonitor = new ServerMonitor(mockEnvironmentDetector, mockInfoCollector, mockShutdownManager);
    }
    
    @Test
    void testEventHandlerAnnotation() {
        // Verify that the event handler method exists and has the correct annotation
        Method[] methods = ServerMonitor.class.getDeclaredMethods();
        boolean hasSubscribeEventMethod = false;
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(SubscribeEvent.class) && 
                method.getName().equals("onServerStarted")) {
                hasSubscribeEventMethod = true;
                
                // Verify method signature
                Class<?>[] paramTypes = method.getParameterTypes();
                assertEquals(1, paramTypes.length);
                assertEquals(ServerStartedEvent.class, paramTypes[0]);
                break;
            }
        }
        
        assertTrue(hasSubscribeEventMethod, "ServerMonitor should have a @SubscribeEvent method for ServerStartedEvent");
    }
    
    @Test
    void testDirectEventHandling_SuccessfulFlow() {
        // Setup mock behavior
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        
        // Call the event handler directly
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Verify the test sequence was executed
        assertTrue(serverMonitor.isTestSequenceExecuted());
        verify(mockInfoCollector).collectAndOutputServerInfo(mockServer);
        verify(mockShutdownManager).scheduleShutdown(mockServer);
    }
    
    @Test
    void testDirectEventHandling_ServerNotReady() {
        // Setup mock behavior - server not ready
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(false);
        
        // Call the event handler directly
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Verify emergency shutdown was called
        assertFalse(serverMonitor.isTestSequenceExecuted());
        verify(mockShutdownManager).emergencyShutdown("Server not ready after startup event");
        verify(mockInfoCollector, never()).collectAndOutputServerInfo(any());
    }
    
    @Test
    void testDirectEventHandling_MultipleEvents() {
        // Setup mock behavior
        when(mockServerStartedEvent.getServer()).thenReturn(mockServer);
        when(mockEnvironmentDetector.getEnvironmentInfo()).thenReturn("Test Environment");
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        
        // Call the event handler multiple times
        serverMonitor.onServerStarted(mockServerStartedEvent);
        serverMonitor.onServerStarted(mockServerStartedEvent);
        
        // Verify the test sequence was executed only once
        assertTrue(serverMonitor.isTestSequenceExecuted());
        verify(mockInfoCollector, times(1)).collectAndOutputServerInfo(mockServer);
        verify(mockShutdownManager, times(1)).scheduleShutdown(mockServer);
    }
    
    @Test
    void testEventPriority() {
        // Verify that the event handler has appropriate priority
        Method onServerStartedMethod = null;
        
        try {
            onServerStartedMethod = ServerMonitor.class.getMethod("onServerStarted", ServerStartedEvent.class);
        } catch (NoSuchMethodException e) {
            fail("onServerStarted method should exist");
        }
        
        assertNotNull(onServerStartedMethod);
        assertTrue(onServerStartedMethod.isAnnotationPresent(SubscribeEvent.class));
        
        SubscribeEvent annotation = onServerStartedMethod.getAnnotation(SubscribeEvent.class);
        // Default priority should be NORMAL
        assertEquals(EventPriority.NORMAL, annotation.priority());
    }
}