/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.danmaku.ijk.media.exo2;


import static tv.danmaku.ijk.media.exo2.ExoSourceManager.getDataSourceFactory;
import static tv.danmaku.ijk.media.exo2.ExoSourceManager.getHttpDataSourceFactory;

import android.content.Context;
import android.net.Uri;

import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.CacheWriter;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadRequest;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

///https://github.com/google/ExoPlayer/issues/10831
public class CacheHelper {

    private static boolean init = false;

    private static DownloadManager downloadManager;

    private static ExecutorService downloadExecutorService;

    private static Context downloadContext;

    private static File downloadCacheDir;

    private static String downloadCachePath;

    public CacheHelper() {

    }

    protected CacheWriter cacheWriter;

    /**
     * 需要自己创建线程
     * length 是需要提前缓存的长度
     * */
    public void preCacheVideo(Context context, Uri uri, long length, CacheWriter.ProgressListener progressListener) throws IOException {
        preCacheVideo(context, uri, null, false, null, null, length, progressListener);
    }


    /**
     * 需要自己创建线程
     * length 是需要提前缓存的长度
     * */
    public void preCacheVideo(Context context, Uri uri, File cacheDir,
                              boolean preview, String uerAgent, Map<String, String> mapHeadData,
                              long length, CacheWriter.ProgressListener progressListener)
        throws IOException {
        CacheDataSource.Factory factory = new CacheDataSource.Factory();
        Cache cache = ExoSourceManager.acquireCacheSingleInstance(context, cacheDir);
        DataSpec dataSpec = new DataSpec(uri, /* position= */ 0, length);
        try {
            factory.setCache(cache)
                .setCacheReadDataSourceFactory(getDataSourceFactory(context, preview, uerAgent, mapHeadData))
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                .setUpstreamDataSourceFactory(getHttpDataSourceFactory(context, preview, uerAgent, mapHeadData));

            cacheWriter =
                new CacheWriter(
                    factory.createDataSource(), dataSpec, /* temporaryBuffer= */ null, /* progrtessListener= */ progressListener);
            cacheWriter.cache();
        } finally {
            ExoSourceManager.releaseCacheSingleInstance(context, cacheDir);
        }
    }

    public void cancel() {
        if (cacheWriter != null) {
            cacheWriter.cancel();
        }
    }

    public static synchronized DownloadManager getDownloadManager() throws Exception {
        if (!init) {
            throw new Exception("downloadManager never init");
        }
        return downloadManager;
    }


    public static synchronized void ensureDownloadManagerInitialized(Context context, File cacheDir, boolean preview, String uerAgent, Map<String, String> mapHeadData) {
        Context appContext = context.getApplicationContext();
        String cachePath = ExoSourceManager.getCachePath(appContext, cacheDir);
        if (downloadManager != null && !cachePath.equals(downloadCachePath)) {
            release();
        }
        if (downloadManager == null) {
            Cache cache = ExoSourceManager.acquireCacheSingleInstance(appContext, cacheDir, true);
            if (cache == null) {
                init = false;
                return;
            }
            try {
                downloadExecutorService = Executors.newFixedThreadPool(/* nThreads= */ 6);
                downloadManager =
                    new DownloadManager(
                        appContext,
                        ExoSourceManager.getDatabaseProvider() != null ? ExoSourceManager.getDatabaseProvider()
                            : new StandaloneDatabaseProvider(appContext),
                        cache,
                        getHttpDataSourceFactory(appContext, preview, uerAgent, mapHeadData),
                        downloadExecutorService);
                downloadContext = appContext;
                downloadCacheDir = cacheDir;
                downloadCachePath = cachePath;
            } catch (RuntimeException e) {
                if (downloadExecutorService != null) {
                    downloadExecutorService.shutdownNow();
                    downloadExecutorService = null;
                }
                ExoSourceManager.releaseCacheSingleInstance(appContext, cacheDir);
                throw e;
            }
        }
        init = true;
    }

    public static synchronized void download(String contentId, Uri contentUri) {
        if (downloadManager != null) {
            downloadManager.addDownload(
                new DownloadRequest.Builder(contentId, contentUri).build());
            downloadManager.resumeDownloads();
        }
    }

    public static synchronized void pause() {
        if (downloadManager != null) {
            downloadManager.pauseDownloads();
        }
    }

    public static synchronized void release() {
        if (downloadManager != null) {
            downloadManager.pauseDownloads();
            downloadManager.release();
        }
        if (downloadExecutorService != null) {
            downloadExecutorService.shutdownNow();
            try {
                downloadExecutorService.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            downloadExecutorService = null;
        }
        if (downloadContext != null) {
            ExoSourceManager.releaseCacheSingleInstance(downloadContext, downloadCacheDir);
        }
        downloadManager = null;
        downloadContext = null;
        downloadCacheDir = null;
        downloadCachePath = null;
        init = false;
    }

}
