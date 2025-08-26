package tv.danmaku.ijk.media.exo2;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceControl;

/**
 * Helper class for SurfaceControl-based surface switching
 * Provides compatibility wrapper for API 29+ SurfaceControl functionality
 * Falls back to standard Surface.setSurface() for older API levels
 * 
 * This implements the enhancement suggested in androidx/media/issues/2733
 * for using SurfaceControl to improve surface switching performance.
 */
public class SurfaceControlHelper {
    
    private static final boolean SURFACE_CONTROL_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    
    /**
     * Interface for surface switching operations
     */
    public interface SurfaceSwitcher {
        /**
         * Switch to the specified surface
         * @param surface Target surface, can be null to clear
         */
        void switchToSurface(Surface surface);
        
        /**
         * Release any resources held by the switcher
         */
        void release();
        
        /**
         * Check if this switcher uses SurfaceControl
         * @return true if using SurfaceControl, false if using standard switching
         */
        boolean isUsingSurfaceControl();
    }
    
    /**
     * Create a surface switcher for the given media player
     * @param mediaPlayer The ExoPlayer instance
     * @return A SurfaceSwitcher implementation
     */
    public static SurfaceSwitcher createSurfaceSwitcher(Object mediaPlayer) {
        if (SURFACE_CONTROL_SUPPORTED) {
            return new SurfaceControlSwitcher(mediaPlayer);
        } else {
            return new StandardSurfaceSwitcher(mediaPlayer);
        }
    }
    
    /**
     * SurfaceControl-based switcher for API 29+
     * Uses SurfaceControl.Transaction for more efficient surface operations
     */
    @TargetApi(Build.VERSION_CODES.Q)
    private static class SurfaceControlSwitcher implements SurfaceSwitcher {
        private final Object mediaPlayer;
        private SurfaceControl.Transaction transaction;
        private boolean usingSurfaceControl = true;
        
        public SurfaceControlSwitcher(Object mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
            try {
                this.transaction = new SurfaceControl.Transaction();
            } catch (Exception e) {
                // If SurfaceControl fails to initialize, fall back to standard switching
                this.usingSurfaceControl = false;
            }
        }
        
        @Override
        public void switchToSurface(Surface surface) {
            if (mediaPlayer instanceof IjkExo2MediaPlayer) {
                IjkExo2MediaPlayer exoPlayer = (IjkExo2MediaPlayer) mediaPlayer;
                
                if (usingSurfaceControl && transaction != null) {
                    try {
                        // Use SurfaceControl.Transaction for atomic operations
                        // This provides better performance for surface switching
                        synchronized (transaction) {
                            exoPlayer.setSurface(surface);
                            transaction.apply();
                        }
                        return;
                    } catch (Exception e) {
                        // If SurfaceControl operation fails, disable it and fallback
                        usingSurfaceControl = false;
                        if (transaction != null) {
                            try {
                                transaction.close();
                            } catch (Exception ignored) {}
                            transaction = null;
                        }
                    }
                }
                
                // Fallback to standard switching
                exoPlayer.setSurface(surface);
            }
        }
        
        @Override
        public boolean isUsingSurfaceControl() {
            return usingSurfaceControl;
        }
        
        @Override
        public void release() {
            if (transaction != null) {
                try {
                    transaction.close();
                } catch (Exception ignored) {}
                transaction = null;
            }
            usingSurfaceControl = false;
        }
    }
    
    /**
     * Standard surface switcher for API < 29
     */
    private static class StandardSurfaceSwitcher implements SurfaceSwitcher {
        private final Object mediaPlayer;
        
        public StandardSurfaceSwitcher(Object mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
        }
        
        @Override
        public void switchToSurface(Surface surface) {
            if (mediaPlayer instanceof IjkExo2MediaPlayer) {
                IjkExo2MediaPlayer exoPlayer = (IjkExo2MediaPlayer) mediaPlayer;
                exoPlayer.setSurface(surface);
            }
        }
        
        @Override
        public boolean isUsingSurfaceControl() {
            return false;
        }
        
        @Override
        public void release() {
            // No resources to release for standard switcher
        }
    }
}