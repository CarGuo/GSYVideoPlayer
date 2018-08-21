package com.shuyu.gsyvideoplayer.player;

/**
 * 播放内核工厂
 * Created by guoshuyu on 2018/5/21.
 */
public class PlayerFactory {

    private static IPlayerManager sPlayerManager;

    public static void setPlayManager(IPlayerManager playManager) {
        sPlayerManager = playManager;
    }

    public static IPlayerManager getPlayManager() {
        if(sPlayerManager == null) {
            sPlayerManager = new IjkPlayerManager();
        }
        return sPlayerManager;
    }

}
