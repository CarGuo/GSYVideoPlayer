package tv.danmaku.ijk.media.exo2;

/**
 * Example usage demonstrating SurfaceControl reparenting integration
 * This shows how to use the enhanced SurfaceControl functionality that addresses
 * the issues in androidx/media/issues/2733 and ExoPlayer/issues/5428
 */
public class SurfaceControlExample {
    
    /**
     * Example showing basic SurfaceControl usage (automatic)
     */
    public void demonstrateBasicUsage() {
        // Create player manager - SurfaceControl reparenting is initialized automatically
        Exo2PlayerManager playerManager = new Exo2PlayerManager();
        
        // Check if SurfaceControl reparenting is available
        boolean usingSurfaceControl = playerManager.isUsingSurfaceControl();
        
        if (usingSurfaceControl) {
            System.out.println("✓ SurfaceControl reparenting system enabled (API 29+)");
            System.out.println("  - Video content can be efficiently hidden/shown");
            System.out.println("  - Optimal buffer sizing for different surface dimensions");
            System.out.println("  - Atomic surface operations via SurfaceControl.Transaction");
            System.out.println("  - True reparenting capabilities (detached state support)");
        } else {
            System.out.println("ℹ Standard surface switching active (compatibility mode)");
        }
        
        // Surface switching works exactly as before - no code changes needed
        // The showDisplay() method now uses SurfaceControl reparenting when available
        
        playerManager.release();
    }
    
    /**
     * Example showing enhanced surface operations with dimensions
     */
    public void demonstrateEnhancedSurfaceOperations() {
        Exo2PlayerManager playerManager = new Exo2PlayerManager();
        
        // Example surfaces (in real usage, these would be your actual surfaces)
        android.view.Surface surface1 = null; // Your first surface
        android.view.Surface surface2 = null; // Your second surface
        
        if (playerManager.isUsingSurfaceControl()) {
            // Enhanced operations with SurfaceControl
            
            // Show video with optimal buffer sizing for 1920x1080 content
            playerManager.showDisplayWithDimensions(surface1, 1920, 1080);
            
            // Switch to different surface with different optimal size
            playerManager.showDisplayWithDimensions(surface2, 1280, 720);
            
            // Hide video efficiently (keeps playback running, just detaches video surface)
            playerManager.showDisplayWithDimensions(null, 0, 0);
            
            // Show video again on first surface
            playerManager.showDisplayWithDimensions(surface1, 1920, 1080);
            
            // Control visibility without changing surface (efficient show/hide)
            playerManager.setVideoVisibility(false); // Hide video
            playerManager.setVideoVisibility(true);  // Show video
            
        } else {
            // Standard operations for compatibility
            playerManager.showDisplayWithDimensions(surface1, 1920, 1080); // Dimensions ignored in standard mode
        }
        
        playerManager.release();
    }
    
    /**
     * Example demonstrating the benefits over standard surface switching
     */
    public void demonstrateAdvantagesOverStandardSwitching() {
        Exo2PlayerManager playerManager = new Exo2PlayerManager();
        
        if (playerManager.isUsingSurfaceControl()) {
            System.out.println("SurfaceControl Reparenting Benefits:");
            System.out.println("1. REPARENTING: Can detach video content (reparent to null)");
            System.out.println("2. BUFFER OPTIMIZATION: Optimal buffer sizing for each target surface");
            System.out.println("3. VISIBILITY CONTROL: Show/hide without stopping playback");
            System.out.println("4. ATOMIC OPERATIONS: Smooth transitions via SurfaceControl.Transaction");
            System.out.println("5. PERFORMANCE: Reduced overhead for surface operations");
            System.out.println();
            System.out.println("This addresses the surface switching issues mentioned in:");
            System.out.println("- androidx/media/issues/2733");
            System.out.println("- google/ExoPlayer/issues/5428");
        }
        
        playerManager.release();
    }
    
    /**
     * Shows testing capabilities
     */
    public void demonstrateTestingUtils() {
        Exo2PlayerManager playerManager = new Exo2PlayerManager();
        
        // Test SurfaceControl support
        SurfaceControlTestUtils.testSurfaceControlSupport(playerManager);
        
        // Get detailed information about device capabilities
        String info = SurfaceControlTestUtils.getSurfaceControlInfo();
        System.out.println("Device SurfaceControl info: " + info);
        
        playerManager.release();
    }
    
    /**
     * Key improvements in this implementation:
     * 
     * 1. TRUE REPARENTING: Uses SurfaceControl.reparent() for detached state management
     * 2. SURFACE-BASED API: Works with Surface objects (not requiring SurfaceView)
     * 3. BUFFER OPTIMIZATION: Proper buffer sizing with SurfaceControl.setBufferSize()
     * 4. VISIBILITY CONTROL: Efficient show/hide with SurfaceControl.setVisibility()
     * 5. ATOMIC OPERATIONS: All operations use SurfaceControl.Transaction
     * 6. AUTOMATIC FALLBACK: Graceful degradation for older devices
     * 7. ZERO BREAKING CHANGES: Existing code works without modifications
     */
}