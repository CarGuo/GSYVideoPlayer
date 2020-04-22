package com.shuyu.gsyvideoplayer.utils;

/**
 * video的一些默认配置
 * Created by shuyu on 2016/12/7.
 */

public class GSYVideoType {

    //默认显示比例
    public final static int SCREEN_TYPE_DEFAULT = 0;

    //16:9
    public final static int SCREEN_TYPE_16_9 = 1;

    //4:3
    public final static int SCREEN_TYPE_4_3 = 2;

    //18:9
    public final static int SCREEN_TYPE_18_9 = 6;

    //全屏裁减显示，为了显示正常 surface_container 和 CoverImageView 建议使用 FrameLayout 作为父布局
    public final static int SCREEN_TYPE_FULL = 4;

    //全屏拉伸显示，使用这个属性时，surface_container 建议使用 FrameLayout
    public final static int SCREEN_MATCH_FULL = -4;

    //自定义比例，需要设置 sScreenScaleRatio
    public final static int SCREEN_TYPE_CUSTOM = -5;

    /**
     * 自定义的显示比例
     */
    private static float sScreenScaleRatio = 0;

    /**
     * GLSurfaceView 主要用于OpenGL渲染的
     */
    public final static int GLSURFACE = 2;

    /**
     * SurfaceView，与动画全屏的效果不是很兼容
     */
    public final static int SUFRACE = 1;

    /**
     * TextureView,默认
     */
    public final static int TEXTURE = 0;


    //显示比例类型
    private static int TYPE = SCREEN_TYPE_DEFAULT;

    //硬解码标志
    private static boolean MEDIA_CODEC_FLAG = false;

    //渲染类型
    private static int sRenderType = TEXTURE;

    //是否使用硬解码优化
    private static boolean sTextureMediaPlay = false;


    /**
     * 使能硬解码，播放前设置
     */
    public static void enableMediaCodec() {
        MEDIA_CODEC_FLAG = true;
    }

    /**
     * 关闭硬解码，播放前设置
     */
    public static void disableMediaCodec() {
        MEDIA_CODEC_FLAG = false;
    }

    /**
     * 使能硬解码渲染优化
     */
    public static void enableMediaCodecTexture() {
        sTextureMediaPlay = true;
    }

    /**
     * 关闭硬解码渲染优化
     */
    public static void disableMediaCodecTexture() {
        sTextureMediaPlay = false;
    }

    /**
     * 是否开启硬解码
     */
    public static boolean isMediaCodec() {
        return MEDIA_CODEC_FLAG;
    }

    /**
     * 是否开启硬解码渲染优化
     */
    public static boolean isMediaCodecTexture() {
        return sTextureMediaPlay;
    }

    public static int getShowType() {
        return TYPE;
    }

    /**
     * 设置显示比例,注意，这是全局生效的
     */
    public static void setShowType(int type) {
        TYPE = type;
    }


    public static int getRenderType() {
        return sRenderType;
    }

    /**
     * 渲染控件
     *
     * @param renderType
     */
    public static void setRenderType(int renderType) {
        sRenderType = renderType;
    }

    public static float getScreenScaleRatio() {
        return sScreenScaleRatio;
    }

    /***
     * SCREEN_TYPE_CUSTOM 下自定义显示比例
     * @param screenScaleRatio  高宽比，如 16：9
     */
    public static void setScreenScaleRatio(float screenScaleRatio) {
        GSYVideoType.sScreenScaleRatio = screenScaleRatio;
    }
}
