package com.shuyu.gsyvideoplayer.cache;

import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

/**
 * 缓存到本地服务工厂
 * Created by guoshuyu on 2018/5/21.
 */
public class CacheFactory {

    /**
     * 不使用cache服务
     */
    public final static int CACHE_NULL = -1;

    /**
     * 默认缓存服务，EXO使用Source缓存，其他使用proxy缓存
     */
    public final static int EXO_DEFAULT = 0;

    /**
     * ExoPlayer使用Proxy服务缓存，只对EXO播放内核生效
     */
    public final static int EXO_CACHE_PROXY = 1;

    private static int sCacheMode = EXO_DEFAULT;

    public static ICacheManager getCacheManager(int videoType) {
        if (sCacheMode == CACHE_NULL) {
            return null;
        }
        switch (videoType) {
            case GSYVideoType.IJKEXOPLAYER2:
                if (sCacheMode == EXO_DEFAULT) {
                    //TODO 针对exoPlayer的cache处理
                    return null;
                }
            case GSYVideoType.SYSTEMPLAYER:
            case GSYVideoType.IJKPLAYER:
            default:
                return new ProxyCacheManager();
        }
    }

    /**
     * 设置缓存模式
     *
     * @param mode CACHE_NULL、EXO_CACHE_PROXY、EXO_DEFAULT
     */
    public static void setCacheMode(int mode) {
        sCacheMode = mode;
    }

}
