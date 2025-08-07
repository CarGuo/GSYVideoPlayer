package com.shuyu.gsyvideoplayer.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

/**
 * 音频焦点管理器 - 解决内存泄漏和重复请求问题
 * Audio Focus Manager - Solves memory leaks and duplicate request issues
 * Created for GSYVideoPlayer optimization
 */
public class GSYAudioFocusManager {
    
    private static final String TAG = "GSYAudioFocusManager";
    
    private WeakReference<AudioManager> mAudioManagerRef;
    private WeakReference<GSYAudioFocusListener> mListenerRef;
    private boolean mHasAudioFocus = false;
    private volatile boolean mIsReleased = false; // 标记是否已释放，避免重复操作
    
    /**
     * 音频焦点监听接口
     */
    public interface GSYAudioFocusListener {
        void onAudioFocusGain();
        void onAudioFocusLoss();
        void onAudioFocusLossTransient();
        void onAudioFocusLossTransientCanDuck();
    }
    
    /**
     * 弱引用的音频焦点监听器，避免内存泄漏
     */
    private final AudioManager.OnAudioFocusChangeListener mInternalListener = 
        new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                GSYAudioFocusListener listener = mListenerRef != null ? mListenerRef.get() : null;
                if (listener == null) {
                    // 如果监听器已被回收，说明外部对象已释放，应该放弃音频焦点
                    abandonAudioFocusInternal();
                    return;
                }
                
                // 使用主线程处理回调，确保UI操作安全
                new Handler(Looper.getMainLooper()).post(() -> handleAudioFocusChange(focusChange, listener));
            }
        };
    
    /**
     * 初始化音频焦点管理器
     * @param context 上下文，建议使用ApplicationContext
     * @param listener 音频焦点监听器
     */
    public void initialize(Context context, GSYAudioFocusListener listener) {
        if (mIsReleased) {
            Debuger.printfWarning(TAG + ": Cannot initialize after release, create a new instance");
            return;
        }
        
        if (context == null) {
            Debuger.printfError(TAG + ": Context is null, cannot initialize AudioManager");
            return;
        }
        
        if (listener == null) {
            Debuger.printfWarning(TAG + ": Listener is null, audio focus events will not be handled");
        }
        
        try {
            AudioManager audioManager = (AudioManager) context.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
            mAudioManagerRef = new WeakReference<>(audioManager);
            mListenerRef = new WeakReference<>(listener);
            
            Debuger.printfLog(TAG + ": AudioFocusManager initialized successfully");
        } catch (Exception e) {
            Debuger.printfError(TAG + ": Failed to initialize AudioManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 请求音频焦点
     * @return 是否成功请求到音频焦点
     */
    public boolean requestAudioFocus() {
        if (mIsReleased) {
            Debuger.printfWarning(TAG + ": Cannot request audio focus after release");
            return false;
        }
        
        AudioManager audioManager = mAudioManagerRef != null ? mAudioManagerRef.get() : null;
        if (audioManager == null) {
            Debuger.printfWarning(TAG + ": AudioManager is null, cannot request audio focus");
            return false;
        }
        
        if (mHasAudioFocus) {
            Debuger.printfLog(TAG + ": Already has audio focus, skipping request");
            return true;
        }
        
        try {
            int result = audioManager.requestAudioFocus(
                mInternalListener, 
                AudioManager.STREAM_MUSIC, 
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            );
            
            mHasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
            
            if (mHasAudioFocus) {
                Debuger.printfLog(TAG + ": Audio focus request granted");
            } else {
                Debuger.printfWarning(TAG + ": Audio focus request failed with result: " + result);
            }
            
            return mHasAudioFocus;
        } catch (Exception e) {
            Debuger.printfError(TAG + ": Exception while requesting audio focus: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 放弃音频焦点
     */
    public void abandonAudioFocus() {
        if (!mHasAudioFocus) {
            Debuger.printfLog(TAG + ": No audio focus to abandon");
            return;
        }
        
        abandonAudioFocusInternal();
    }
    
    /**
     * 内部放弃音频焦点方法
     */
    private void abandonAudioFocusInternal() {
        AudioManager audioManager = mAudioManagerRef != null ? mAudioManagerRef.get() : null;
        if (audioManager == null) {
            mHasAudioFocus = false;
            return;
        }
        
        try {
            int result = audioManager.abandonAudioFocus(mInternalListener);
            mHasAudioFocus = false;
            
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Debuger.printfLog(TAG + ": Audio focus abandoned successfully");
            } else {
                Debuger.printfWarning(TAG + ": Audio focus abandon failed with result: " + result);
            }
        } catch (Exception e) {
            mHasAudioFocus = false;
            Debuger.printfError(TAG + ": Exception while abandoning audio focus: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理音频焦点变化
     */
    private void handleAudioFocusChange(int focusChange, GSYAudioFocusListener listener) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mHasAudioFocus = true;
                try {
                    listener.onAudioFocusGain();
                } catch (Exception e) {
                    Debuger.printfError(TAG + ": Error in onAudioFocusGain: " + e.getMessage());
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                mHasAudioFocus = false;
                try {
                    listener.onAudioFocusLoss();
                } catch (Exception e) {
                    Debuger.printfError(TAG + ": Error in onAudioFocusLoss: " + e.getMessage());
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // 暂时失去焦点，不更改mHasAudioFocus状态
                try {
                    listener.onAudioFocusLossTransient();
                } catch (Exception e) {
                    Debuger.printfError(TAG + ": Error in onAudioFocusLossTransient: " + e.getMessage());
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                try {
                    listener.onAudioFocusLossTransientCanDuck();
                } catch (Exception e) {
                    Debuger.printfError(TAG + ": Error in onAudioFocusLossTransientCanDuck: " + e.getMessage());
                }
                break;
            default:
                Debuger.printfWarning(TAG + ": Unknown audio focus change: " + focusChange);
                break;
        }
    }
    
    /**
     * 检查是否拥有音频焦点
     */
    public boolean hasAudioFocus() {
        return mHasAudioFocus;
    }
    
    /**
     * 释放所有资源
     */
    public void release() {
        if (mIsReleased) {
            return; // 避免重复释放
        }
        
        abandonAudioFocus();
        
        if (mAudioManagerRef != null) {
            mAudioManagerRef.clear();
            mAudioManagerRef = null;
        }
        
        if (mListenerRef != null) {
            mListenerRef.clear();
            mListenerRef = null;
        }
        
        mIsReleased = true;
        Debuger.printfLog(TAG + ": AudioFocusManager released");
    }
    
    /**
     * 获取当前音频管理器（用于其他音频操作，如音量控制）
     * @return AudioManager实例，可能为null
     */
    public AudioManager getAudioManager() {
        if (mIsReleased) {
            return null;
        }
        return mAudioManagerRef != null ? mAudioManagerRef.get() : null;
    }
}