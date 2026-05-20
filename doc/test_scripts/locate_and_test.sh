#!/bin/bash
# Usage: locate_and_test.sh <ExactTitle> <ExpectedActivityClass> <Label> [audio]
TITLE="$1"; EXP="$2"; LABEL="$3"; MODE="${4:-video}"

# 滚到顶
for i in 1 2 3 4 5 6 7 8; do
  adb -s emulator-5554 shell input swipe 540 600 540 2200 400 >/dev/null 2>&1
  sleep 1
done
sleep 1

for page in 1 2 3 4 5 6 7 8 9 10; do
  adb -s emulator-5554 shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  adb -s emulator-5554 pull /sdcard/ui.xml /tmp/ui.xml >/dev/null 2>&1
  COORDS=$(python3 /tmp/find_xy.py /tmp/ui.xml "$TITLE")
  if [ -n "$COORDS" ]; then
    X=$(echo $COORDS | awk '{print $1}')
    Y=$(echo $COORDS | awk '{print $2}')
    bash /tmp/real_test.sh $X $Y "$EXP" "$LABEL" "$MODE"
    exit $?
  fi
  adb -s emulator-5554 shell input swipe 540 1900 540 700 500 >/dev/null 2>&1
  sleep 2
done
echo "[$LABEL] NOT FOUND"
exit 1
