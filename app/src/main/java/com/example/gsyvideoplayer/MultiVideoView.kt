package com.example.gsyvideoplayer

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge

/**
 * ================================================
 * 作    者：Weirdo_lin
 * 版    本：1.0
 * 创建日期：2022/06/14
 * 描    述：首页列表多视频播放器
 * ================================================
 */
class MultiVideoView : StandardGSYVideoPlayer {

    var mCoverImage: AppCompatImageView? = null

    var mCoverOriginUrl: String = ""

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun init(context: Context) {
        super.init(context)
        mCoverImage = findViewById(R.id.thumbImage)

        if (mThumbImageViewLayout != null && (mCurrentState == -1 || mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.visibility = VISIBLE
        }
    }

    fun loadCoverImage(url: String, res: Int) {
        mCoverOriginUrl = url
        mCoverImage?.let {
            Glide.with(context.applicationContext).setDefaultRequestOptions(
                RequestOptions().frame(0).placeholder(res).centerCrop()
            ).load(url).into(it)
        }

    }

    /******************* 下方两个重载方法，在播放开始前不屏蔽封面，不需要可屏蔽  */
    override fun onSurfaceUpdated(surface: Surface?) {
        super.onSurfaceUpdated(surface)
        if (mThumbImageViewLayout != null && mThumbImageViewLayout.isVisible) {
            mThumbImageViewLayout.visibility = INVISIBLE
        }
    }

    override fun setViewShowState(view: View?, visibility: Int) {
        if (view === mThumbImageViewLayout && visibility != VISIBLE) {
            return
        }
        super.setViewShowState(view, visibility)
    }

    override fun onSurfaceAvailable(surface: Surface?) {
        super.onSurfaceAvailable(surface)
        if (GSYVideoType.getRenderType() != GSYVideoType.TEXTURE) {
            if (mThumbImageViewLayout != null && mThumbImageViewLayout.isVisible) {
                mThumbImageViewLayout.visibility = INVISIBLE
            }
        }
    }

    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        setViewShowState(mThumbImageViewLayout, VISIBLE)
    }

    override fun onPrepared() {
        super.onPrepared()
        CustomVideoManager.getCustomManager(key).isNeedMute = true
    }

    override fun showWifiDialog() {

    }

    override fun onAutoCompletion() {
    }

    override fun getGSYVideoManager(): GSYVideoViewBridge {
        CustomVideoManager.getCustomManager(key).initContext(context.applicationContext)
        return CustomVideoManager.getCustomManager(key)
    }

    override fun releaseVideos() {
        CustomVideoManager.releaseAllVideos(key)
    }

    override fun getLayoutId(): Int {
        return R.layout.multi_video_view
    }

    override fun touchSurfaceMoveFullLogic(absDeltaX: Float, absDeltaY: Float) {
        mChangePosition = false //不给触摸快进，如果需要，屏蔽下方代码即可
        mChangeVolume = false //不给触摸音量，如果需要，屏蔽下方代码即可
        mBrightness = false //不给触摸亮度，如果需要，屏蔽下方代码即可
    }


    override fun touchDoubleUp(e: MotionEvent?) {
//        super.touchDoubleUp(e);
    }

    override fun touchSurfaceUp() {

    }

    override fun onAudioFocusGain() {
    }

    override fun onAudioFocusLoss() {
    }

    override fun onAudioFocusLossTransient() {
    }

    override fun onAudioFocusLossTransientCanDuck() {
    }

    private val key: String
        get() {
            if (mPlayPosition == -22) {
                Debuger.printfError(javaClass.simpleName + " used getKey() " + "******* PlayPosition never set. ********")
            }
            if (TextUtils.isEmpty(mPlayTag)) {
                Debuger.printfError(javaClass.simpleName + " used getKey() " + "******* PlayTag never set. ********")
            }
            return "MultiSampleVideo$mPlayPosition$mPlayTag"
        }

}
