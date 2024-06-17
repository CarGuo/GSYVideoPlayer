
package com.shuyu.gsyvideoplayer.placeholder;

import static com.shuyu.gsyvideoplayer.placeholder.EGLSurfaceTexture.SECURE_MODE_NONE;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * A placeholder {@link Surface}.
 */
@RequiresApi(17)
public final class PlaceholderSurface extends Surface {

    private static final String TAG = "PlaceholderSurface";

    /**
     * Whether the surface is secure.
     */
    public final boolean secure;

    private static boolean secureModeInitialized;

    private final PlaceholderSurfaceThread thread;
    private boolean threadReleased;

    /**
     * Returns whether the device supports secure placeholder surfaces.
     *
     * @param context Any {@link Context}.
     * @return Whether the device supports secure placeholder surfaces.
     */
    public static synchronized boolean isSecureSupported(Context context) {
        return false;
    }

    /**
     * Returns a newly created placeholder surface. The surface must be released by calling {@link
     * #release} when it's no longer required.
     *
     * @param context Any {@link Context}.
     * @param secure  Whether a secure surface is required. Must only be requested if {@link
     *                #isSecureSupported(Context)} returns {@code true}.
     * @throws IllegalStateException If a secure surface is requested on a device for which {@link
     *                               #isSecureSupported(Context)} returns {@code false}.
     */
    public static PlaceholderSurface newInstanceV17(Context context, boolean secure) {
        PlaceholderSurfaceThread thread = new PlaceholderSurfaceThread();
        return thread.init(SECURE_MODE_NONE);
    }

    private PlaceholderSurface(PlaceholderSurfaceThread thread, SurfaceTexture surfaceTexture, boolean secure) {
        super(surfaceTexture);
        this.thread = thread;
        this.secure = secure;
    }

    @Override
    public void release() {
        super.release();
        // The Surface may be released multiple times (explicitly and by Surface.finalize()). The
        // implementation of super.release() has its own deduplication logic. Below we need to
        // deduplicate ourselves. Synchronization is required as we don't control the thread on which
        // Surface.finalize() is called.
        synchronized (thread) {
            if (!threadReleased) {
                thread.release();
                threadReleased = true;
            }
        }
    }


    private static class PlaceholderSurfaceThread extends HandlerThread implements Handler.Callback {

        private static final int MSG_INIT = 1;
        private static final int MSG_RELEASE = 2;

        private EGLSurfaceTexture eglSurfaceTexture;
        private Handler handler;
        @Nullable
        private Error initError;
        @Nullable
        private RuntimeException initException;
        @Nullable
        private PlaceholderSurface surface;

        public PlaceholderSurfaceThread() {
            super("ExoPlayer:PlaceholderSurface");
        }

        public PlaceholderSurface init(int secureMode) {
            start();
            handler = new Handler(getLooper(), /* callback= */ this);
            eglSurfaceTexture = new EGLSurfaceTexture(handler);
            boolean wasInterrupted = false;
            synchronized (this) {
                handler.obtainMessage(MSG_INIT, secureMode, 0).sendToTarget();
                while (surface == null && initException == null && initError == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        wasInterrupted = true;
                    }
                }
            }
            if (wasInterrupted) {
                // Restore the interrupted status.
                Thread.currentThread().interrupt();
            }
            if (initException != null) {
                throw initException;
            } else if (initError != null) {
                throw initError;
            } else {
                return surface;
            }
        }

        public void release() {
            handler.sendEmptyMessage(MSG_RELEASE);
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    try {
                        initInternal(/* secureMode= */ msg.arg1);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to initialize placeholder surface", e);
                        initException = e;
                    } catch (Error e) {
                        Log.e(TAG, "Failed to initialize placeholder surface", e);
                        initError = e;
                    } finally {
                        synchronized (this) {
                            notify();
                        }
                    }
                    return true;
                case MSG_RELEASE:
                    try {
                        releaseInternal();
                    } catch (Throwable e) {
                        Log.e(TAG, "Failed to release placeholder surface", e);
                    } finally {
                        quit();
                    }
                    return true;
                default:
                    return true;
            }
        }

        private void initInternal(int secureMode)  {
            eglSurfaceTexture.init(secureMode);
            this.surface = new PlaceholderSurface(this, eglSurfaceTexture.getSurfaceTexture(), false);
        }

        private void releaseInternal() {
            eglSurfaceTexture.release();
        }
    }
}
