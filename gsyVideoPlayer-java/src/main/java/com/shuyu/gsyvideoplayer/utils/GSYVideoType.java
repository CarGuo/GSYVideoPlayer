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

    //全屏裁减显示，为了显示正常 CoverImageView 建议使用FrameLayout作为父布局
    public final static int SCREEN_TYPE_FULL = 4;

    //全屏拉伸显示，使用这个属性时，surface_container建议使用FrameLayout
    public final static int SCREEN_MATCH_FULL = -4;

    //默认播放内核IJK
    public final static int IJKPLAYER = 0;
    /**
     * IJKEXOPLAYER 和 IJKEXOPLAYER2 是互斥的
     * IJKEXOPLAYER2 must be compile com.shuyu:gsyVideoPlayer-exo2:$gsyVideoVersion
     */
    //EXO 2 播放内核
    public final static int IJKEXOPLAYER2 = 2;
    //EXO 1 播放内核，弃用，现在使用的是IJKEXOPLAYER2
    @Deprecated
    public final static int IJKEXOPLAYER = IJKEXOPLAYER2;
    //系统播放器
    public final static int SYSTEMPLAYER = 4;


    /**
     * GLSurfaceView 主要用于OpenGL渲染的
     */
    public final static int GLSURFACE = 2;

    /**
     * SurfaceView，与动画全屏的效果不是很兼容，但是与IJKEXOPLAYER2模式兼容性比较好
     */
    public final static int SUFRACE = 1;

    /**
     * TextureView，与IJKPLAYER兼容号，但是与IJKEXOPLAYER2在切换时兼容比较差
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

}
