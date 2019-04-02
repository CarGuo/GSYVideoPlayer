//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tv.danmaku.ijk.media.exo2.source;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSourceException;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Predicate;
import com.google.android.exoplayer2.util.Util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.google.android.exoplayer2.upstream.DataSpec.FLAG_ALLOW_GZIP;
import static com.google.android.exoplayer2.upstream.HttpDataSource.HttpDataSourceException.TYPE_OPEN;

public class GSYExoHttpDataSource extends BaseDataSource implements HttpDataSource {
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 8000;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 8000;
    private static final String TAG = "GSYExoHttpDataSource";
    private static final int MAX_REDIRECTS = 20;
    private static final int HTTP_STATUS_TEMPORARY_REDIRECT = 307;
    private static final int HTTP_STATUS_PERMANENT_REDIRECT = 308;
    private static final long MAX_BYTES_TO_DRAIN = 2048L;
    private static final Pattern CONTENT_RANGE_HEADER = Pattern.compile("^bytes (\\d+)-(\\d+)/(\\d+)$");
    private static final AtomicReference<byte[]> skipBufferReference = new AtomicReference();
    private final boolean allowCrossProtocolRedirects;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final String userAgent;
    @Nullable
    private final Predicate<String> contentTypePredicate;
    @Nullable
    private final RequestProperties defaultRequestProperties;
    private final RequestProperties requestProperties;
    @Nullable
    private DataSpec dataSpec;
    @Nullable
    private HttpURLConnection connection;
    @Nullable
    private InputStream inputStream;
    private boolean opened;
    private long bytesToSkip;
    private long bytesToRead;
    private long bytesSkipped;
    private long bytesRead;

    public GSYExoHttpDataSource(String userAgent, @Nullable Predicate<String> contentTypePredicate) {
        this(userAgent, contentTypePredicate, 8000, 8000);
    }

    public GSYExoHttpDataSource(String userAgent, @Nullable Predicate<String> contentTypePredicate, int connectTimeoutMillis, int readTimeoutMillis) {
        this(userAgent, contentTypePredicate, connectTimeoutMillis, readTimeoutMillis, false, (RequestProperties) null);
    }

    public GSYExoHttpDataSource(String userAgent, @Nullable Predicate<String> contentTypePredicate, int connectTimeoutMillis, int readTimeoutMillis, boolean allowCrossProtocolRedirects, @Nullable RequestProperties defaultRequestProperties) {
        super(true);
        this.userAgent = Assertions.checkNotEmpty(userAgent);
        this.contentTypePredicate = contentTypePredicate;
        this.requestProperties = new RequestProperties();
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
        this.defaultRequestProperties = defaultRequestProperties;
    }

    /**
     @deprecated
     */
    @Deprecated
    public GSYExoHttpDataSource(String userAgent, @Nullable Predicate<String> contentTypePredicate, @Nullable TransferListener listener) {
        this(userAgent, contentTypePredicate, listener, 8000, 8000);
    }

    /**
     @deprecated
     */
    @Deprecated
    public GSYExoHttpDataSource(String userAgent, @Nullable Predicate<String> contentTypePredicate, @Nullable TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis) {
        this(userAgent, contentTypePredicate, listener, connectTimeoutMillis, readTimeoutMillis, false, (RequestProperties) null);
    }

    /**
     @deprecated
     */
    @Deprecated
    public GSYExoHttpDataSource(String userAgent, @Nullable Predicate<String> contentTypePredicate, @Nullable TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis, boolean allowCrossProtocolRedirects, @Nullable RequestProperties defaultRequestProperties) {
        this(userAgent, contentTypePredicate, connectTimeoutMillis, readTimeoutMillis, allowCrossProtocolRedirects, defaultRequestProperties);
        if (listener != null) {
            this.addTransferListener(listener);
        }

    }

    @Nullable
    public Uri getUri() {
        return this.connection == null ? null : Uri.parse(this.connection.getURL().toString());
    }

    public Map<String, List<String>> getResponseHeaders() {
        return this.connection == null ? new HashMap<String, List<String>>() : this.connection.getHeaderFields();
    }

    public void setRequestProperty(String name, String value) {
        Assertions.checkNotNull(name);
        Assertions.checkNotNull(value);
        this.requestProperties.set(name, value);
    }

    public void clearRequestProperty(String name) {
        Assertions.checkNotNull(name);
        this.requestProperties.remove(name);
    }

    public void clearAllRequestProperties() {
        this.requestProperties.clear();
    }

    public long open(DataSpec dataSpec) throws HttpDataSourceException {
        this.dataSpec = dataSpec;
        this.bytesRead = 0L;
        this.bytesSkipped = 0L;
        this.transferInitializing(dataSpec);

        try {
            this.connection = this.makeConnection(dataSpec);
        } catch (IOException var9) {
            throw new HttpDataSourceException("Unable to connect to " + dataSpec.uri.toString(), var9, dataSpec, TYPE_OPEN);
        }

        int responseCode;
        String responseMessage;
        try {
            responseCode = this.connection.getResponseCode();
            responseMessage = this.connection.getResponseMessage();
        } catch (IOException var8) {
            this.closeConnectionQuietly();
            throw new HttpDataSourceException("Unable to connect to " + dataSpec.uri.toString(), var8, dataSpec, TYPE_OPEN);
        }

        if (responseCode >= 200 && responseCode <= 299) {
            String contentType = this.connection.getContentType();
            if (this.contentTypePredicate != null && !this.contentTypePredicate.evaluate(contentType)) {
                this.closeConnectionQuietly();
                throw new InvalidContentTypeException(contentType, dataSpec);
            } else {
                this.bytesToSkip = responseCode == 200 && dataSpec.position != 0L ? dataSpec.position : 0L;
                if (!dataSpec.isFlagSet(FLAG_ALLOW_GZIP)) {
                    if (dataSpec.length != -1L) {
                        this.bytesToRead = dataSpec.length;
                    } else {
                        long contentLength = getContentLength(this.connection);
                        this.bytesToRead = contentLength != -1L ? contentLength - this.bytesToSkip : -1L;
                    }
                } else {
                    this.bytesToRead = dataSpec.length;
                }

                try {
                    this.inputStream = this.connection.getInputStream();
                } catch (IOException var7) {
                    this.closeConnectionQuietly();
                    throw new HttpDataSourceException(var7, dataSpec, TYPE_OPEN);
                }

                this.opened = true;
                this.transferStarted(dataSpec);
                return this.bytesToRead;
            }
        } else {
            Map<String, List<String>> headers = this.connection.getHeaderFields();
            this.closeConnectionQuietly();
            InvalidResponseCodeException exception = new InvalidResponseCodeException(responseCode, responseMessage, headers, dataSpec);
            if (responseCode == 416) {
                exception.initCause(new DataSourceException(0));
            }

            throw exception;
        }
    }

    public int read(byte[] buffer, int offset, int readLength) throws HttpDataSourceException {
        try {
            this.skipInternal();
            return this.readInternal(buffer, offset, readLength);
        } catch (IOException var5) {
            throw new HttpDataSourceException(var5, this.dataSpec, HttpDataSourceException.TYPE_CLOSE);
        }
    }

    public void close() throws HttpDataSourceException {
        try {
            if (this.inputStream != null) {
                maybeTerminateInputStream(this.connection, this.bytesRemaining());

                try {
                    this.inputStream.close();
                } catch (IOException var5) {
                    throw new HttpDataSourceException(var5, this.dataSpec, HttpDataSourceException.TYPE_CLOSE);
                }
            }
        } finally {
            this.inputStream = null;
            this.closeConnectionQuietly();
            if (this.opened) {
                this.opened = false;
                this.transferEnded();
            }

        }

    }

    @Nullable
    protected final HttpURLConnection getConnection() {
        return this.connection;
    }

    protected final long bytesSkipped() {
        return this.bytesSkipped;
    }

    protected final long bytesRead() {
        return this.bytesRead;
    }

    protected final long bytesRemaining() {
        return this.bytesToRead == -1L ? this.bytesToRead : this.bytesToRead - this.bytesRead;
    }

    private HttpURLConnection makeConnection(DataSpec dataSpec) throws IOException {
        URL url = new URL(dataSpec.uri.toString());
        int httpMethod = dataSpec.httpMethod;
        byte[] httpBody = dataSpec.httpBody;
        long position = dataSpec.position;
        long length = dataSpec.length;
        boolean allowGzip = dataSpec.isFlagSet(FLAG_ALLOW_GZIP);
        if (!this.allowCrossProtocolRedirects) {
            return this.makeConnection(url, httpMethod, httpBody, position, length, allowGzip, true);
        } else {
            int redirectCount = 0;

            while (true) {
                while (redirectCount++ <= 20) {
                    HttpURLConnection connection = this.makeConnection(url, httpMethod, httpBody, position, length, allowGzip, false);
                    int responseCode = connection.getResponseCode();
                    String location = connection.getHeaderField("Location");
                    if (httpMethod != 1 && httpMethod != 3 || responseCode != 300 && responseCode != 301 && responseCode != 302 && responseCode != 303 && responseCode != 307 && responseCode != 308) {
                        if (httpMethod != 2 || responseCode != 300 && responseCode != 301 && responseCode != 302 && responseCode != 303) {
                            return connection;
                        }

                        connection.disconnect();
                        httpMethod = 1;
                        httpBody = null;
                        url = handleRedirect(url, location);
                    } else {
                        connection.disconnect();
                        url = handleRedirect(url, location);
                    }
                }

                throw new NoRouteToHostException("Too many redirects: " + redirectCount);
            }
        }
    }

    private HttpURLConnection makeConnection(URL url, int httpMethod, byte[] httpBody, long position, long length, boolean allowGzip, boolean followRedirects) throws IOException {
        HttpURLConnection connection;
        if (url.getProtocol().endsWith("https")) {
            /**去除证书限制**/
            connection = (HttpsURLConnection) url.openConnection();
            ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };
            // Install the all-trusting trust manager
            final SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            /**去除证书限制**/
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        connection.setConnectTimeout(this.connectTimeoutMillis);
        connection.setReadTimeout(this.readTimeoutMillis);
        Iterator var11;
        Entry property;
        if (this.defaultRequestProperties != null) {
            var11 = this.defaultRequestProperties.getSnapshot().entrySet().iterator();

            while (var11.hasNext()) {
                property = (Entry) var11.next();
                connection.setRequestProperty((String) property.getKey(), (String) property.getValue());
            }
        }

        var11 = this.requestProperties.getSnapshot().entrySet().iterator();

        while (var11.hasNext()) {
            property = (Entry) var11.next();
            connection.setRequestProperty((String) property.getKey(), (String) property.getValue());
        }

        if (position != 0L || length != -1L) {
            String rangeRequest = "bytes=" + position + "-";
            if (length != -1L) {
                rangeRequest = rangeRequest + (position + length - 1L);
            }

            connection.setRequestProperty("Range", rangeRequest);
        }

        connection.setRequestProperty("User-Agent", this.userAgent);
        if (!allowGzip) {
            connection.setRequestProperty("Accept-Encoding", "identity");
        }

        connection.setInstanceFollowRedirects(followRedirects);
        connection.setDoOutput(httpBody != null);
        connection.setRequestMethod(DataSpec.getStringForHttpMethod(httpMethod));
        if (httpBody != null) {
            connection.setFixedLengthStreamingMode(httpBody.length);
            connection.connect();
            OutputStream os = connection.getOutputStream();
            os.write(httpBody);
            os.close();
        } else {
            connection.connect();
        }

        return connection;
    }

    private static URL handleRedirect(URL originalUrl, String location) throws IOException {
        if (location == null) {
            throw new ProtocolException("Null location redirect");
        } else {
            URL url = new URL(originalUrl, location);
            String protocol = url.getProtocol();
            if (!"https".equals(protocol) && !"http".equals(protocol)) {
                throw new ProtocolException("Unsupported protocol redirect: " + protocol);
            } else {
                return url;
            }
        }
    }

    private static long getContentLength(HttpURLConnection connection) {
        long contentLength = -1L;
        String contentLengthHeader = connection.getHeaderField("Content-Length");
        if (!TextUtils.isEmpty(contentLengthHeader)) {
            try {
                contentLength = Long.parseLong(contentLengthHeader);
            } catch (NumberFormatException var9) {
                Log.e("GSYExoHttpDataSource", "Unexpected Content-Length [" + contentLengthHeader + "]");
            }
        }

        String contentRangeHeader = connection.getHeaderField("Content-Range");
        if (!TextUtils.isEmpty(contentRangeHeader)) {
            Matcher matcher = CONTENT_RANGE_HEADER.matcher(contentRangeHeader);
            if (matcher.find()) {
                try {
                    long contentLengthFromRange = Long.parseLong(matcher.group(2)) - Long.parseLong(matcher.group(1)) + 1L;
                    if (contentLength < 0L) {
                        contentLength = contentLengthFromRange;
                    } else if (contentLength != contentLengthFromRange) {
                        Log.w("GSYExoHttpDataSource", "Inconsistent headers [" + contentLengthHeader + "] [" + contentRangeHeader + "]");
                        contentLength = Math.max(contentLength, contentLengthFromRange);
                    }
                } catch (NumberFormatException var8) {
                    Log.e("GSYExoHttpDataSource", "Unexpected Content-Range [" + contentRangeHeader + "]");
                }
            }
        }

        return contentLength;
    }

    private void skipInternal() throws IOException {
        if (this.bytesSkipped != this.bytesToSkip) {
            byte[] skipBuffer = (byte[]) skipBufferReference.getAndSet(null);
            if (skipBuffer == null) {
                skipBuffer = new byte[4096];
            }

            while (this.bytesSkipped != this.bytesToSkip) {
                int readLength = (int) Math.min(this.bytesToSkip - this.bytesSkipped, (long) skipBuffer.length);
                int read = this.inputStream.read(skipBuffer, 0, readLength);
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedIOException();
                }

                if (read == -1) {
                    throw new EOFException();
                }

                this.bytesSkipped += (long) read;
                this.bytesTransferred(read);
            }

            skipBufferReference.set(skipBuffer);
        }
    }

    private int readInternal(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        } else {
            if (this.bytesToRead != -1L) {
                long bytesRemaining = this.bytesToRead - this.bytesRead;
                if (bytesRemaining == 0L) {
                    return -1;
                }

                readLength = (int) Math.min((long) readLength, bytesRemaining);
            }

            int read = this.inputStream.read(buffer, offset, readLength);
            if (read == -1) {
                if (this.bytesToRead != -1L) {
                    throw new EOFException();
                } else {
                    return -1;
                }
            } else {
                this.bytesRead += (long) read;
                this.bytesTransferred(read);
                return read;
            }
        }
    }

    private static void maybeTerminateInputStream(HttpURLConnection connection, long bytesRemaining) {
        if (Util.SDK_INT == 19 || Util.SDK_INT == 20) {
            try {
                InputStream inputStream = connection.getInputStream();
                if (bytesRemaining == -1L) {
                    if (inputStream.read() == -1) {
                        return;
                    }
                } else if (bytesRemaining <= 2048L) {
                    return;
                }

                String className = inputStream.getClass().getName();
                if ("com.android.okhttp.internal.http.HttpTransport$ChunkedInputStream".equals(className) || "com.android.okhttp.internal.http.HttpTransport$FixedLengthInputStream".equals(className)) {
                    Class<?> superclass = inputStream.getClass().getSuperclass();
                    Method unexpectedEndOfInput = superclass.getDeclaredMethod("unexpectedEndOfInput");
                    unexpectedEndOfInput.setAccessible(true);
                    unexpectedEndOfInput.invoke(inputStream);
                }
            } catch (Exception var7) {
                ;
            }

        }
    }

    private void closeConnectionQuietly() {
        if (this.connection != null) {
            try {
                this.connection.disconnect();
            } catch (Exception var2) {
                Log.e("GSYExoHttpDataSource", "Unexpected error while disconnecting", var2);
            }

            this.connection = null;
        }

    }
}
