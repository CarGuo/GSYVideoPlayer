#!/bin/bash
# Java demo real-play test:
#   - clear logcat
#   - tap (X,Y), wait 12s (Java demo 起播比 Compose 慢些)
#   - assert topResumed == EXP
#   - assert logcat real signals
#   - back, wait 3s
# Usage: java_test.sh <X> <Y> <ExpectedActivityClass> <Label>
X=$1; Y=$2; EXP=$3; LABEL=$4

adb -s emulator-5554 logcat -c >/dev/null 2>&1
adb -s emulator-5554 shell input tap $X $Y
sleep 4
# Java demo 通常需手动按播放按钮，先 dump 找按钮
adb -s emulator-5554 shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
adb -s emulator-5554 pull /sdcard/ui.xml /tmp/ui_java.xml >/dev/null 2>&1

TOP=$(adb -s emulator-5554 shell dumpsys activity activities | grep "topResumedActivity" | head -1)
if ! echo "$TOP" | grep -q "$EXP"; then
  echo "[$LABEL] FAIL ENTER: $TOP"
  adb -s emulator-5554 shell input keyevent 4 ; sleep 2
  adb -s emulator-5554 shell input keyevent 4 ; sleep 2
  exit 1
fi

# 找播放按钮并点击（startBtn = res 'start'）
PLAY_XY=$(python3 -c "
import re
xml = open('/tmp/ui_java.xml').read()
# 找 resource-id 含 start 的节点
m = re.search(r'resource-id=\"[^\"]*start\"[^>]*?bounds=\"\[(\d+),(\d+)\]\[(\d+),(\d+)\]\"', xml)
if m:
    x = (int(m.group(1)) + int(m.group(3))) // 2
    y = (int(m.group(2)) + int(m.group(4))) // 2
    print(f'{x} {y}')
")
if [ -n "$PLAY_XY" ]; then
  echo "[$LABEL] tap start btn: $PLAY_XY"
  adb -s emulator-5554 shell input tap $PLAY_XY
fi
sleep 12

LOG=$(adb -s emulator-5554 logcat -d 2>/dev/null)
PREP=$(echo "$LOG" | grep -c "onPrepared")
PLAY=$(echo "$LOG" | grep -c "CURRENT_STATE_PLAYING")
RENDER=$(echo "$LOG" | grep -cE "MEDIA_INFO_VIDEO_RENDERING_START|videoSizeChanged|videoWidth:")
NET=$(echo "$LOG" | grep -c "Net speed:")
FATAL=$(echo "$LOG" | grep -c "FATAL EXCEPTION")
ANR=$(echo "$LOG" | grep -c "ANR in")
CRASH=$(adb -s emulator-5554 logcat -d -b crash 2>/dev/null | grep -c "FATAL EXCEPTION")

echo "[$LABEL] prep=$PREP play=$PLAY render=$RENDER net=$NET fatal=$FATAL anr=$ANR crash=$CRASH"

PASS_PLAY=$([ "$PLAY" -ge 1 ] && [ "$RENDER" -ge 1 ] && [ "$NET" -ge 1 ] && echo 1 || echo 0)
PASS_NOCRASH=$([ "$FATAL" -eq 0 ] && [ "$ANR" -eq 0 ] && [ "$CRASH" -eq 0 ] && echo 1 || echo 0)

adb -s emulator-5554 shell input keyevent 4
sleep 3
adb -s emulator-5554 shell input keyevent 4
sleep 2

if [ "$PASS_PLAY" = "1" ] && [ "$PASS_NOCRASH" = "1" ]; then
  echo "[$LABEL] PASS"
  exit 0
else
  echo "[$LABEL] PROBLEM"
  exit 1
fi
