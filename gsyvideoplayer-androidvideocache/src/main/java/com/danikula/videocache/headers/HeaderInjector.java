package com.danikula.videocache.headers;

import java.util.Map;

/**
 * Allows to add custom headers to server's requests.
 *
 * @author Lucas Nelaupe (https://github.com/lucas34).
 */
public interface HeaderInjector {

    /**
     * Adds headers to server's requests for corresponding url.
     *
     * @param url an url headers will be added for
     * @return a map with headers, where keys are header's names, and values are header's values. {@code null} is not acceptable!
     */
    Map<String, String> addHeaders(String url);

}
