package com.example.gsyvideoplayer.adapter;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.util.IOUtils;

/**
 * Created by guoshuyu on 2017/2/16.
 */

public class DanamakuAdapter extends BaseCacheStuffer.Proxy {


    private Drawable mDrawable;

    private IDanmakuView mDanmakuView;


    public DanamakuAdapter(IDanmakuView mDanmakuView) {
        super();
        this.mDanmakuView = mDanmakuView;
    }

    @Override
    public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
        if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕
            // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
            new Thread() {

                @Override
                public void run() {
                    String url = "http://www.bilibili.com/favicon.ico";
                    InputStream inputStream = null;
                    Drawable drawable = mDrawable;
                    if (drawable == null) {
                        try {
                            URLConnection urlConnection = new URL(url).openConnection();
                            inputStream = urlConnection.getInputStream();
                            drawable = BitmapDrawable.createFromStream(inputStream, "bitmap");
                            mDrawable = drawable;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                        }
                    }
                    if (drawable != null) {
                        drawable.setBounds(0, 0, 100, 100);
                        SpannableStringBuilder spannable = createSpannable(drawable);
                        danmaku.text = spannable;
                        if (mDanmakuView != null) {
                            mDanmakuView.invalidateDanmaku(danmaku, false);
                        }
                        return;
                    }
                }
            }.start();
        }
    }


    @Override
    public void releaseResource(BaseDanmaku danmaku) {
        // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
    }


    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        ImageSpan span = new ImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("图文混排");
        spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

}
