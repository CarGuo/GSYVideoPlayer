package com.shuyu.gsyvideoplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import com.shuyu.gsyvideoplayer.listener.GSYVideoGifSaveListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * gif截图
 * Created by guoshuyu on 2017/10/10.
 */

public class GifCreateHelper {

    private boolean mSaveShotBitmapSuccess = true;

    private StandardGSYVideoPlayer mPlayer;

    private TaskLocal mTimerTask;

    private Timer mTimer = new Timer();

    private List<String> mPicList = new ArrayList<>();

    private File mTmpPath;

    private GSYVideoGifSaveListener mGSYVideoGifSaveListener;

    //gif的帧之间延时
    private int mDelay = 0;

    //采样率
    private int mSampleSize = 1;

    //缩放比例
    private int mScaleSize = 5;

    //截图频率，毫秒
    private int mFrequencyCount = 50;

    public GifCreateHelper(StandardGSYVideoPlayer standardGSYVideoPlayer, GSYVideoGifSaveListener gsyVideoGifSaveListener) {
        this(standardGSYVideoPlayer, gsyVideoGifSaveListener, 0, 1, 5, 50);
    }

    /**
     * @param delay          每一帧之间的延时
     * @param inSampleSize   采样率，越大图片越小，越大图片越模糊，需要处理的时长越短
     * @param scaleSize      缩减尺寸比例，对生成的截图进行缩减，越大图片越模糊，需要处理的时长越短
     * @param frequencyCount 截图频率，毫秒，用在定时获取帧图片，越大获取的越少
     */
    public GifCreateHelper(StandardGSYVideoPlayer standardGSYVideoPlayer, GSYVideoGifSaveListener gsyVideoGifSaveListener,
                           int delay, int inSampleSize, int scaleSize, int frequencyCount) {
        mPlayer = standardGSYVideoPlayer;
        mGSYVideoGifSaveListener = gsyVideoGifSaveListener;
        mDelay = delay;
        mSampleSize = inSampleSize;
        mScaleSize = scaleSize;
        mFrequencyCount = frequencyCount;
    }

    /**
     * 开始gif截图
     *
     * @param tmpPicPath 临时缓存图路径
     */
    public void startGif(File tmpPicPath) {
        mTmpPath = tmpPicPath;
        cancelTask();
        clearTmpFiles();
        if (mTmpPath != null && !mTmpPath.exists()) {
            mTmpPath.mkdirs();
        }
        mTimerTask = new TaskLocal();
        //频率可以稍微控制下
        mTimer.schedule(mTimerTask, 0, mFrequencyCount);
    }

    /**
     * 生成gif
     *
     * @param path gif图保存路径
     */
    public void stopGif(final File path) {
        cancelTask();
        mSaveShotBitmapSuccess = true;
        final List<String> pics = new ArrayList<>(mPicList);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (pics.size() > 2) {
                    // inSampleSize  采样率，越大图片越小，越大图片越模糊，需要处理的时长越短
                    // scaleSize 缩减尺寸比例，对生成的截图进行缩减，越大图片越模糊，需要处理的时长越短
                    createGif(path, pics, mDelay, mSampleSize, mScaleSize, mGSYVideoGifSaveListener);
                } else {
                    clearTmpFiles();
                    mGSYVideoGifSaveListener.result(false, null);
                }
            }
        }).start();
    }

    /**
     * 取消帧图片定时任务
     */
    public void cancelTask() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    /**
     * 彻底释放 GIF 录制相关资源
     */
    public void release() {
        cancelTask();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        clearTmpFiles();
    }

    /**
     * 开始保存帧图片
     */
    private void startSaveBitmap() {
        if (mTmpPath == null || mPlayer == null) {
            mSaveShotBitmapSuccess = true;
            return;
        }
        // 保存的文件路径，请确保文件夹目录已经创建
        File file = new File(mTmpPath, "GSY-TMP-FRAME" + System.currentTimeMillis() + ".tmp");
        mPlayer.saveFrame(file, new GSYVideoShotSaveListener() {
            @Override
            public void result(boolean success, final File file) {
                mSaveShotBitmapSuccess = true;
                if (success) {
                    Debuger.printfError(" SUCCESS CREATE FILE " + file.getAbsolutePath());
                    mPicList.add(file.getAbsolutePath());
                } else if (file != null && file.exists()) {
                    file.delete();
                }
            }
        });
    }


    /**
     * 生成gif图
     *
     * @param file                    保存的文件路径，请确保文件夹目录已经创建
     * @param pics                    需要转化的bitmap本地路径集合
     * @param delay                   每一帧之间的延时
     * @param inSampleSize            采样率，越大图片越小，越大图片越模糊，需要处理的时长越短
     * @param scaleSize               缩减尺寸比例，对生成的截图进行缩减，越大图片越模糊，需要处理的时长越短
     * @param gsyVideoGifSaveListener 结果回调
     */
    public void createGif(File file, List<String> pics, int delay, int inSampleSize, int scaleSize,
                          final GSYVideoGifSaveListener gsyVideoGifSaveListener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
        localAnimatedGifEncoder.start(baos);//start
        localAnimatedGifEncoder.setRepeat(0);//设置生成gif的开始播放时间。0为立即开始播放
        localAnimatedGifEncoder.setDelay(delay);
        boolean hasValidFrame = false;
        for (int i = 0; i < pics.size(); i++) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = true; // 先获取原大小
            BitmapFactory.decodeFile(pics.get(i), options);
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                continue;
            }
            double w = (double) options.outWidth / scaleSize;
            double h = (double) options.outHeight / scaleSize;
            options.inJustDecodeBounds = false; // 获取新的大小
            Bitmap bitmap = BitmapFactory.decodeFile(pics.get(i), options);
            if (bitmap == null) {
                continue;
            }
            Bitmap pic = ThumbnailUtils.extractThumbnail(bitmap, (int) w, (int) h);
            if (pic == null) {
                bitmap.recycle();
                continue;
            }
            localAnimatedGifEncoder.addFrame(pic);
            hasValidFrame = true;
            bitmap.recycle();
            pic.recycle();
            gsyVideoGifSaveListener.process(i + 1, pics.size());
        }
        localAnimatedGifEncoder.finish();//finish
        if (!hasValidFrame) {
            clearTmpFiles();
            gsyVideoGifSaveListener.result(false, file);
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file.getPath());
            baos.writeTo(fos);
            baos.flush();
            fos.flush();
            baos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            clearTmpFiles();
            gsyVideoGifSaveListener.result(false, file);
            return;
        }
        clearTmpFiles();
        gsyVideoGifSaveListener.result(true, file);
    }

    private void clearTmpFiles() {
        for (String pic : mPicList) {
            if (pic != null) {
                File file = new File(pic);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        mPicList.clear();
    }

    /**
     * 保存帧图片定时任务
     */
    private class TaskLocal extends TimerTask {
        @Override
        public void run() {
            if (mSaveShotBitmapSuccess) {
                mSaveShotBitmapSuccess = false;
                startSaveBitmap();
            }
        }
    }
}
