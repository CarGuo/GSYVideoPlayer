package com.shuyu.gsyvideoplayer.render.view.listener;

import android.view.Surface;

/**
 * Created by guoshuyu on 2018/1/29.
 */

public interface IGSYSurfaceListener {
    void onSurfaceAvailable(Surface surface);

    void onSurfaceSizeChanged(Surface surface, int width, int height);

    boolean onSurfaceDestroyed(Surface surface);

    void onSurfaceUpdated(Surface surface);
}
