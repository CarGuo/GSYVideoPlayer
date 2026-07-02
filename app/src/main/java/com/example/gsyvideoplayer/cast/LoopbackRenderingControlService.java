package com.example.gsyvideoplayer.cast;

import android.util.Log;

import org.jupnp.model.types.UnsignedIntegerFourBytes;
import org.jupnp.model.types.UnsignedIntegerTwoBytes;
import org.jupnp.support.model.Channel;
import org.jupnp.support.renderingcontrol.AbstractAudioRenderingControl;
import org.jupnp.support.renderingcontrol.RenderingControlException;

/**
 * Loopback RenderingControl 实现：Loopback 场景音量/静音无实际输出，只对 sender 有礼貌地回一个
 * 默认值，让对方"看起来能设"。
 *
 * <p>父类 {@link AbstractAudioRenderingControl} 已带 @UpnpAction 注解，子类仅 override 方法体。
 * 由 {@link LoopbackDeviceFactory} 中的 AnnotationLocalServiceBinder 扫描父类拿到 metadata。
 */
public class LoopbackRenderingControlService extends AbstractAudioRenderingControl {

    private static final String TAG = "LoopbackRenderingCtrl";

    private volatile boolean mute;
    private volatile int volume = 50;

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channel) throws RenderingControlException {
        return mute;
    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instanceId, String channel, boolean desiredMute) throws RenderingControlException {
        Log.i(TAG, "setMute " + desiredMute);
        this.mute = desiredMute;
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channel) throws RenderingControlException {
        return new UnsignedIntegerTwoBytes(volume);
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instanceId, String channel, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
        long raw = desiredVolume == null ? 0L : desiredVolume.getValue().longValue();
        int v = (int) Math.max(0L, Math.min(100L, raw));
        Log.i(TAG, "setVolume " + v);
        this.volume = v;
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return new UnsignedIntegerFourBytes[]{new UnsignedIntegerFourBytes(0L)};
    }

    @Override
    protected Channel[] getCurrentChannels() {
        return new Channel[]{Channel.Master};
    }
}
