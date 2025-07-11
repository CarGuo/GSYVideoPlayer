package com.shuyu.gsyvideoplayer.player;

/**
 * 播放内核工厂
 * Created by guoshuyu on 2018/5/21.
 */
public class PlayerFactory {

    private static volatile Class<? extends IPlayerManager> sPlayerManager;

    public static synchronized void setPlayManager(Class<? extends IPlayerManager> playManager) {
        sPlayerManager = playManager;
    }

    public static IPlayerManager getPlayManager() {
        Class<? extends IPlayerManager> localPlayerManager = sPlayerManager;
        if (localPlayerManager == null) {
            synchronized (PlayerFactory.class) {
                localPlayerManager = sPlayerManager;
                if (localPlayerManager == null) {
                    sPlayerManager = localPlayerManager = IjkPlayerManager.class;
                }
            }
        }
        try {
            return localPlayerManager.newInstance();
        } catch (InstantiationException e) {
            android.util.Log.e("PlayerFactory", "Failed to instantiate player manager: " + localPlayerManager.getName(), e);
        } catch (IllegalAccessException e) {
            android.util.Log.e("PlayerFactory", "Illegal access to player manager: " + localPlayerManager.getName(), e);
        } catch (Exception e) {
            android.util.Log.e("PlayerFactory", "Unexpected error creating player manager: " + localPlayerManager.getName(), e);
        }
        return null;
    }

}
