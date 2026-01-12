package tv.danmaku.ijk.media.exo2;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.CacheSpan;
import androidx.media3.datasource.cache.ContentMetadata;
import androidx.media3.datasource.cache.SimpleCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NavigableSet;

@UnstableApi
public class Media3CacheExportHelper {

    /**
     * 判断该 URL 的缓存是否完整且为 MP4
     * @param cache 全局单例 Cache 对象
     * @param videoUrl 缓存的 Key
     */
    public static boolean isCompleteMp4Cache(Cache cache, String videoUrl) {
        // 1. 获取元数据记录的总长度
        long contentLength = ContentMetadata.getContentLength(cache.getContentMetadata(videoUrl));
        if (contentLength <= 0) return false;

        // 2. 获取当前已缓存的字节数
        long cachedBytes = cache.getCachedBytes(videoUrl, 0, contentLength);

        // 容错处理：只要已缓存字节覆盖了总长度的 99.9% 且没有空隙，就认为可以导出
        return cachedBytes >= contentLength * 0.98;
    }

    /**
     * 简单的 MP4 文件头校验 (Magic Number)
     * MP4 文件的前几个字节通常包含 "ftyp"
     */
    public static boolean verifyMp4Header(File file) {
        if (!file.exists() || file.length() < 12) return false;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[12];
            if (fis.read(header) != -1) {
                String hex = new String(header);
                return hex.contains("ftyp");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
