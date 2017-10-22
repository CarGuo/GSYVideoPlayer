package com.danikula.videocache;

/**
 * Cache for proxy.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public interface Cache {

    long available() throws ProxyCacheException;

    int read(byte[] buffer, long offset, int length) throws ProxyCacheException;

    void append(byte[] data, int length) throws ProxyCacheException;

    void close() throws ProxyCacheException;

    void complete() throws ProxyCacheException;

    boolean isCompleted();
}
