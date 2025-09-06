package com.servertest.mod.core;

import com.servertest.mod.model.TestResult;
import com.servertest.mod.model.ModInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InfoCollector class
 */
public class InfoCollectorTest {
    
    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private ModList mockModList;
    
    @Mock
    private IModInfo mockModInfo1;
    
    @Mock
    private IModInfo mockModInfo2;
    
    @Mock
    private ArtifactVersion mockVersion1;
    
    @Mock
    private ArtifactVersion mockVersion2;
    
    private InfoCollector infoCollector;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        infoCollector = new InfoCollector();
    }
    
    @Test
    void testCalculateTPS_PerfectPerformance() {
        // Perfect performance: 50ms per tick = 50,000,000 nanoseconds
        when(mockServer.getAverageTickTime()).thenReturn(50_000_000.0f);
        
        // Use reflection to access private method for testing
        try {
            java.lang.reflect.Method calculateTPS = InfoCollector.class.getDeclaredMethod("calculateTPS", MinecraftServer.class);
            calculateTPS.setAccessible(true);
            
            double tps = (Double) calculateTPS.invoke(infoCollector, mockServer);
            
            assertEquals(20.0, tps, 0.1, "Perfect performance should result in 20 TPS");
        } catch (Exception e) {
            fail("Failed to test calculateTPS method: " + e.getMessage());
        }
    }
    
    @Test
    void testCalculateTPS_SlowPerformance() {
        // Slow performance: 100ms per tick = 100,000,000 nanoseconds
        when(mockServer.getAverageTickTime()).thenReturn(100_000_000.0f);
        
        try {
            java.lang.reflect.Method calculateTPS = InfoCollector.class.getDeclaredMethod("calculateTPS", MinecraftServer.class);
            calculateTPS.setAccessible(true);
            
            double tps = (Double) calculateTPS.invoke(infoCollector, mockServer);
            
            assertEquals(10.0, tps, 0.1, "100ms per tick should result in 10 TPS");
        } catch (Exception e) {
            fail("Failed to test calculateTPS method: " + e.getMessage());
        }
    }
    
    @Test
    void testCalculateTPS_VeryFastPerformance() {
        // Very fast performance: 25ms per tick = 25,000,000 nanoseconds
        // This should be capped at 20 TPS
        when(mockServer.getAverageTickTime()).thenReturn(25_000_000.0f);
        
        try {
            java.lang.reflect.Method calculateTPS = InfoCollector.class.getDeclaredMethod("calculateTPS", MinecraftServer.class);
            calculateTPS.setAccessible(true);
            
            double tps = (Double) calculateTPS.invoke(infoCollector, mockServer);
            
            assertEquals(20.0, tps, 0.1, "TPS should be capped at 20.0 even with faster performance");
        } catch (Exception e) {
            fail("Failed to test calculateTPS method: " + e.getMessage());
        }
    }
    
    @Test
    void testGetAverageTickTime() {
        // 75ms per tick = 75,000,000 nanoseconds
        when(mockServer.getAverageTickTime()).thenReturn(75_000_000.0f);
        
        try {
            java.lang.reflect.Method getAverageTickTime = InfoCollector.class.getDeclaredMethod("getAverageTickTime", MinecraftServer.class);
            getAverageTickTime.setAccessible(true);
            
            long tickTime = (Long) getAverageTickTime.invoke(infoCollector, mockServer);
            
            assertEquals(75L, tickTime, "75,000,000 nanoseconds should convert to 75 milliseconds");
        } catch (Exception e) {
            fail("Failed to test getAverageTickTime method: " + e.getMessage());
        }
    }
    
    @Test
    void testCollectAndOutputServerInfo_Success() {
        // Setup server mock
        when(mockServer.getAverageTickTime()).thenReturn(50_000_000.0f); // 20 TPS
        
        // Setup mod list mock
        try (MockedStatic<ModList> mockedModList = mockStatic(ModList.class)) {
            mockedModList.when(ModList::get).thenReturn(mockModList);
            
            // Setup mod info mocks
            when(mockModInfo1.getModId()).thenReturn("minecraft");
            when(mockModInfo1.getVersion()).thenReturn(mockVersion1);
            when(mockVersion1.toString()).thenReturn("1.20.1");
            when(mockModInfo1.getDisplayName()).thenReturn("Minecraft");
            
            when(mockModInfo2.getModId()).thenReturn("forge");
            when(mockModInfo2.getVersion()).thenReturn(mockVersion2);
            when(mockVersion2.toString()).thenReturn("47.2.0");
            when(mockModInfo2.getDisplayName()).thenReturn("Minecraft Forge");
            
            when(mockModList.getMods()).thenReturn(Arrays.asList(mockModInfo1, mockModInfo2));
            
            // This test mainly verifies that no exceptions are thrown
            // In a real scenario, we would capture the log output to verify formatting
            assertDoesNotThrow(() -> {
                infoCollector.collectAndOutputServerInfo(mockServer);
            }, "collectAndOutputServerInfo should not throw exceptions with valid input");
        }
    }
    
    @Test
    void testCollectAndOutputServerInfo_ServerException() {
        // Setup server to throw exception
        when(mockServer.getAverageTickTime()).thenThrow(new RuntimeException("Server error"));
        
        // This test verifies that exceptions are re-thrown after handling
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            infoCollector.collectAndOutputServerInfo(mockServer);
        }, "collectAndOutputServerInfo should re-throw exceptions after handling");
        
        assertEquals("Failed to collect server information", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Server error", exception.getCause().getMessage());
    }
    
    @Test
    void testCollectModInfo_EmptyModList() {
        try (MockedStatic<ModList> mockedModList = mockStatic(ModList.class)) {
            mockedModList.when(ModList::get).thenReturn(mockModList);
            when(mockModList.getMods()).thenReturn(Arrays.asList());
            
            // Use reflection to access private method for testing
            try {
                java.lang.reflect.Method collectModInfo = InfoCollector.class.getDeclaredMethod("collectModInfo");
                collectModInfo.setAccessible(true);
                
                @SuppressWarnings("unchecked")
                List<ModInfo> modInfoList = (List<ModInfo>) collectModInfo.invoke(infoCollector);
                
                assertNotNull(modInfoList, "Mod info list should not be null");
                assertTrue(modInfoList.isEmpty(), "Mod info list should be empty when no mods are loaded");
            } catch (Exception e) {
                fail("Failed to test collectModInfo method: " + e.getMessage());
            }
        }
    }
    
    @Test
    void testCollectModInfo_MultipleModsLoaded() {
        try (MockedStatic<ModList> mockedModList = mockStatic(ModList.class)) {
            mockedModList.when(ModList::get).thenReturn(mockModList);
            
            // Setup mod info mocks
            when(mockModInfo1.getModId()).thenReturn("minecraft");
            when(mockModInfo1.getVersion()).thenReturn(mockVersion1);
            when(mockVersion1.toString()).thenReturn("1.20.1");
            when(mockModInfo1.getDisplayName()).thenReturn("Minecraft");
            
            when(mockModInfo2.getModId()).thenReturn("servertest");
            when(mockModInfo2.getVersion()).thenReturn(mockVersion2);
            when(mockVersion2.toString()).thenReturn("1.0.0");
            when(mockModInfo2.getDisplayName()).thenReturn("Server Test Mod");
            
            when(mockModList.getMods()).thenReturn(Arrays.asList(mockModInfo1, mockModInfo2));
            
            // Use reflection to access private method for testing
            try {
                java.lang.reflect.Method collectModInfo = InfoCollector.class.getDeclaredMethod("collectModInfo");
                collectModInfo.setAccessible(true);
                
                @SuppressWarnings("unchecked")
                List<ModInfo> modInfoList = (List<ModInfo>) collectModInfo.invoke(infoCollector);
                
                assertNotNull(modInfoList, "Mod info list should not be null");
                assertEquals(2, modInfoList.size(), "Should collect information for all loaded mods");
                
                // Verify first mod
                ModInfo minecraftMod = modInfoList.get(0);
                assertEquals("minecraft", minecraftMod.getModId(), "First mod ID should be minecraft");
                assertEquals("1.20.1", minecraftMod.getVersion(), "First mod version should be 1.20.1");
                assertEquals("Minecraft", minecraftMod.getDisplayName(), "First mod display name should be Minecraft");
                
                // Verify second mod
                ModInfo serverTestMod = modInfoList.get(1);
                assertEquals("servertest", serverTestMod.getModId(), "Second mod ID should be servertest");
                assertEquals("1.0.0", serverTestMod.getVersion(), "Second mod version should be 1.0.0");
                assertEquals("Server Test Mod", serverTestMod.getDisplayName(), "Second mod display name should be Server Test Mod");
                
            } catch (Exception e) {
                fail("Failed to test collectModInfo method: " + e.getMessage());
            }
        }
    }
    
    @Test
    void testCollectModInfo_ModListException() {
        try (MockedStatic<ModList> mockedModList = mockStatic(ModList.class)) {
            mockedModList.when(ModList::get).thenThrow(new RuntimeException("ModList error"));
            
            // Use reflection to access private method for testing
            try {
                java.lang.reflect.Method collectModInfo = InfoCollector.class.getDeclaredMethod("collectModInfo");
                collectModInfo.setAccessible(true);
                
                // This should throw an InvocationTargetException wrapping the RuntimeException
                Exception exception = assertThrows(Exception.class, () -> {
                    collectModInfo.invoke(infoCollector);
                }, "collectModInfo should propagate ModList exceptions");
                
                // Check that the cause is the expected RuntimeException
                assertTrue(exception.getCause() instanceof RuntimeException, 
                    "Exception cause should be RuntimeException");
                assertEquals("ModList error", exception.getCause().getMessage(), 
                    "Exception message should match the original error");
                
            } catch (Exception e) {
                fail("Failed to test collectModInfo method: " + e.getMessage());
            }
        }
    }
    
    @Test
    void testModInfoDataModel() {
        // Test ModInfo creation and methods
        ModInfo modInfo = new ModInfo("testmod", "2.1.0", "Test Mod");
        
        assertEquals("testmod", modInfo.getModId(), "Mod ID should match constructor parameter");
        assertEquals("2.1.0", modInfo.getVersion(), "Version should match constructor parameter");
        assertEquals("Test Mod", modInfo.getDisplayName(), "Display name should match constructor parameter");
        
        // Test toString method
        String expectedString = "Test Mod (2.1.0) - testmod";
        assertEquals(expectedString, modInfo.toString(), "toString should format mod info correctly");
        
        // Test equals method
        ModInfo sameModInfo = new ModInfo("testmod", "2.1.0", "Test Mod");
        ModInfo differentModInfo = new ModInfo("othermod", "1.0.0", "Other Mod");
        
        assertEquals(modInfo, sameModInfo, "ModInfo objects with same data should be equal");
        assertNotEquals(modInfo, differentModInfo, "ModInfo objects with different data should not be equal");
        assertNotEquals(modInfo, null, "ModInfo should not equal null");
        assertNotEquals(modInfo, "string", "ModInfo should not equal different type");
        
        // Test hashCode consistency
        assertEquals(modInfo.hashCode(), sameModInfo.hashCode(), "Equal objects should have same hash code");
    }
    
    @Test
    void testCollectAndOutputServerInfo_ModListException() {
        // Setup server mock to work normally
        when(mockServer.getAverageTickTime()).thenReturn(50_000_000.0f);
        
        // Setup ModList to throw exception
        try (MockedStatic<ModList> mockedModList = mockStatic(ModList.class)) {
            mockedModList.when(ModList::get).thenThrow(new RuntimeException("ModList access error"));
            
            // This should handle the error and re-throw
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                infoCollector.collectAndOutputServerInfo(mockServer);
            }, "Should re-throw exception when ModList fails");
            
            assertEquals("Failed to collect server information", exception.getMessage());
            assertTrue(exception.getCause() instanceof RuntimeException);
            assertEquals("ModList access error", exception.getCause().getMessage());
        }
    }
    
    @Test
    void testCollectAndOutputServerInfo_NullServer() {
        // Test with null server
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            infoCollector.collectAndOutputServerInfo(null);
        }, "Should handle null server gracefully");
        
        assertEquals("Failed to collect server information", exception.getMessage());
        assertTrue(exception.getCause() instanceof NullPointerException);
    }
    
    @Test
    void testCalculateTPS_ExtremeValues() {
        // Test with very small tick time (should be capped at 20 TPS)
        when(mockServer.getAverageTickTime()).thenReturn(1_000_000.0f); // 1ms per tick
        
        try {
            java.lang.reflect.Method calculateTPS = InfoCollector.class.getDeclaredMethod("calculateTPS", MinecraftServer.class);
            calculateTPS.setAccessible(true);
            
            double tps = (Double) calculateTPS.invoke(infoCollector, mockServer);
            
            assertEquals(20.0, tps, 0.1, "TPS should be capped at 20.0 for very fast performance");
        } catch (Exception e) {
            fail("Failed to test calculateTPS with extreme values: " + e.getMessage());
        }
        
        // Test with very large tick time
        when(mockServer.getAverageTickTime()).thenReturn(1_000_000_000.0f); // 1 second per tick
        
        try {
            java.lang.reflect.Method calculateTPS = InfoCollector.class.getDeclaredMethod("calculateTPS", MinecraftServer.class);
            calculateTPS.setAccessible(true);
            
            double tps = (Double) calculateTPS.invoke(infoCollector, mockServer);
            
            assertEquals(1.0, tps, 0.1, "1 second per tick should result in 1 TPS");
        } catch (Exception e) {
            fail("Failed to test calculateTPS with extreme values: " + e.getMessage());
        }
    }
}