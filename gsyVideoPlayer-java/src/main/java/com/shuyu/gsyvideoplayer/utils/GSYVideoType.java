package com.shuyu.gsyvideoplayer.utils;

/**
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


    public final static int IJKPLAYER = 0;

    public final static int IJKEXOPLAYER = 1;


    //gl
    public final static int GLSURFACE = 2;

    //surface
    public final static int SUFRACE = 1;

    //texture
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
