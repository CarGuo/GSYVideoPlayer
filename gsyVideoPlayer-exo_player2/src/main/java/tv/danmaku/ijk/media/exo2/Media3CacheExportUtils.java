package tv.danmaku.ijk.media.exo2;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.FileDataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.DefaultDataSource; // 添加这个
import androidx.media3.datasource.cache.ContentMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UnstableApi
public class Media3CacheExportUtils {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ExportCallback {
        void onSuccess(File file);
        void onProgress(float progress);
        void onError(Exception e);
    }

    /**
     * 导出 Media3 缓存（核心入口）
     *
     * @param context    上下文
     * @param videoUrl   视频 URL
     * @param targetFile 目标文件。
     * 1. 如果传 null，默认保存到 context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
     * 2. 如果传自定义路径，请确保应用有写入权限（Android 10+ 非私有目录可能会失败）
     * @param callback   回调
     */
    public static void export(Context context, String videoUrl, File targetFile, ExportCallback callback) {
        executor.execute(() -> {
            try {
                // --- 1. 智能路径处理逻辑 ---
                File finalTargetFile = targetFile;

                // 如果未指定路径，默认使用应用私有下载目录
                // 优点：不需要申请存储权限，Android 10+ 也能直接写，卸载应用自动清除
                if (finalTargetFile == null) {
                    File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    if (dir == null) {
                        dir = context.getFilesDir(); // 极端情况兜底
                    }
                    // 根据时间戳生成文件名，避免冲突
                    String fileName = "video_export_" + System.currentTimeMillis() + ".mp4";
                    finalTargetFile = new File(dir, fileName);
                }

                // 关键修复：确保父文件夹存在，否则会报 FileNotFoundException
                File parentDir = finalTargetFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (!created && !parentDir.exists()) {
                        throw new IOException("无法创建目录: " + parentDir.getAbsolutePath());
                    }
                }

                // --- 2. Cache 获取逻辑 (复用单例) ---
                // 使用 GSYVideoPlayer 现有的单例 Cache
                // 修复了 "Another SimpleCache instance uses the folder" 错误
                Cache cache = ExoSourceManager.getCacheSingleInstance(context, null);
                if (cache == null) {
                    throw new IllegalStateException("ExoPlayer Cache 未初始化，请先播放视频");
                }

                // --- 3. 导出核心逻辑 ---
                DataSpec dataSpec = new DataSpec.Builder()
                    .setUri(Uri.parse(videoUrl))
                    .setFlags(DataSpec.FLAG_ALLOW_CACHE_FRAGMENTATION)
                    .build();

                    // 使用 DefaultDataSource，如果缓存缺了一点点，它会自动联网补齐，而不是崩溃
                CacheDataSource dataSource = new CacheDataSource(
                    cache,
                    new DefaultDataSource(context, true), // <--- 修正：传入 Context，支持 HTTP/HTTPS 和 File
                    CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
                );

                long contentLength = ContentMetadata.getContentLength(cache.getContentMetadata(videoUrl));
                if (contentLength <= 0) {
                    contentLength = dataSource.open(dataSpec);
                } else {
                    dataSource.open(dataSpec);
                }

                FileOutputStream fos = new FileOutputStream(finalTargetFile);
                byte[] buffer = new byte[1024 * 64];
                int read;
                long totalRead = 0;

                while ((read = dataSource.read(buffer, 0, buffer.length)) != -1) {
                    fos.write(buffer, 0, read);
                    totalRead += read;

                    if (contentLength > 0) {
                        float progress = (float) totalRead / contentLength;
                        mainHandler.post(() -> callback.onProgress(progress));
                    }
                }

                fos.flush();
                fos.getFD().sync();
                fos.close();
                dataSource.close();

                // --- 4. 导出后处理 ---
                // 尝试通知系统扫描文件（主要针对旧版本 Android 或公共目录）
                scanFile(context, finalTargetFile);

                File finalResultFile = finalTargetFile;
                mainHandler.post(() -> callback.onSuccess(finalResultFile));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * 【高版本适配工具】将导出的私有文件复制到系统相册（Gallery/Movies）
     * 解决 Android 10+ 无法直接写入 /sdcard/Movies 的问题。
     * * 使用场景：
     * 1. 先调用 export() 导出到私有目录 (targetFile传null)。
     * 2. 在 onSuccess 中调用此方法，将视频“发布”到相册给用户看。
     *
     * @param context    上下文
     * @param privateFile 刚刚导出的私有文件
     * @param displayName 在相册中显示的文件名 (如 "my_video.mp4")
     */
    public static void copyToSystemGallery(Context context, File privateFile, String displayName) {
        executor.execute(() -> {
            try {
                if (privateFile == null || !privateFile.exists()) return;

                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DISPLAY_NAME, displayName);
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ 建议存入 Movies 下的子目录，例如 "GSYVideo"
                    values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + File.separator + "GSYVideo");
                    values.put(MediaStore.Video.Media.IS_PENDING, 1); // 标记为写入中
                }

                Uri externalContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                Uri insertUri = context.getContentResolver().insert(externalContentUri, values);

                if (insertUri != null) {
                    try (OutputStream os = context.getContentResolver().openOutputStream(insertUri);
                         InputStream is = new FileInputStream(privateFile)) {

                        if (os != null) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                os.write(buffer, 0, len);
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Video.Media.IS_PENDING, 0); // 写入完成，解除标记
                        context.getContentResolver().update(insertUri, values, null, null);
                    }

                    // 可选：复制完后删除私有目录的源文件
                    // privateFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void scanFile(Context context, File file) {
        try {
            MediaScannerConnection.scanFile(context,
                new String[]{file.getAbsolutePath()},
                new String[]{"video/mp4"}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
