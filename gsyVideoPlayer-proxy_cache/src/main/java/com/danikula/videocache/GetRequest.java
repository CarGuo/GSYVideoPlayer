package com.danikula.videocache;

import static com.danikula.videocache.Preconditions.checkNotNull;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model for Http GET request.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
class GetRequest {

    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-");
    private static final Pattern URL_PATTERN = Pattern.compile("GET /(.*) HTTP");

    public final String uri;
    public final long rangeOffset;
    public final boolean partial;

    public GetRequest(String request) {
        checkNotNull(request);
        long offset = findRangeOffset(request);
        this.rangeOffset = Math.max(0, offset);
        this.partial = offset >= 0;
        this.uri = findUri(request);
    }

    public static GetRequest read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder stringRequest = new StringBuilder();
        String line;
        while (!TextUtils.isEmpty(line = reader.readLine())) { // until new line (headers ending)
            stringRequest.append(line).append('\n');
        }
        return new GetRequest(stringRequest.toString());
    }

    private long findRangeOffset(String request) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(1);
            return Long.parseLong(rangeValue);
        }
        return -1;
    }

    private String findUri(String request) {
        Matcher matcher = URL_PATTERN.matcher(request);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid request `" + request + "`: url not found!");
    }

    @NonNull
    @Override
    public String toString() {
        return "GetRequest{" +
                "rangeOffset=" + rangeOffset +
                ", partial=" + partial +
                ", uri='" + uri + '\'' +
                '}';
    }
}
