package com.shuyu.gsyvideoplayer.player;

import android.content.Context;
import android.os.Message;

import com.shuyu.gsyvideoplayer.model.VideoOptionModel;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 播放器差异管理接口
 * Created by guoshuyu on 2018/1/11.
 */

public interface IPlayerManager {

    IMediaPlayer getMediaPlayer();

    void initVideoPlayer(Context context, Message message, List<VideoOptionModel> optionModelList);

    void showDisplay(Message msg);

    void setSpeed(float speed, boolean soundTouch);

    void setNeedMute(boolean needMute);

    void release();

}
