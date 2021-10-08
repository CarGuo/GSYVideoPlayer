package com.danikula.videocache;

import shuyu.com.androidvideocache.BuildConfig;

/**
 * Indicates any error in work of {@link ProxyCache}.
 *
 * @author Alexey Danilov
 */
public class ProxyCacheException extends Exception {

    public ProxyCacheException(String message) {
        super(message + BuildConfig.LIBRARY_VERSION);
    }

    public ProxyCacheException(String message, Throwable cause) {
        super(message + BuildConfig.LIBRARY_VERSION, cause);
    }

    public ProxyCacheException(Throwable cause) {
        super("No explanation error" + BuildConfig.LIBRARY_VERSION, cause);
    }
}
