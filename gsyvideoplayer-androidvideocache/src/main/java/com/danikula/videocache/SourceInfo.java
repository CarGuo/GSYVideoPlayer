package com.danikula.videocache;

/**
 * Stores source's info.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class SourceInfo {

    public final String url;
    public final long length;
    public final String mime;

    public SourceInfo(String url, long length, String mime) {
        this.url = url;
        this.length = length;
        this.mime = mime;
    }

    @Override
    public String toString() {
        return "SourceInfo{" +
                "url='" + url + '\'' +
                ", length=" + length +
                ", mime='" + mime + '\'' +
                '}';
    }
}
