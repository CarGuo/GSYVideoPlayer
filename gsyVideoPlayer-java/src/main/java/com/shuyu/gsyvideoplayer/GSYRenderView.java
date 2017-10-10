package com.shuyu.gsyvideoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.listener.GSYVideoGifSaveListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.render.GSYVideoGLViewBaseRender;
import com.shuyu.gsyvideoplayer.utils.AnimatedGifEncoder;
import com.shuyu.gsyvideoplayer.utils.FileUtils;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * render绘制中间控件
 * Created by guoshuyu on 2017/8/26.
 */

public class GSYRenderView {

    private View mShowView;

    private GSYVideoShotListener mGSYVideoShotListener;

    private boolean mShotHigh;

    public void setTransform(Matrix transform) {
        if (mShowView instanceof TextureView) {
            ((TextureView) mShowView).setTransform(transform);
        }
    }

    public void requestLayout() {
        if (mShowView != null)
            mShowView.requestLayout();
    }

    public float getRotation() {
        return mShowView.getRotation();
    }

    public void setRotation(float rotation) {
        if (mShowView != null)
            mShowView.setRotation(rotation);
    }

    public void invalidate() {
        mShowView.invalidate();
    }

    public int getWidth() {
        return mShowView.getWidth();
    }

    public int getHeight() {
        return mShowView.getHeight();
    }

    public View getShowView() {
        return mShowView;
    }

    /**
     * 暂停时初始化位图
     */
    public Bitmap initCover() {
        if (mShowView != null && mShowView instanceof GSYTextureView) {
            GSYTextureView textureView = (GSYTextureView) mShowView;
            Bitmap bitmap = Bitmap.createBitmap(
                    textureView.getSizeW(), textureView.getSizeH(), Bitmap.Config.RGB_565);
            return textureView.getBitmap(bitmap);
        }
        return null;
    }

    /**
     * 暂停时初始化位图
     */
    public Bitmap initCoverHigh() {
        if (mShowView != null && mShowView instanceof GSYTextureView) {
            GSYTextureView textureView = (GSYTextureView) mShowView;
            Bitmap bitmap = Bitmap.createBitmap(
                    textureView.getSizeW(), textureView.getSizeH(), Bitmap.Config.ARGB_8888);
            return textureView.getBitmap(bitmap);
        }
        return null;
    }

    /**
     * 获取当前画面
     */
    public void setCurrentFrameBitmapListener(GSYVideoShotListener gsyVideoShotListener) {
        setCurrentFrameBitmapListener(gsyVideoShotListener, false);
    }

    /**
     * 获取获取截图监听
     */
    public void setCurrentFrameBitmapListener(GSYVideoShotListener gsyVideoShotListener, boolean high) {
        mGSYVideoShotListener = gsyVideoShotListener;
        mShotHigh = high;
    }

    /**
     * 获取截图
     */
    public void taskShotPic() {
        if (mGSYVideoShotListener != null) {
            if (mShowView instanceof GSYVideoGLView) {
                GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
                gsyVideoGLView.setGSYVideoShotListener(mGSYVideoShotListener, mShotHigh);
                gsyVideoGLView.takeShotPic();
            } else if (mShowView instanceof GSYTextureView) {
                if (mShotHigh) {
                    mGSYVideoShotListener.getBitmap(initCoverHigh());
                } else {
                    mGSYVideoShotListener.getBitmap(initCover());
                }
            }
        }
    }


    /**
     * 生成gif图
     *
     * @param file                    保存的文件路径，请确保文件夹目录已经创建
     * @param pics                    需要转化的bitmap本地路径集合
     * @param delay                   每一帧之间的延时
     * @param inSampleSize            采样率，越大图片越小，越大图片越模糊，需要处理的时长越短
     * @param scaleSize               缩减尺寸比例，对生成的截图进行缩减，越大图片越模糊，需要处理的时长越短
     * @param gsyVideoGifSaveListener 结果回调
     */
    public void createGif(File file, List<String> pics, int delay, int inSampleSize, int scaleSize,
                          final GSYVideoGifSaveListener gsyVideoGifSaveListener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
        localAnimatedGifEncoder.start(baos);//start
        localAnimatedGifEncoder.setRepeat(0);//设置生成gif的开始播放时间。0为立即开始播放
        localAnimatedGifEncoder.setDelay(delay);
        for (int i = 0; i < pics.size(); i++) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = true; // 先获取原大小
            BitmapFactory.decodeFile(pics.get(i), options);
            double w = (double) options.outWidth / scaleSize;
            double h = (double) options.outHeight / scaleSize;
            options.inJustDecodeBounds = false; // 获取新的大小
            Bitmap bitmap = BitmapFactory.decodeFile(pics.get(i), options);
            Bitmap pic = ThumbnailUtils.extractThumbnail(bitmap, (int) w, (int) h);
            localAnimatedGifEncoder.addFrame(pic);
            bitmap.recycle();
            pic.recycle();
            gsyVideoGifSaveListener.process(i + 1, pics.size());
        }
        localAnimatedGifEncoder.finish();//finish
        try {
            FileOutputStream fos = new FileOutputStream(file.getPath());
            baos.writeTo(fos);
            baos.flush();
            fos.flush();
            baos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            gsyVideoGifSaveListener.result(false, file);
            return;
        }
        gsyVideoGifSaveListener.result(true, file);
    }

    /**
     * 保存截图
     */
    public void saveFrame(final File file, GSYVideoShotSaveListener gsyVideoShotSaveListener) {
        saveFrame(file, false, gsyVideoShotSaveListener);
    }

    /**
     * 保存截图
     */
    public void saveFrame(final File file, final boolean high, final GSYVideoShotSaveListener gsyVideoShotSaveListener) {
        GSYVideoShotListener gsyVideoShotListener = new GSYVideoShotListener() {
            @Override
            public void getBitmap(Bitmap bitmap) {
                if (bitmap == null) {
                    gsyVideoShotSaveListener.result(false, file);
                } else {
                    FileUtils.saveBitmap(bitmap, file);
                    gsyVideoShotSaveListener.result(true, file);
                }
            }
        };
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.setGSYVideoShotListener(gsyVideoShotListener, high);
            gsyVideoGLView.takeShotPic();
        } else if (mShowView instanceof GSYTextureView) {
            if (high) {
                gsyVideoShotListener.getBitmap(initCoverHigh());
            } else {
                gsyVideoShotListener.getBitmap(initCover());
            }
        }
    }

    public ViewGroup.LayoutParams getLayoutParams() {
        return mShowView.getLayoutParams();
    }

    public void setLayoutParams(ViewGroup.LayoutParams layoutParams) {
        if (mShowView != null)
            mShowView.setLayoutParams(layoutParams);
    }

    public void onResume() {
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.requestLayout();
            gsyVideoGLView.onResume();
        }
    }

    public void onPause() {
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.requestLayout();
            gsyVideoGLView.onPause();
        }
    }

    public void releaseAll() {
        if (mShowView instanceof GSYVideoGLView) {
            GSYVideoGLView gsyVideoGLView = (GSYVideoGLView) mShowView;
            gsyVideoGLView.requestLayout();
            gsyVideoGLView.releaseAll();
        }
    }

    /**
     * 添加播放的view
     */
    public void addTextureView(Context context, ViewGroup textureViewContainer, int rotate, TextureView.SurfaceTextureListener listener) {

        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        GSYTextureView gsyTextureView = new GSYTextureView(context);
        gsyTextureView.setSurfaceTextureListener(listener);
        gsyTextureView.setRotation(rotate);

        mShowView = gsyTextureView;

        int params = getTextureParams();

        if (textureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureViewContainer.addView(gsyTextureView, layoutParams);
        } else if (textureViewContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            textureViewContainer.addView(gsyTextureView, layoutParams);
        }
    }

    /**
     * 添加播放的view
     */
    public void addSurfaceView(Context context, ViewGroup textureViewContainer, int rotate, SurfaceHolder.Callback2 callback2) {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        GSYSurfaceView showSurfaceView = new GSYSurfaceView(context);
        showSurfaceView.getHolder().addCallback(callback2);
        showSurfaceView.setRotation(rotate);

        mShowView = showSurfaceView;

        int params = getTextureParams();

        if (textureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureViewContainer.addView(showSurfaceView, layoutParams);
        } else if (textureViewContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            textureViewContainer.addView(showSurfaceView, layoutParams);
        }
    }

    /**
     * 添加播放的view
     */
    public void addGLView(Context context, ViewGroup textureViewContainer, int rotate,
                          GSYVideoGLView.onGSYSurfaceListener gsySurfaceListener,
                          GSYVideoGLView.ShaderInterface effect, float[] transform,
                          GSYVideoGLViewBaseRender customRender) {
        if (textureViewContainer.getChildCount() > 0) {
            textureViewContainer.removeAllViews();
        }
        GSYVideoGLView gsyVideoGLView = new GSYVideoGLView(context);
        if (customRender != null) {
            gsyVideoGLView.setCustomRenderer(customRender);
        }
        gsyVideoGLView.setEffect(effect);
        gsyVideoGLView.setGSYSurfaceListener(gsySurfaceListener);
        gsyVideoGLView.setRotation(rotate);
        gsyVideoGLView.initRender();
        mShowView = gsyVideoGLView;

        if (transform != null && transform.length == 16) {
            gsyVideoGLView.setMVPMatrix(transform);
        }

        int params = getTextureParams();

        if (textureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureViewContainer.addView(gsyVideoGLView, layoutParams);
        } else if (textureViewContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            textureViewContainer.addView(gsyVideoGLView, layoutParams);
        }
    }

    /**
     * 获取布局参数
     *
     * @return
     */
    protected int getTextureParams() {
        boolean typeChanged = (GSYVideoType.getShowType() != GSYVideoType.SCREEN_TYPE_DEFAULT);
        return (typeChanged) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
    }

}
