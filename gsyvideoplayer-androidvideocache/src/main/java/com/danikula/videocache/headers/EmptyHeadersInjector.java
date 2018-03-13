package com.danikula.videocache.headers;

import java.util.HashMap;
import java.util.Map;

/**
 * Empty {@link HeaderInjector} implementation.
 *
 * @author Lucas Nelaupe (https://github.com/lucas34).
 */
public class EmptyHeadersInjector implements HeaderInjector {

    @Override
    public Map<String, String> addHeaders(String url) {
        return new HashMap<>();
    }

}
