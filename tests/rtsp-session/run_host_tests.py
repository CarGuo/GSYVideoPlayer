#!/usr/bin/env python3
"""Compile the complete supplied production manager, unchanged, with host-only Android stubs.
This is deterministic JVM lifecycle testing, not Android or RTSP device runtime testing.
"""
import argparse, atexit, pathlib, re, shutil, subprocess, tempfile

TEST_ROOT = pathlib.Path(__file__).resolve().parent
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('source', nargs='?', type=pathlib.Path,
               help='Full manager source to test; defaults to the project manager')
p.add_argument('--project-root', type=pathlib.Path, default=TEST_ROOT.parents[1],
               help='GSY checkout providing the eight production reference sources')
p.add_argument('--label', default='candidate', help='Label for the isolated build directory')
p.add_argument('--output', type=pathlib.Path,
               help='Retain a unique build subdirectory here; otherwise use and clean a temp directory')
a = p.parse_args()
project_root = a.project_root.resolve()

def production_source(relative):
    for module in ('gsyVideoPlayer-java', 'gsyVideoPlayer-base'):
        candidate = project_root / module / 'src/main/java/com/shuyu/gsyvideoplayer' / relative
        if candidate.is_file():
            return candidate
    p.error('Missing production source in gsyVideoPlayer-java or gsyVideoPlayer-base: ' + relative)

a.source = a.source.resolve() if a.source else production_source('GSYVideoBaseManager.java')
if not a.source.is_file():
    p.error('Manager source is not a readable file: ' + str(a.source))
if a.output:
    a.output = a.output.resolve()
    a.output.mkdir(parents=True, exist_ok=True)
label = re.sub(r'[^A-Za-z0-9_.-]', '_', a.label)[:60] or 'candidate'
build = pathlib.Path(tempfile.mkdtemp(prefix='gsy-rtsp-' + label + '-', dir=a.output))
if not a.output:
    atexit.register(shutil.rmtree, build)
print('Isolated host build: ' + str(build), flush=True)
src = build / 'src'; classes = build / 'classes'
src.mkdir(parents=True, exist_ok=True); classes.mkdir(parents=True, exist_ok=True)
def put(path, value):
    f=src/path; f.parent.mkdir(parents=True,exist_ok=True); f.write_text(value)
for suffix in ['video/base/GSYVideoViewBridge.java','listener/GSYMediaPlayerListener.java','model/GSYModel.java','model/VideoOptionModel.java','player/IPlayerManager.java','player/BasePlayerManager.java','player/IPlayerInitSuccessListener.java','cache/ICacheManager.java']:
    reference = production_source(suffix)
    destination = src / ('com/shuyu/gsyvideoplayer/' + suffix)
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(reference, destination)
manager_copy = src / 'com/shuyu/gsyvideoplayer/GSYVideoBaseManager.java'
shutil.copyfile(a.source, manager_copy)
assert manager_copy.read_bytes() == a.source.read_bytes(), 'Production manager changed during copy'
put('android/content/Context.java','package android.content; public class Context { public Context getApplicationContext() { return this; } }')
put('android/media/MediaPlayer.java','package android.media; public class MediaPlayer { public static final int MEDIA_INFO_BUFFERING_START=701, MEDIA_INFO_BUFFERING_END=702; }')
put('android/os/Message.java','package android.os; public class Message { public int what, arg1, arg2; public Object obj; }')
put('android/text/TextUtils.java','package android.text; public class TextUtils { public static boolean isEmpty(String s) { return s==null||s.isEmpty(); } }')
put('android/view/Surface.java','package android.view; public class Surface { public boolean isValid() { return true; } }')
put('androidx/annotation/Nullable.java','package androidx.annotation; public @interface Nullable {}')
put('android/os/Looper.java', '''package android.os;
import java.util.*;
public class Looper {
 public static long now, serial; private static final Looper MAIN=new Looper();
 public static Looper getMainLooper(){return MAIN;}
 static class Job { final Handler h; final Runnable r; final long at,seq; Job(Handler h,Runnable r,long at){this.h=h;this.r=r;this.at=at;seq=serial++;} }
 final List<Job> jobs=new ArrayList<>();
 void add(Handler h,Runnable r,long delay){jobs.add(new Job(h,r,now+delay));}
 void remove(Handler h,Runnable r){jobs.removeIf(j->j.h==h&&j.r==r);}
 public void drain(){while(true){Job j=jobs.stream().filter(x->x.at<=now).min(Comparator.<Job>comparingLong(x->x.at).thenComparingLong(x->x.seq)).orElse(null);if(j==null)return;jobs.remove(j);j.r.run();}}
 public static void reset(){now=0;serial=0;MAIN.jobs.clear();}
 public static void advance(long time){now=time;MAIN.drain();}
 public int pending(){return jobs.size();}
}''')
put('android/os/Handler.java','''package android.os; public class Handler {
 final Looper l; public Handler(Looper l){this.l=l;} public void handleMessage(Message m){}
 public boolean sendMessage(Message m){l.add(this,()->handleMessage(m),0);return true;}
 public boolean post(Runnable r){l.add(this,r,0);return true;} public boolean postDelayed(Runnable r,long t){l.add(this,r,t);return true;}
 public void removeCallbacks(Runnable r){l.remove(this,r);}
}''')
put('com/shuyu/gsyvideoplayer/cache/CacheFactory.java','package com.shuyu.gsyvideoplayer.cache; public class CacheFactory { public static ICacheManager getCacheManager(){return null;} }')
put('com/shuyu/gsyvideoplayer/cast/CastCapability.java','package com.shuyu.gsyvideoplayer.cast; public class CastCapability { public static CastCapability getInstance(){return null;} }')
put('com/shuyu/gsyvideoplayer/utils/Debuger.java','package com.shuyu.gsyvideoplayer.utils; public class Debuger {public static void printfWarning(String s){} public static void printfError(String s){} }')
put('com/shuyu/gsyvideoplayer/utils/GSYVideoType.java','package com.shuyu.gsyvideoplayer.utils; public class GSYVideoType {public static boolean smart; public static boolean isSmartMediaCodec(){return smart;} public static boolean isMediaCodec(){return smart;} }')
put('com/shuyu/gsyvideoplayer/player/PlayerFactory.java','package com.shuyu.gsyvideoplayer.player; public class PlayerFactory {public static IPlayerManager getPlayManager(){return null;} }')
put('tv/danmaku/ijk/media/player/MediaInfo.java','package tv.danmaku.ijk.media.player; public class MediaInfo {public String mVideoDecoder;}')
put('tv/danmaku/ijk/media/player/IMediaPlayer.java','''package tv.danmaku.ijk.media.player; public interface IMediaPlayer {
 int MEDIA_INFO_VIDEO_ROTATION_CHANGED=10001, MEDIA_ERROR_UNKNOWN=1, MEDIA_ERROR_UNSUPPORTED=-1010;
 interface OnPreparedListener{void onPrepared(IMediaPlayer mp);} interface OnCompletionListener{void onCompletion(IMediaPlayer mp);}
 interface OnBufferingUpdateListener{void onBufferingUpdate(IMediaPlayer mp,int p);} interface OnSeekCompleteListener{void onSeekComplete(IMediaPlayer mp);}
 interface OnErrorListener{boolean onError(IMediaPlayer mp,int what,int extra);} interface OnInfoListener{boolean onInfo(IMediaPlayer mp,int what,int extra);}
 interface OnVideoSizeChangedListener{void onVideoSizeChanged(IMediaPlayer mp,int w,int h,int sn,int sd);}
 void setOnPreparedListener(OnPreparedListener l); void setOnCompletionListener(OnCompletionListener l); void setOnBufferingUpdateListener(OnBufferingUpdateListener l);
 void setOnSeekCompleteListener(OnSeekCompleteListener l); void setOnErrorListener(OnErrorListener l); void setOnInfoListener(OnInfoListener l); void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l);
 void setScreenOnWhilePlaying(boolean b); void prepareAsync(); int getVideoWidth(); int getVideoHeight();
}''')
put('tv/danmaku/ijk/media/player/IjkMediaPlayer.java','''package tv.danmaku.ijk.media.player; public class IjkMediaPlayer implements IMediaPlayer {
 public static final int FFP_PROPV_DECODER_MEDIACODEC=2; public int width=1920,height=1080; public boolean failPrepare; public int decoder;
 public int getVideoDecoder(){return decoder;} public MediaInfo getMediaInfo(){return null;}
 public void setOnPreparedListener(OnPreparedListener l){} public void setOnCompletionListener(OnCompletionListener l){} public void setOnBufferingUpdateListener(OnBufferingUpdateListener l){}
 public void setOnSeekCompleteListener(OnSeekCompleteListener l){} public void setOnErrorListener(OnErrorListener l){} public void setOnInfoListener(OnInfoListener l){} public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l){}
 public void setScreenOnWhilePlaying(boolean b){} public void prepareAsync(){if(failPrepare)throw new IllegalStateException("injected prepare failure");} public int getVideoWidth(){return width;} public int getVideoHeight(){return height;}
}''')
put('com/shuyu/gsyvideoplayer/player/IjkPlayerManager.java','''package com.shuyu.gsyvideoplayer.player;
import android.content.Context; import android.os.Message; import java.util.List; import com.shuyu.gsyvideoplayer.cache.ICacheManager; import com.shuyu.gsyvideoplayer.model.*; import tv.danmaku.ijk.media.player.*;
public class IjkPlayerManager extends BasePlayerManager {
 public final IjkMediaPlayer mp=new IjkMediaPlayer(); public int starts,releases; public long seek,position=1234; public GSYModel model;
 public IMediaPlayer getMediaPlayer(){return mp;}
 public void initVideoPlayer(Context c,Message m,List<VideoOptionModel> o,ICacheManager cache){model=(GSYModel)m.obj;}
 public void showDisplay(Message m){} public void setNeedMute(boolean b){} public void setVolume(float l,float r){} public void releaseSurface(){} public void release(){releases++;}
 public int getBufferedPercentage(){return 0;} public long getNetSpeed(){return 0;} public void setSpeedPlaying(float s,boolean st){} public boolean isSurfaceSupportLockCanvas(){return false;} public void setSpeed(float s,boolean st){}
 public void start(){starts++;} public void stop(){} public void pause(){} public int getVideoWidth(){return mp.width;} public int getVideoHeight(){return mp.height;} public boolean isPlaying(){return false;} public void seekTo(long t){seek=t;}
 public long getCurrentPosition(){return position;} public long getDuration(){return 0;} public int getVideoSarNum(){return 1;} public int getVideoSarDen(){return 1;}
}''')
shutil.copyfile(TEST_ROOT / 'SessionLifecycleTest.java', src / 'com/shuyu/gsyvideoplayer/SessionLifecycleTest.java')
javafiles=sorted(map(str,src.rglob('*.java')))
subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-encoding','UTF-8','-source','8','-target','8','-d',str(classes)]+javafiles,check=True)
r=subprocess.run(['java','-cp',str(classes),'com.shuyu.gsyvideoplayer.SessionLifecycleTest'])
raise SystemExit(r.returncode)
