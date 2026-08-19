package com.example.gsyvideoplayer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import android.net.Uri;
import android.system.Os;
import android.system.OsConstants;

import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.rtmp.RtmpDataSource;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.antmedia.rtmp_client.RtmpClient;

/**
 * 2026-08-19: Guards the module-owned RTMP JNI library against 16 KB page-size regressions.
 */
@RunWith(AndroidJUnit4.class)
public class Rtmp16KbPageInstrumentedTest {

    @Test
    public void media3RtmpReadsFromConfiguredServerOn16KbDevice() throws Exception {
        long pageSize = Os.sysconf(OsConstants._SC_PAGESIZE);
        assumeTrue("This regression test requires a 16 KB page-size device", pageSize == 16 * 1024);

        String testUrl = InstrumentationRegistry.getArguments().getString("rtmpTestUrl");
        assumeTrue("Set -e rtmpTestUrl to run the RTMP integration test",
                testUrl != null && !testUrl.trim().isEmpty());

        RtmpDataSource dataSource = new RtmpDataSource.Factory().createDataSource();
        try {
            dataSource.open(new DataSpec.Builder().setUri(Uri.parse(testUrl)).build());
            byte[] buffer = new byte[4096];
            assertTrue("The RTMP server returned no media data",
                    dataSource.read(buffer, 0, buffer.length) > 0);
        } finally {
            dataSource.close();
        }
    }

    @Test
    public void media3RtmpJniLoadsAndFailsGracefullyOn16KbDevice() {
        long pageSize = Os.sysconf(OsConstants._SC_PAGESIZE);
        assumeTrue("This regression test requires a 16 KB page-size device", pageSize == 16 * 1024);

        RtmpDataSource dataSource = new RtmpDataSource.Factory().createDataSource();
        DataSpec dataSpec = new DataSpec.Builder()
                .setUri(Uri.parse("rtmp://127.0.0.1:1/live/gsy-16kb-test"))
                .build();

        try {
            dataSource.open(dataSpec);
            fail("RTMP connection unexpectedly succeeded without a local RTMP server");
        } catch (RtmpClient.RtmpIOException expected) {
            // A Java-side connection error proves the 16 KB process loaded and executed JNI
            // instead of dying in the dynamic linker or RTMP native open/close path.
            assertNotNull(expected.getMessage());
        } finally {
            dataSource.close();
        }
    }
}
