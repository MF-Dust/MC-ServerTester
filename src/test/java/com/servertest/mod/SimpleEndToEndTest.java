package com.servertest.mod;

import com.servertest.mod.core.*;
import net.minecraft.server.MinecraftServer;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simple end-to-end workflow tests without System mocking
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleEndToEndTest {
    
    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private Thread mockServerThread;
    
    @Mock
    private IModInfo mockMod1;
    
    @Mock
    private ArtifactVersion mockVersion1;
    
    private SystemExitHandler mockExitHandler;
    private AtomicInteger exitCode;
    
    private MockedStatic<ModList> modListMock;
    
    @BeforeEach
    void setUp() {
        exitCode = new AtomicInteger(-1);
        mockExitHandler = code -> exitCode.set(code);
        
        ErrorHandler.setExitHandler(mockExitHandler);
        modListMock = mockStatic(ModList.class);
        
        setupMockServer();
        setupMockModList();
    }
    
    @AfterEach
    void tearDown() {
        ErrorHandler.resetExitHandler();
        modListMock.close();
    }
    
    private void setupMockServer() {
        when(mockServer.isRunning()).thenReturn(true);
        when(mockServer.getRunningThread()).thenReturn(mockServerThread);
        when(mockServerThread.isAlive()).thenReturn(true);
        when(mockServer.getAverageTickTime()).thenReturn(50000000.0f); // 20 TPS
    }
    
    private void setupMockModList() {
        when(mockVersion1.toString()).thenReturn("1.0.0");
        when(mockMod1.getModId()).thenReturn("testmod");
        when(mockMod1.getDisplayName()).thenReturn("Test Mod");
        when(mockMod1.getVersion()).thenReturn(mockVersion1);
        
        List<IModInfo> mods = Arrays.asList(mockMod1);
        modListMock.when(ModList::get).thenReturn(mock(ModList.class));
        when(ModList.get().getMods()).thenReturn(mods);
    }
    
    @Test
    void testBasicWorkflow() {
        // Test basic server monitoring workflow
        EnvironmentDetector detector = new EnvironmentDetector(TestEnvironmentProvider.githubActions());
        InfoCollector collector = new InfoCollector();
        ShutdownManager shutdownManager = new ShutdownManager();
        ServerMonitor monitor = new ServerMonitor(detector, collector, shutdownManager);
        
        // This should work without throwing exceptions
        assertDoesNotThrow(() -> {
            collector.collectAndOutputServerInfo(mockServer);
        });
    }
    
    @Test
    void testEnvironmentDetection() {
        // Test CI environment detection
        EnvironmentDetector ciDetector = new EnvironmentDetector(TestEnvironmentProvider.githubActions());
        assertTrue(ciDetector.isInCIEnvironment());
        
        // Test development environment detection
        EnvironmentDetector devDetector = new EnvironmentDetector(TestEnvironmentProvider.development());
        assertFalse(devDetector.isInCIEnvironment());
    }
    
    @Test
    void testErrorHandling() {
        // Test that critical errors trigger exit
        ErrorHandler.handleCriticalError(
            ErrorHandler.ErrorType.SERVER_STARTUP,
            "Test critical error",
            null
        );
        
        // Verify exit was called with correct code
        assertEquals(ErrorHandler.SERVER_STARTUP_ERROR, exitCode.get());
    }
    
    @Test
    void testShutdownManager() {
        ShutdownManager shutdownManager = new ShutdownManager();
        
        // Test shutdown scheduling
        assertDoesNotThrow(() -> {
            shutdownManager.scheduleShutdown(mockServer);
        });
    }
}