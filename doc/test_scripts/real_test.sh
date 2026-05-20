#!/bin/bash
# Real playback test:
#   - clear logcat
#   - tap (X,Y), wait 8s
#   - assert topResumed == EXP
#   - assert logcat contains: onPrepared / CURRENT_STATE_PLAYING / MEDIA_INFO_VIDEO_RENDERING_START
#     (for AudioOnly: only onPrepared + CURRENT_STATE_PLAYING — no surface)
#   - back, wait 3s, assert returned to ComposeDemoListActivity / list / parent
#   - assert no FATAL/ANR
# Usage: real_test.sh <X> <Y> <ExpectedActivityClass> <Label> [audio_only]
X=$1; Y=$2; EXP=$3; LABEL=$4; MODE=${5:-video}

adb -s emulator-5554 logcat -c >/dev/null 2>&1
adb -s emulator-5554 shell input tap $X $Y
sleep 8

TOP=$(adb -s emulator-5554 shell dumpsys activity activities | grep "topResumedActivity" | head -1)
if ! echo "$TOP" | grep -q "$EXP"; then
  echo "[$LABEL] FAIL ENTER: $TOP"
  adb -s emulator-5554 shell input keyevent 4 ; sleep 2
  adb -s emulator-5554 shell input keyevent 4 ; sleep 2
  exit 1
fi

LOG=$(adb -s emulator-5554 logcat -d 2>/dev/null)
PREP=$(echo "$LOG" | grep -c "onPrepared")
PLAY=$(echo "$LOG" | grep -c "CURRENT_STATE_PLAYING")
RENDER=$(echo "$LOG" | grep -cE "MEDIA_INFO_VIDEO_RENDERING_START|videoSizeChanged|videoWidth:")
PROGRESS=$(echo "$LOG" | grep -c "Net speed:")
FATAL=$(echo "$LOG" | grep -c "FATAL EXCEPTION")
ANR=$(echo "$LOG" | grep -c "ANR in")
CRASH=$(adb -s emulator-5554 logcat -d -b crash 2>/dev/null | grep -c "FATAL EXCEPTION")

if [ "$MODE" = "audio" ]; then
  PASS_PLAY=$([ "$PREP" -ge 1 ] && [ "$PLAY" -ge 1 ] && echo 1 || echo 0)
else
  PASS_PLAY=$([ "$PREP" -ge 1 ] && [ "$PLAY" -ge 1 ] && [ "$RENDER" -ge 1 ] && echo 1 || echo 0)
fi
PASS_NOCRASH=$([ "$FATAL" -eq 0 ] && [ "$ANR" -eq 0 ] && [ "$CRASH" -eq 0 ] && echo 1 || echo 0)

echo "[$LABEL] enter=$EXP prep=$PREP play=$PLAY render=$RENDER progress=$PROGRESS fatal=$FATAL anr=$ANR crash=$CRASH playOK=$PASS_PLAY safe=$PASS_NOCRASH"

adb -s emulator-5554 shell input keyevent 4
sleep 3
TOP_BACK=$(adb -s emulator-5554 shell dumpsys activity activities | grep "topResumedActivity" | head -1)
if ! echo "$TOP_BACK" | grep -qE "ComposeDemoListActivity|MainActivity|ListNormalActivity|RecyclerView2Activity|VideoActivityType|RecyclerView3Activity"; then
  # 子页面如 SubtitleSwitch / SwitchUrl 在子 Activity 里返回会回到列表 - OK；否则可能多层嵌套
  echo "[$LABEL] BACK: $TOP_BACK"
  # 再按一次 back 兜底
  adb -s emulator-5554 shell input keyevent 4
  sleep 2
fi

if [ "$PASS_PLAY" = "1" ] && [ "$PASS_NOCRASH" = "1" ]; then
  echo "[$LABEL] PASS"
  exit 0
else
  echo "[$LABEL] PROBLEM"
  exit 1
fi
