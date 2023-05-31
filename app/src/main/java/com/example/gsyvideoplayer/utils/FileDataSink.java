package com.example.gsyvideoplayer.utils;


import com.google.android.exoplayer2.upstream.DataSink;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demo 测试代码，还不具备可用性
 * **/
public final class FileDataSink implements DataSink {

    public static final class Factory implements DataSink.Factory {

        private final File file;

        public Factory(File file) {
            this.file = file;
        }

        @Override
        public DataSink createDataSink() {
            return new FileDataSink(file);
        }
    }

    private File file;
    private FileOutputStream fileOutputStream;

    public FileDataSink(File file) {
        this.file = file;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void open(DataSpec dataSpec) throws IOException {
        fileOutputStream = new FileOutputStream(file, false);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        fileOutputStream.write(buffer, offset, length);
    }

    @Override
    public void close() throws IOException {
        fileOutputStream.close();
    }
}
