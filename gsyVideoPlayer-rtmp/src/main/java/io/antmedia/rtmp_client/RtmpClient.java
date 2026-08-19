package io.antmedia.rtmp_client;

import java.io.IOException;

/**
 * Created by faraklit on 01.01.2016.
 */
public class RtmpClient {

    static {
        System.loadLibrary("rtmp-jni");
    }


    /**
     * RTMP read has received an EOF or READ_COMPLETE from the server
     */
    public final static int RTMP_READ_DONE = -1;

    /**
     * No error
     */
    private final static int RTMP_SUCCESS = 0;

    private final static int TIMEOUT_IN_MS = 10000;
    private long rtmpPointer = 0;

    /** Socket send timeout value in milliseconds */
    private int sendTimeoutInMs = TIMEOUT_IN_MS;
    /** Socket receive timeout value in seconds */
    private int receiveTimeoutInMs = TIMEOUT_IN_MS;

    public static class RtmpIOException extends IOException {

        /**
         * RTMP client could not allocate memory for rtmp context structure
         */
        public final static int OPEN_ALLOC = -2;

        /**
         * RTMP client could not open the stream on server
         */
        public final static int OPEN_CONNECT_STREAM = -3;

        /**
         * Received an unknown option from the RTMP server
         */
        public final static int UNKNOWN_RTMP_OPTION = -4;

        /**
         * RTMP server sent a packet with unknown AMF type
         */
        public final static int UNKNOWN_RTMP_AMF_TYPE = -5;

        /**
         * DNS server is not reachable
         */
        public final static int DNS_NOT_REACHABLE = -6;

        /**
         * Could not establish a socket connection to the server
         */
        public final static int SOCKET_CONNECT_FAIL = -7;

        /**
         * SOCKS negotiation failed
         */
        public final static int SOCKS_NEGOTIATION_FAIL = -8;

        /**
         * Could not create a socket to connect to RTMP server
         */
        public final static int SOCKET_CREATE_FAIL = -9;

        /**
         * SSL connection requested but not supported by the client
         */
        public final static int NO_SSL_TLS_SUPP = -10;

        /**
         * Could not connect to the server for handshake
         */
        public final static int HANDSHAKE_CONNECT_FAIL = -11;

        /**
         * Handshake with the server failed
         */
        public final static int HANDSHAKE_FAIL = -12;

        /**
         * RTMP server connection failed
         */
        public final static int RTMP_CONNECT_FAIL = -13;

        /**
         * Connection to the server lost
         */
        public final static int CONNECTION_LOST = -14;

        /**
         * Received an unexpected timestamp from the server
         */
        public final static int RTMP_KEYFRAME_TS_MISMATCH = -15;

        /**
         * The RTMP stream received is corrupted
         */
        public final static int RTMP_READ_CORRUPT_STREAM = -16;

        /**
         * Memory allocation failed
         */
        public final static int RTMP_MEM_ALLOC_FAIL = -17;

        /**
         * Stream indicated a bad datasize, could be corrupted
         */
        public final static int RTMP_STREAM_BAD_DATASIZE = -18;

        /**
         * RTMP packet received is too small
         */
        public final static int RTMP_PACKET_TOO_SMALL = -19;

        /**
         * Could not send packet to RTMP server
         */
        public final static int RTMP_SEND_PACKET_FAIL = -20;

        /**
         * AMF Encode failed while preparing a packet
         */
        public final static int RTMP_AMF_ENCODE_FAIL = -21;

        /**
         * Missing a :// in the URL
         */
        public final static int URL_MISSING_PROTOCOL = -22;

        /**
         * Hostname is missing in the URL
         */
        public final static int URL_MISSING_HOSTNAME = -23;

        /**
         * The port number indicated in the URL is wrong
         */
        public final static int URL_INCORRECT_PORT = -24;

        /**
         * Error code used by JNI to return after throwing an exception
         */
        public final static int RTMP_IGNORED = -25;

        /**
         * RTMP client has encountered an unexpected error
         */
        public final static int RTMP_GENERIC_ERROR = -26;

        /**
         * A sanity check failed in the RTMP client
         */
        public final static int RTMP_SANITY_FAIL = -27;

        public final int errorCode;

        public RtmpIOException(int errorCode) {
            super("RTMP error: " + errorCode);
            this.errorCode = errorCode;
        }

    }

    /**
     *  Sets the socket's send timeout value
     * @param sendTimeoutInMs
     * The send timeout value for the rtmp socket in milliseconds.
     * Parameter expects a non-zero positive integer and will reset timeout to the default value
     * (10000 ms) if zero or a negative integer is passed.
     *  */
    public void setSendTimeout(int sendTimeoutInMs) {
        if (sendTimeoutInMs > 0) {
            this.sendTimeoutInMs = sendTimeoutInMs;
        } else {
            this.sendTimeoutInMs = TIMEOUT_IN_MS;
        }
    }

    /**
     *  Sets the socket's receive timeout value
     * @param receiveTimeoutInMs
     * The receive timeout value for the rtmp socket in milliseconds.
     * Parameter expects a non-zero positive integer and will reset timeout to the default value
     * (10000 ms) if zero or a negative integer is passed.
     *  */
    public void setReceiveTimeout(int receiveTimeoutInMs) {
        if (receiveTimeoutInMs > 0) {
            this.receiveTimeoutInMs = receiveTimeoutInMs;
        } else {
            this.receiveTimeoutInMs = TIMEOUT_IN_MS;
        }
    }

    /**
     * opens the rtmp url
     * @param url
     * url of the stream
     * @param isPublishMode
     * if this is an publication it is true,
     * if connection is for getting stream it is false
     * @throws RtmpIOException if open fails
     */
    public void open(String url, boolean isPublishMode) throws RtmpIOException {
        rtmpPointer = nativeAlloc();
        if (rtmpPointer == 0) {
            throw new RtmpIOException(RtmpIOException.OPEN_ALLOC);
        }
        int result = nativeOpen(url, isPublishMode, rtmpPointer, sendTimeoutInMs,
            receiveTimeoutInMs);
        if (result != RTMP_SUCCESS) {
            rtmpPointer = 0;
            throw new RtmpIOException(result);
        }
    }

    private native long nativeAlloc();

    /**
     * opens the rtmp url
     * @param url
     * url of the stream
     * @param isPublishMode
     * if this is an publication it is true,
     * if connection is for getting stream it is false
     * @return return a minus value if it fails
     * returns

     *
     * returns {@link #RTMP_SUCCESS} if it is successful, throws RtmpIOException if it is failed
     */
    private native int nativeOpen(String url, boolean isPublishMode, long rtmpPointer,
        int sendTimeoutInMs, int receiveTimeoutInMs);

    /**
     * read data from rtmp connection
     *
     * @param data
     * buffer that will be filled
     * @param offset
     * offset to read data
     * @param size
     * size of the data to be read
     * @return
     * number of bytes to be read
     *
     * if it returns {@link #RTMP_READ_DONE}, it means stream is complete
     * and close function can be called.
     *
     *
     * @throws RtmpIOException if connection to server is lost
     * @throws IllegalStateException if call to {@link #open(String, boolean)} was unsuccessful or
     * missing
     */
    public int read(byte[] data, int offset, int size) throws RtmpIOException, IllegalStateException {
        int ret = nativeRead(data, offset, size, rtmpPointer);
        if (ret < RTMP_SUCCESS && ret != RTMP_READ_DONE) {
            throw new RtmpIOException(ret);
        }
        return ret;
    }

    private native int nativeRead(byte[] data, int offset, int size, long rtmpPointer) throws IllegalStateException;

    /**
     * Sends data to server
     * @param data
     * The data to write to server
     * @return number of bytes written
     * @throws RtmpIOException if connection to server is lost
     * @throws IllegalStateException if call to {@link #open(String, boolean)} was unsuccessful or
     * missing
     */
    public int write(byte[] data) throws RtmpIOException, IllegalStateException  {
        return write(data, 0, data.length);
    }

    /**
     * Sends data to server
     * @param data
     * data to write to server
     * @param offset
     * The offset from where data will be accessed to write to server
     * @param size
     * The number of bytes to write to server
     * @return number of bytes written
     * @throws RtmpIOException if connection to server is lost
     * @throws IllegalStateException if call to {@link #open(String, boolean)} was unsuccessful or
     * missing
     */
    public int write(byte[] data, int offset, int size) throws RtmpIOException, IllegalStateException {
        int ret = nativeWrite(data, offset, size, rtmpPointer);
        if (ret < RTMP_SUCCESS) {
            throw new RtmpIOException(ret);
        }
        return ret;
    }

    private native int nativeWrite(byte[] data, int offset, int size, long rtmpPointer) throws IllegalStateException;


    /**
     * @param pause
     * if pause is true then stream is going to be paused
     *
     * If pause is false, it unpauses the stream and it is ready to to play again
     *
     * @return true if it is successful else returns false
     * @throws RtmpIOException if connection is lost
     * @throws IllegalStateException if call to {@link #open(String, boolean)} was unsuccessful or
     * missing
     */
    public boolean pause(boolean pause) throws RtmpIOException, IllegalStateException {
        int ret = nativePause(pause, rtmpPointer);
        if (ret != RTMP_SUCCESS) {
            throw new RtmpIOException(ret);
        }
        return true;
    }
    private native int nativePause(boolean pause, long rtmpPointer) throws IllegalStateException;



    /**
     *
     * @return true if it is connected
     * false if it is not connected
     */
    public boolean isConnected() {
        return nativeIsConnected(rtmpPointer);
    }

    private native boolean nativeIsConnected(long rtmpPointer);

    /**
     *
     * closes the connection. Don't forget to call
     */
    public void close() {
        nativeClose(rtmpPointer);
        rtmpPointer = 0;
    }

    private native void nativeClose(long rtmpPointer);

}
