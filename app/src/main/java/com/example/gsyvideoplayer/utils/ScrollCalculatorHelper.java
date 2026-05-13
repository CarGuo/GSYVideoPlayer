package com.example.gsyvideoplayer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.*;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 计算滑动，自动播放的帮助类
 * Created by guoshuyu on 2017/11/2.
 */

public class ScrollCalculatorHelper {

    private int firstVisible = 0;
    private int lastVisible = 0;
    private int visibleCount = 0;
    private int playId;
    private int rangeTop;
    private int rangeBottom;
    private PlayRunnable runnable;

    private final Handler playHandler = new Handler(Looper.getMainLooper());

    private AlertDialog wifiDialog;

    public ScrollCalculatorHelper(int playId, int rangeTop, int rangeBottom) {
        this.playId = playId;
        this.rangeTop = rangeTop;
        this.rangeBottom = rangeBottom;
    }

    public void setPlayRange(int rangeTop, int rangeBottom) {
        this.rangeTop = rangeTop;
        this.rangeBottom = rangeBottom;
    }

    public void release() {
        if (runnable != null) {
            playHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (wifiDialog != null && wifiDialog.isShowing()) {
            wifiDialog.dismiss();
        }
        wifiDialog = null;
    }

    public void onScrollStateChanged(RecyclerView view, int scrollState) {
        switch (scrollState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                playVideo(view);
                break;
        }
    }

    public void onScroll(RecyclerView view, int firstVisibleItem, int lastVisibleItem, int visibleItemCount) {
        if (firstVisible == firstVisibleItem && lastVisible == lastVisibleItem && visibleCount == visibleItemCount) {
            return;
        }
        firstVisible = firstVisibleItem;
        lastVisible = lastVisibleItem;
        visibleCount = visibleItemCount;
    }


    void playVideo(RecyclerView view) {

        if (view == null) {
            return;
        }

        RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
        if (layoutManager == null) {
            return;
        }

        GSYBaseVideoPlayer gsyBaseVideoPlayer = null;

        boolean needPlay = false;

        int childCount = layoutManager.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (layoutManager.getChildAt(i) != null && layoutManager.getChildAt(i).findViewById(playId) != null) {
                GSYBaseVideoPlayer player = (GSYBaseVideoPlayer) layoutManager.getChildAt(i).findViewById(playId);
                //播放器中心点在播放区域内即可作为候选，避免要求完全可见导致漏播。
                if (isInPlayRange(player)) {
                    gsyBaseVideoPlayer = player;
                    if ((player.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL
                            || player.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
                        needPlay = true;
                    }
                    break;
                }

            }
        }

        if (gsyBaseVideoPlayer != null && needPlay) {
            if (runnable != null) {
                GSYBaseVideoPlayer tmpPlayer = runnable.gsyBaseVideoPlayer;
                playHandler.removeCallbacks(runnable);
                runnable = null;
                if (tmpPlayer == gsyBaseVideoPlayer) {
                    return;
                }
            }
            runnable = new PlayRunnable(gsyBaseVideoPlayer);
            //降低频率
            playHandler.postDelayed(runnable, 400);
        }


    }

    private class PlayRunnable implements Runnable {

        GSYBaseVideoPlayer gsyBaseVideoPlayer;

        public PlayRunnable(GSYBaseVideoPlayer gsyBaseVideoPlayer) {
            this.gsyBaseVideoPlayer = gsyBaseVideoPlayer;
        }

        @Override
        public void run() {
            try {
                //如果未播放，需要播放
                if (canStartPlay(gsyBaseVideoPlayer)) {
                    startPlayLogic(gsyBaseVideoPlayer, gsyBaseVideoPlayer.getContext());
                }
            } finally {
                if (runnable == this) {
                    runnable = null;
                }
            }
        }
    }

    private boolean canStartPlay(GSYBaseVideoPlayer player) {
        if (player == null || !player.isShown() || !player.isAttachedToWindow()) {
            return false;
        }
        Activity activity = scanForActivity(player.getContext());
        if (activity != null && GSYVideoManager.isFullState(activity)) {
            return false;
        }
        int currentState = player.getCurrentPlayer().getCurrentState();
        return (currentState == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL
            || currentState == GSYBaseVideoPlayer.CURRENT_STATE_ERROR) && isInPlayRange(player);
    }

    private Activity scanForActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private boolean isInPlayRange(GSYBaseVideoPlayer player) {
        if (player == null || player.getHeight() <= 0) {
            return false;
        }
        int[] screenPosition = new int[2];
        player.getLocationOnScreen(screenPosition);
        int halfHeight = player.getHeight() / 2;
        int rangePosition = screenPosition[1] + halfHeight;
        //中心点在播放区域内
        return rangePosition >= rangeTop && rangePosition <= rangeBottom;
    }


    /***************************************自动播放的点击播放确认******************************************/
    private void startPlayLogic(GSYBaseVideoPlayer gsyBaseVideoPlayer, Context context) {
        if (!com.shuyu.gsyvideoplayer.utils.CommonUtil.isWifiConnected(context)) {
            //这里判断是否wifi
            showWifiDialog(gsyBaseVideoPlayer, context);
            return;
        }
        gsyBaseVideoPlayer.startPlayLogic();
    }

    private void showWifiDialog(final GSYBaseVideoPlayer gsyBaseVideoPlayer, Context context) {
        if (!NetworkUtils.isAvailable(context)) {
            Toast.makeText(context, context.getResources().getString(com.shuyu.gsyvideoplayer.R.string.no_net), Toast.LENGTH_LONG).show();
            return;
        }
        if (wifiDialog != null && wifiDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi));
        builder.setPositiveButton(context.getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (canStartPlay(gsyBaseVideoPlayer)) {
                    gsyBaseVideoPlayer.startPlayLogic();
                }
            }
        });
        builder.setNegativeButton(context.getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        wifiDialog = builder.create();
        wifiDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                wifiDialog = null;
            }
        });
        wifiDialog.show();
    }

}
