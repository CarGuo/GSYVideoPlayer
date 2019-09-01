package com.example.gsyvideoplayer.mediacodec;

import android.content.Context;
import android.view.ViewGroup;

import com.example.gsyvideoplayer.view.CustomTextureSurface;
import com.shuyu.gsyvideoplayer.render.GSYRenderView;
import com.shuyu.gsyvideoplayer.render.glrender.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.render.view.listener.IGSYSurfaceListener;
import com.shuyu.gsyvideoplayer.utils.MeasureHelper;

/**
 * 自定义代理渲染层
 * Created by guoshuyu on 2018/1/30.
 */

public class MediaCodecRenderView extends GSYRenderView {

    @Override
    public void addView(Context context, ViewGroup textureViewContainer, int rotate, IGSYSurfaceListener gsySurfaceListener, MeasureHelper.MeasureFormVideoParamsListener videoParamsListener, GSYVideoGLView.ShaderInterface effect, float[] transform, GSYVideoGLViewBaseRender customRender, int mode) {
        mShowView = MediaCodecTextureView.addTextureView(context, textureViewContainer, rotate, gsySurfaceListener, videoParamsListener);
    }
}
