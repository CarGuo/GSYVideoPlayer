package com.shuyu.gsyvideoplayer.utils;

import java.io.BufferedInputStream;
import java.io.IOException;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * @author : Gouzhong
 * 视频元数据输入流数据源
 */
public class StreamDataSourceProvider implements IMediaDataSource {

    private final BufferedInputStream bufferedInputStream;

    public StreamDataSourceProvider(BufferedInputStream bufferedInputStream) {
        this.bufferedInputStream = bufferedInputStream;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (size == 0) {
            // size=0 means there is a seek request.
            // You can handle it now, or ignore it, and handle new position at next readAt() call.
            return 0;
        }
        return bufferedInputStream.read(buffer, offset, size);
    }

    @Override
    public long getSize() throws IOException {
        return bufferedInputStream.available();
    }

    @Override
    public void close() throws IOException {
        bufferedInputStream.close();
    }
}
