/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.danmaku.ijk.media.exo2.demo.player;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;


@TargetApi(16)
public class SimpleExoPlayer2 implements ExoPlayer {


  public interface VideoListener {


    void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                            float pixelWidthHeightRatio);

    /**
     * Called when a frame is rendered for the first time since setting the surface, and when a
     * frame is rendered for the first time since a video track was selected.
     */
    void onRenderedFirstFrame();

  }

  private static final String TAG = "SimpleExoPlayer";

  protected final Renderer[] renderers;

  private final ExoPlayer player;
  private final ComponentListener componentListener;
  private final CopyOnWriteArraySet<VideoListener> videoListeners;
  private final CopyOnWriteArraySet<TextOutput> textOutputs;
  private final CopyOnWriteArraySet<MetadataOutput> metadataOutputs;
  private final int videoRendererCount;
  private final int audioRendererCount;

  private Format videoFormat;
  private Format audioFormat;

  private Surface surface;
  private boolean ownsSurface;
  @C.VideoScalingMode
  private int videoScalingMode;
  private SurfaceHolder surfaceHolder;
  private TextureView textureView;
  private AudioRendererEventListener audioDebugListener;
  private VideoRendererEventListener videoDebugListener;
  private DecoderCounters videoDecoderCounters;
  private DecoderCounters audioDecoderCounters;
  private int audioSessionId;
  private AudioAttributes audioAttributes;
  private float audioVolume;

  public SimpleExoPlayer2(RenderersFactory renderersFactory, TrackSelector trackSelector,
                             LoadControl loadControl) {
    componentListener = new ComponentListener();
    videoListeners = new CopyOnWriteArraySet<>();
    textOutputs = new CopyOnWriteArraySet<>();
    metadataOutputs = new CopyOnWriteArraySet<>();
    Looper eventLooper = Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper();
    Handler eventHandler = new Handler(eventLooper);
    renderers = renderersFactory.createRenderers(eventHandler, componentListener, componentListener,
        componentListener, componentListener);

    // Obtain counts of video and audio renderers.
    int videoRendererCount = 0;
    int audioRendererCount = 0;
    for (Renderer renderer : renderers) {
      switch (renderer.getTrackType()) {
        case C.TRACK_TYPE_VIDEO:
          videoRendererCount++;
          break;
        case C.TRACK_TYPE_AUDIO:
          audioRendererCount++;
          break;
      }
    }
    this.videoRendererCount = videoRendererCount;
    this.audioRendererCount = audioRendererCount;

    // Set initial values.
    audioVolume = 1;
    audioSessionId = C.AUDIO_SESSION_ID_UNSET;
    audioAttributes = AudioAttributes.DEFAULT;
    videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT;

    // Build the player and associated objects.
    player = createExoPlayerImpl(renderers, trackSelector, loadControl);
  }


  public void setVideoScalingMode(@C.VideoScalingMode int videoScalingMode) {
    this.videoScalingMode = videoScalingMode;
    ExoPlayerMessage[] messages = new ExoPlayerMessage[videoRendererCount];
    int count = 0;
    for (Renderer renderer : renderers) {
      if (renderer.getTrackType() == C.TRACK_TYPE_VIDEO) {
        messages[count++] = new ExoPlayerMessage(renderer, C.MSG_SET_SCALING_MODE,
            videoScalingMode);
      }
    }
    player.sendMessages(messages);
  }

  /**
   * Returns the video scaling mode.
   */
  public @C.VideoScalingMode int getVideoScalingMode() {
    return videoScalingMode;
  }


  public void clearVideoSurface() {
    setVideoSurface(null);
  }


  public void setVideoSurface(Surface surface) {
    removeSurfaceCallbacks();
    setVideoSurfaceInternal(surface, false);
  }

  public void clearVideoSurface(Surface surface) {
    if (surface != null && surface == this.surface) {
      setVideoSurface(null);
    }
  }


  public void setVideoSurfaceHolder(SurfaceHolder surfaceHolder) {
    removeSurfaceCallbacks();
    this.surfaceHolder = surfaceHolder;
    if (surfaceHolder == null) {
      setVideoSurfaceInternal(null, false);
    } else {
      surfaceHolder.addCallback(componentListener);
      Surface surface = surfaceHolder.getSurface();
      setVideoSurfaceInternal(surface != null && surface.isValid() ? surface : null, false);
    }
  }


  public void clearVideoSurfaceHolder(SurfaceHolder surfaceHolder) {
    if (surfaceHolder != null && surfaceHolder == this.surfaceHolder) {
      setVideoSurfaceHolder(null);
    }
  }


  public void setVideoSurfaceView(SurfaceView surfaceView) {
    setVideoSurfaceHolder(surfaceView == null ? null : surfaceView.getHolder());
  }


  public void clearVideoSurfaceView(SurfaceView surfaceView) {
    clearVideoSurfaceHolder(surfaceView == null ? null : surfaceView.getHolder());
  }


  public void setVideoTextureView(TextureView textureView) {
    removeSurfaceCallbacks();
    this.textureView = textureView;
    if (textureView == null) {
      setVideoSurfaceInternal(null, true);
    } else {
      if (textureView.getSurfaceTextureListener() != null) {
        Log.w(TAG, "Replacing existing SurfaceTextureListener.");
      }
      textureView.setSurfaceTextureListener(componentListener);
      SurfaceTexture surfaceTexture = textureView.isAvailable() ? textureView.getSurfaceTexture()
          : null;
      setVideoSurfaceInternal(surfaceTexture == null ? null : new Surface(surfaceTexture), true);
    }
  }


  public void clearVideoTextureView(TextureView textureView) {
    if (textureView != null && textureView == this.textureView) {
      setVideoTextureView(null);
    }
  }


  @Deprecated
  public void setAudioStreamType(@C.StreamType int streamType) {
    @C.AudioUsage int usage = Util.getAudioUsageForStreamType(streamType);
    @C.AudioContentType int contentType = Util.getAudioContentTypeForStreamType(streamType);
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder().setUsage(usage).setContentType(contentType).build();
    setAudioAttributes(audioAttributes);
  }


  @Deprecated
  public @C.StreamType int getAudioStreamType() {
    return Util.getStreamTypeForAudioUsage(audioAttributes.usage);
  }


  public void setAudioAttributes(AudioAttributes audioAttributes) {
    this.audioAttributes = audioAttributes;
    ExoPlayerMessage[] messages = new ExoPlayerMessage[audioRendererCount];
    int count = 0;
    for (Renderer renderer : renderers) {
      if (renderer.getTrackType() == C.TRACK_TYPE_AUDIO) {
        messages[count++] = new ExoPlayerMessage(renderer, C.MSG_SET_AUDIO_ATTRIBUTES,
            audioAttributes);
      }
    }
    player.sendMessages(messages);
  }

  /**
   * Returns the attributes for audio playback.
   */
  public AudioAttributes getAudioAttributes() {
    return audioAttributes;
  }

  /**
   * Sets the audio volume, with 0 being silence and 1 being unity gain.
   *
   * @param audioVolume The audio volume.
   */
  public void setVolume(float audioVolume) {
    this.audioVolume = audioVolume;
    ExoPlayerMessage[] messages = new ExoPlayerMessage[audioRendererCount];
    int count = 0;
    for (Renderer renderer : renderers) {
      if (renderer.getTrackType() == C.TRACK_TYPE_AUDIO) {
        messages[count++] = new ExoPlayerMessage(renderer, C.MSG_SET_VOLUME, audioVolume);
      }
    }
    player.sendMessages(messages);
  }

  /**
   * Returns the audio volume, with 0 being silence and 1 being unity gain.
   */
  public float getVolume() {
    return audioVolume;
  }



  /**
   * Returns the video format currently being played, or null if no video is being played.
   */
  public Format getVideoFormat() {
    return videoFormat;
  }

  /**
   * Returns the audio format currently being played, or null if no audio is being played.
   */
  public Format getAudioFormat() {
    return audioFormat;
  }


  public int getAudioSessionId() {
    return audioSessionId;
  }

  public DecoderCounters getVideoDecoderCounters() {
    return videoDecoderCounters;
  }


  public DecoderCounters getAudioDecoderCounters() {
    return audioDecoderCounters;
  }

  /**
   * Adds a listener to receive video events.
   *
   * @param listener The listener to register.
   */
  public void addVideoListener(VideoListener listener) {
    videoListeners.add(listener);
  }

  /**
   * Removes a listener of video events.
   *
   * @param listener The listener to unregister.
   */
  public void removeVideoListener(VideoListener listener) {
    videoListeners.remove(listener);
  }


  @Deprecated
  public void setVideoListener(VideoListener listener) {
    videoListeners.clear();
    if (listener != null) {
      addVideoListener(listener);
    }
  }


  @Deprecated
  public void clearVideoListener(VideoListener listener) {
    removeVideoListener(listener);
  }

  /**
   * Registers an output to receive text events.
   *
   * @param listener The output to register.
   */
  public void addTextOutput(TextOutput listener) {
    textOutputs.add(listener);
  }

  /**
   * Removes a text output.
   *
   * @param listener The output to remove.
   */
  public void removeTextOutput(TextOutput listener) {
    textOutputs.remove(listener);
  }


  @Deprecated
  public void setTextOutput(TextOutput output) {
    textOutputs.clear();
    if (output != null) {
      addTextOutput(output);
    }
  }


  @Deprecated
  public void clearTextOutput(TextOutput output) {
    removeTextOutput(output);
  }

  /**
   * Registers an output to receive metadata events.
   *
   * @param listener The output to register.
   */
  public void addMetadataOutput(MetadataOutput listener) {
    metadataOutputs.add(listener);
  }

  /**
   * Removes a metadata output.
   *
   * @param listener The output to remove.
   */
  public void removeMetadataOutput(MetadataOutput listener) {
    metadataOutputs.remove(listener);
  }


  @Deprecated
  public void setMetadataOutput(MetadataOutput output) {
    metadataOutputs.clear();
    if (output != null) {
      addMetadataOutput(output);
    }
  }


  @Deprecated
  public void clearMetadataOutput(MetadataOutput output) {
    removeMetadataOutput(output);
  }

  /**
   * Sets a listener to receive debug events from the video renderer.
   *
   * @param listener The listener.
   */
  public void setVideoDebugListener(VideoRendererEventListener listener) {
    videoDebugListener = listener;
  }

  /**
   * Sets a listener to receive debug events from the audio renderer.
   *
   * @param listener The listener.
   */
  public void setAudioDebugListener(AudioRendererEventListener listener) {
    audioDebugListener = listener;
  }

  // ExoPlayer implementation

  @Override
  public Looper getPlaybackLooper() {
    return player.getPlaybackLooper();
  }

  @Override
  public void addListener(Player.EventListener listener) {
    player.addListener(listener);
  }

  @Override
  public void removeListener(Player.EventListener listener) {
    player.removeListener(listener);
  }

  @Override
  public int getPlaybackState() {
    return player.getPlaybackState();
  }

  @Override
  public void prepare(MediaSource mediaSource) {
    player.prepare(mediaSource);
  }

  @Override
  public void prepare(MediaSource mediaSource, boolean resetPosition, boolean resetState) {
    player.prepare(mediaSource, resetPosition, resetState);
  }

  @Override
  public void setPlayWhenReady(boolean playWhenReady) {
    player.setPlayWhenReady(playWhenReady);
  }

  @Override
  public boolean getPlayWhenReady() {
    return player.getPlayWhenReady();
  }

  @Override
  public @RepeatMode
  int getRepeatMode() {
    return player.getRepeatMode();
  }

  @Override
  public void setRepeatMode(@RepeatMode int repeatMode) {
    player.setRepeatMode(repeatMode);
  }

  @Override
  public void setShuffleModeEnabled(boolean shuffleModeEnabled) {
    player.setShuffleModeEnabled(shuffleModeEnabled);
  }

  @Override
  public boolean getShuffleModeEnabled() {
    return player.getShuffleModeEnabled();
  }

  @Override
  public boolean isLoading() {
    return player.isLoading();
  }

  @Override
  public void seekToDefaultPosition() {
    player.seekToDefaultPosition();
  }

  @Override
  public void seekToDefaultPosition(int windowIndex) {
    player.seekToDefaultPosition(windowIndex);
  }

  @Override
  public void seekTo(long positionMs) {
    player.seekTo(positionMs);
  }

  @Override
  public void seekTo(int windowIndex, long positionMs) {
    player.seekTo(windowIndex, positionMs);
  }

  @Override
  public void setPlaybackParameters(PlaybackParameters playbackParameters) {
    player.setPlaybackParameters(playbackParameters);
  }

  @Override
  public PlaybackParameters getPlaybackParameters() {
    return player.getPlaybackParameters();
  }

  @Override
  public void stop() {
    player.stop();
  }

  @Override
  public void release() {
    player.release();
    removeSurfaceCallbacks();
    if (surface != null) {
      if (ownsSurface) {
        surface.release();
      }
      surface = null;
    }
  }

  @Override
  public void sendMessages(ExoPlayerMessage... messages) {
    player.sendMessages(messages);
  }

  @Override
  public void blockingSendMessages(ExoPlayerMessage... messages) {
    player.blockingSendMessages(messages);
  }

  @Override
  public int getRendererCount() {
    return player.getRendererCount();
  }

  @Override
  public int getRendererType(int index) {
    return player.getRendererType(index);
  }

  @Override
  public TrackGroupArray getCurrentTrackGroups() {
    return player.getCurrentTrackGroups();
  }

  @Override
  public TrackSelectionArray getCurrentTrackSelections() {
    return player.getCurrentTrackSelections();
  }

  @Override
  public Timeline getCurrentTimeline() {
    return player.getCurrentTimeline();
  }

  @Override
  public Object getCurrentManifest() {
    return player.getCurrentManifest();
  }

  @Override
  public int getCurrentPeriodIndex() {
    return player.getCurrentPeriodIndex();
  }

  @Override
  public int getCurrentWindowIndex() {
    return player.getCurrentWindowIndex();
  }

  @Override
  public int getNextWindowIndex() {
    return player.getNextWindowIndex();
  }

  @Override
  public int getPreviousWindowIndex() {
    return player.getPreviousWindowIndex();
  }

  @Override
  public long getDuration() {
    return player.getDuration();
  }

  @Override
  public long getCurrentPosition() {
    return player.getCurrentPosition();
  }

  @Override
  public long getBufferedPosition() {
    return player.getBufferedPosition();
  }

  @Override
  public int getBufferedPercentage() {
    return player.getBufferedPercentage();
  }

  @Override
  public boolean isCurrentWindowDynamic() {
    return player.isCurrentWindowDynamic();
  }

  @Override
  public boolean isCurrentWindowSeekable() {
    return player.isCurrentWindowSeekable();
  }

  @Override
  public boolean isPlayingAd() {
    return player.isPlayingAd();
  }

  @Override
  public int getCurrentAdGroupIndex() {
    return player.getCurrentAdGroupIndex();
  }

  @Override
  public int getCurrentAdIndexInAdGroup() {
    return player.getCurrentAdIndexInAdGroup();
  }

  @Override
  public long getContentPosition() {
    return player.getContentPosition();
  }

  // Internal methods.


  protected ExoPlayer createExoPlayerImpl(Renderer[] renderers, TrackSelector trackSelector,
                                          LoadControl loadControl) {
    return ExoPlayerFactory.newInstance(renderers, trackSelector, loadControl);
  }

  private void removeSurfaceCallbacks() {
    if (textureView != null) {
      if (textureView.getSurfaceTextureListener() != componentListener) {
        Log.w(TAG, "SurfaceTextureListener already unset or replaced.");
      } else {
        textureView.setSurfaceTextureListener(null);
      }
      textureView = null;
    }
    if (surfaceHolder != null) {
      surfaceHolder.removeCallback(componentListener);
      surfaceHolder = null;
    }
  }

  private void setVideoSurfaceInternal(Surface surface, boolean ownsSurface) {
    // Note: We don't turn this method into a no-op if the surface is being replaced with itself
    // so as to ensure onRenderedFirstFrame callbacks are still called in this case.
    ExoPlayerMessage[] messages = new ExoPlayerMessage[videoRendererCount];
    int count = 0;
    for (Renderer renderer : renderers) {
      if (renderer.getTrackType() == C.TRACK_TYPE_VIDEO) {
        messages[count++] = new ExoPlayerMessage(renderer, C.MSG_SET_SURFACE, surface);
      }
    }
    if (this.surface != null && this.surface != surface) {
      // We're replacing a surface. Block to ensure that it's not accessed after the method returns.
      player.blockingSendMessages(messages);
      // If we created the previous surface, we are responsible for releasing it.
      if (this.ownsSurface) {
        this.surface.release();
      }
    } else {
      player.sendMessages(messages);
    }
    this.surface = surface;
    this.ownsSurface = ownsSurface;
  }

  private final class ComponentListener implements VideoRendererEventListener,
          AudioRendererEventListener, TextOutput, MetadataOutput, SurfaceHolder.Callback,
      TextureView.SurfaceTextureListener {

    // VideoRendererEventListener implementation

    @Override
    public void onVideoEnabled(DecoderCounters counters) {
      videoDecoderCounters = counters;
      if (videoDebugListener != null) {
        videoDebugListener.onVideoEnabled(counters);
      }
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs,
        long initializationDurationMs) {
      if (videoDebugListener != null) {
        videoDebugListener.onVideoDecoderInitialized(decoderName, initializedTimestampMs,
            initializationDurationMs);
      }
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
      videoFormat = format;
      if (videoDebugListener != null) {
        videoDebugListener.onVideoInputFormatChanged(format);
      }
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
      if (videoDebugListener != null) {
        videoDebugListener.onDroppedFrames(count, elapsed);
      }
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio) {
      for (VideoListener videoListener : videoListeners) {
        videoListener.onVideoSizeChanged(width, height, unappliedRotationDegrees,
            pixelWidthHeightRatio);
      }
      if (videoDebugListener != null) {
        videoDebugListener.onVideoSizeChanged(width, height, unappliedRotationDegrees,
            pixelWidthHeightRatio);
      }
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
      if (SimpleExoPlayer2.this.surface == surface) {
        for (VideoListener videoListener : videoListeners) {
          videoListener.onRenderedFirstFrame();
        }
      }
      if (videoDebugListener != null) {
        videoDebugListener.onRenderedFirstFrame(surface);
      }
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
      if (videoDebugListener != null) {
        videoDebugListener.onVideoDisabled(counters);
      }
      videoFormat = null;
      videoDecoderCounters = null;
    }

    // AudioRendererEventListener implementation

    @Override
    public void onAudioEnabled(DecoderCounters counters) {
      audioDecoderCounters = counters;
      if (audioDebugListener != null) {
        audioDebugListener.onAudioEnabled(counters);
      }
    }

    @Override
    public void onAudioSessionId(int sessionId) {
      audioSessionId = sessionId;
      if (audioDebugListener != null) {
        audioDebugListener.onAudioSessionId(sessionId);
      }
    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs,
        long initializationDurationMs) {
      if (audioDebugListener != null) {
        audioDebugListener.onAudioDecoderInitialized(decoderName, initializedTimestampMs,
            initializationDurationMs);
      }
    }

    @Override
    public void onAudioInputFormatChanged(Format format) {
      audioFormat = format;
      if (audioDebugListener != null) {
        audioDebugListener.onAudioInputFormatChanged(format);
      }
    }

    @Override
    public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs,
        long elapsedSinceLastFeedMs) {
      if (audioDebugListener != null) {
        audioDebugListener.onAudioSinkUnderrun(bufferSize, bufferSizeMs, elapsedSinceLastFeedMs);
      }
    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {
      if (audioDebugListener != null) {
        audioDebugListener.onAudioDisabled(counters);
      }
      audioFormat = null;
      audioDecoderCounters = null;
      audioSessionId = C.AUDIO_SESSION_ID_UNSET;
    }

    // TextOutput implementation

    @Override
    public void onCues(List<Cue> cues) {
      for (TextOutput textOutput : textOutputs) {
        textOutput.onCues(cues);
      }
    }

    // MetadataOutput implementation

    @Override
    public void onMetadata(Metadata metadata) {
      for (MetadataOutput metadataOutput : metadataOutputs) {
        metadataOutput.onMetadata(metadata);
      }
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      setVideoSurfaceInternal(holder.getSurface(), false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      setVideoSurfaceInternal(null, false);
    }

    // TextureView.SurfaceTextureListener implementation

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
      setVideoSurfaceInternal(new Surface(surfaceTexture), true);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
      // Do nothing.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
      setVideoSurfaceInternal(null, true);
      return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
      // Do nothing.
    }

  }

}
