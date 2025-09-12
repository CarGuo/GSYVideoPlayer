package com.example.gsyvideoplayer

import android.text.TextUtils
import com.shuyu.gsyvideoplayer.GSYVideoBaseManager
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager

class CustomVideoManager : GSYVideoBaseManager() {
    init {
        init()
    }

    override fun getPlayManager(): Exo2PlayerManager {
        return Exo2PlayerManager()
    }

    /**
     * 暂停播放
     */
    fun onPause(key: String?) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onVideoPause()
        }
    }

    /**
     * 恢复播放
     */
    fun onResume(key: String?) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onVideoResume()
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    fun onResume(key: String?, seek: Boolean) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onVideoResume(seek)
        }
    }

    companion object {
        var TAG: String = "AIVideoManager"

        private val sMap: MutableMap<String?, CustomVideoManager?> = HashMap<String?, CustomVideoManager?>()

        /**
         * 页面销毁了记得调用是否所有的video
         */
        fun releaseAllVideos(key: String?) {
            if (getCustomManager(key).listener() != null) {
                getCustomManager(key).listener().onCompletion()
            }
            getCustomManager(key).releaseMediaPlayer()
        }


        /**
         * 单例管理器
         */
        @Synchronized
        fun instance(): MutableMap<String?, CustomVideoManager?> {
            return sMap
        }

        /**
         * 单例管理器
         */
        @Synchronized
        fun getCustomManager(key: String?): CustomVideoManager {
            check(!TextUtils.isEmpty(key)) { "key not be empty" }
            var customVideoManager = sMap.get(key)
            if (customVideoManager == null) {
                customVideoManager = CustomVideoManager()
                customVideoManager.setNeedMute(true)
                //精准seek q
//            VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
//            List<VideoOptionModel> list = new ArrayList<>();
//            list.add(videoOptionModel);
//            customVideoManager.optionModelList = list;
//            customVideoManager.setTimeOut(120000, false);
                sMap.put(key, customVideoManager)
            }
            return customVideoManager
        }

        fun onPauseAll() {
            if (sMap.size > 0) {
                for (header in sMap.entries) {
                    header.value!!.onPause(header.key)
                }
            }
        }

        fun onResumeAll() {
            if (sMap.size > 0) {
                for (header in sMap.entries) {
                    header.value!!.onResume(header.key)
                }
            }
        }

        /**
         * 恢复暂停状态
         *
         * @param seek 是否产生seek动作
         */
        fun onResumeAll(seek: Boolean) {
            if (sMap.size > 0) {
                for (header in sMap.entries) {
                    header.value!!.onResume(header.key, seek)
                }
            }
        }

        fun clearAllVideo() {
            if (sMap.size > 0) {
                for (header in sMap.entries) {
                    releaseAllVideos(header.key)
                }
            }
            sMap.clear()
        }


        fun removeManager(key: String?) {
            sMap.remove(key)
        }
    }
}
