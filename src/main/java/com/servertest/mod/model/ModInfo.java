package com.servertest.mod.model;

/**
 * Data model representing information about a loaded mod
 */
public class ModInfo {
    private final String modId;
    private final String version;
    private final String displayName;
    
    public ModInfo(String modId, String version, String displayName) {
        this.modId = modId;
        this.version = version;
        this.displayName = displayName;
    }
    
    /**
     * Get the mod ID
     * @return mod ID string
     */
    public String getModId() {
        return modId;
    }
    
    /**
     * Get the mod version
     * @return version string
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Get the display name of the mod
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %s", displayName, version, modId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ModInfo modInfo = (ModInfo) obj;
        return modId.equals(modInfo.modId) && 
               version.equals(modInfo.version) && 
               displayName.equals(modInfo.displayName);
    }
    
    @Override
    public int hashCode() {
        return modId.hashCode() + version.hashCode() + displayName.hashCode();
    }
}