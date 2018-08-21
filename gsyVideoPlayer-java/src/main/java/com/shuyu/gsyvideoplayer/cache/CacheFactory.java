package com.shuyu.gsyvideoplayer.cache;

/**
 * 缓存到本地服务工厂
 * Created by guoshuyu on 2018/5/21.
 */
public class CacheFactory {

    private static ICacheManager sICacheManager;

    public static void setCacheManager(ICacheManager cacheManager) {
        sICacheManager = cacheManager;
    }

    public static ICacheManager getCacheManager() {
        if (sICacheManager == null) {
            sICacheManager = new ProxyCacheManager();
        }
        return sICacheManager;
    }
}
