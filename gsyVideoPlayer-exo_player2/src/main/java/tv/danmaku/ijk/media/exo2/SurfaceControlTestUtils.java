package tv.danmaku.ijk.media.exo2;

import android.util.Log;

/**
 * Utility class for testing and debugging SurfaceControl functionality
 */
public class SurfaceControlTestUtils {
    
    private static final String TAG = "SurfaceControlTest";
    
    /**
     * Test SurfaceControl functionality with an Exo2PlayerManager
     * @param playerManager The player manager to test
     */
    public static void testSurfaceControlSupport(Exo2PlayerManager playerManager) {
        if (playerManager == null) {
            Log.w(TAG, "PlayerManager is null, cannot test SurfaceControl support");
            return;
        }
        
        boolean usingSurfaceControl = playerManager.isUsingSurfaceControl();
        Log.i(TAG, "SurfaceControl support status: " + 
              (usingSurfaceControl ? "ENABLED (API 29+)" : "DISABLED (API < 29 or fallback)"));
        
        if (usingSurfaceControl) {
            Log.i(TAG, "✓ Using SurfaceControl.Transaction for improved surface switching performance");
        } else {
            Log.i(TAG, "ℹ Using standard Surface.setSurface() method");
        }
    }
    
    /**
     * Log surface switching operation
     * @param playerManager The player manager
     * @param surfaceType Description of the surface being switched to
     */
    public static void logSurfaceSwitch(Exo2PlayerManager playerManager, String surfaceType) {
        if (playerManager == null) return;
        
        boolean usingSurfaceControl = playerManager.isUsingSurfaceControl();
        Log.d(TAG, String.format("Surface switch to %s using %s", 
              surfaceType, 
              usingSurfaceControl ? "SurfaceControl" : "standard method"));
    }
    
    /**
     * Get information about SurfaceControl support
     * @return String describing SurfaceControl support status
     */
    public static String getSurfaceControlInfo() {
        return String.format("SurfaceControl available: %s (API level: %d)", 
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q ? "YES" : "NO",
                android.os.Build.VERSION.SDK_INT);
    }
}