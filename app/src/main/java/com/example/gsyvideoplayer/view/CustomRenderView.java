package com.example.gsyvideoplayer.view;

import android.content.Context;
import android.view.ViewGroup;

import com.shuyu.gsyvideoplayer.render.GSYRenderView;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;

/**
 * 自定义代理渲染层
 * Created by guoshuyu on 2018/1/30.
 */

public class CustomRenderView extends GSYRenderView {

    @Override
    public void addView(Context context, ViewGroup textureViewContainer, int rotate, IGSYSurfaceListener gsySurfaceListener, GSYVideoGLView.ShaderInterface effect, float[] transform, GSYVideoGLViewBaseRender customRender, int mode) {
        mShowView = CustomTextureSurface.addSurfaceView(context, textureViewContainer, rotate, gsySurfaceListener);
    }
}
