package com.shuyu.gsyvideoplayer.player;

import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

/**
 * 播放内核工厂
 * Created by guoshuyu on 2018/5/21.
 */
public class PlayerFactory {

    public static IPlayerManager getPlayManager(int videoType) {
        switch (videoType) {
            case GSYVideoType.IJKEXOPLAYER2:
                return new EXO2PlayerManager();
            case GSYVideoType.SYSTEMPLAYER:
                return new SystemPlayerManager();
            case GSYVideoType.IJKPLAYER:
            default:
                return new IJKPlayerManager();
        }
    }

}
