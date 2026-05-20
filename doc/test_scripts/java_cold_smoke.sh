#!/bin/bash
set -u

ADB="adb -s emulator-5554"
PKG="com.example.gsyvideoplayer"
DUMP="/tmp/gsy_ui.xml"

reset_app() {
  $ADB shell am force-stop "$PKG" >/dev/null 2>&1
  sleep 1
  $ADB shell am start -n "${PKG}/.MainActivity" >/dev/null 2>&1
  sleep 2
}

dump_ui() {
  $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  $ADB pull /sdcard/ui.xml "$DUMP" >/dev/null 2>&1
}

scroll_to_text() {
  local target="$1"
  local upper="$(python3 -c 'import sys; print(sys.argv[1].upper())' "$target")"
  for _ in 1 2 3 4 5 6 7 8 9 10 11 12; do
    dump_ui
    if grep -qE "text=\"(${target}|${upper})\"" "$DUMP"; then return 0; fi
    $ADB shell input swipe 540 1800 540 600 350
    sleep 0.5
  done
  return 1
}

text_center() {
  python3 - "$1" <<'PY'
import re, sys
target = sys.argv[1]
xml = open('/tmp/gsy_ui.xml').read()
m = re.search(rf'text="{re.escape(target)}"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
if not m:
    m = re.search(rf'text="{re.escape(target.upper())}"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
if m:
    x = (int(m.group(1)) + int(m.group(3))) // 2
    y = (int(m.group(2)) + int(m.group(4))) // 2
    print(f"{x} {y}")
PY
}

run_one() {
  local entry="$1"
  reset_app
  $ADB logcat -c
  if ! scroll_to_text "$entry"; then echo "[SKIP] $entry (not found in MainActivity)"; return; fi
  local xy
  xy=$(text_center "$entry")
  if [ -z "$xy" ]; then echo "[SKIP] $entry (no center)"; return; fi
  $ADB shell input tap $xy
  sleep 4
  local crash
  crash=$($ADB logcat -d 2>/dev/null | grep -cE "FATAL EXCEPTION|ANR in com.example")
  local top
  top=$($ADB shell dumpsys activity top 2>/dev/null | grep -oE "ACTIVITY com.example[^ ]+" | head -1)
  if [ "$crash" -ge 1 ]; then
    echo "[FAIL] $entry crash=$crash top=$top"
  elif [[ "$top" == *MainActivity* ]] || [ -z "$top" ]; then
    echo "[FAIL] $entry never left MainActivity (top=$top)"
  else
    echo "[PASS] $entry top=$top"
  fi
}

ENTRIES=(
  "简单播放"
  "打开Video"
  "带控制DEMO"
  "完成保留最后一帧"
  "透明"
  "无UI界面"
  "滤镜"
  "带广告"
  "带广告2"
  "无缝切换"
  "List列表"
  "List全屏和小窗口列表"
  "ViewPager2列表"
  "ViewPager Demo"
  "recycler列表"
  "自动recycler列表"
  "recycler全屏和小窗口列表"
  "Detail模式"
  "Detail列表模式，切换下一集"
  "TV机顶盒播放器"
  "Web detail模式"
  "弹幕demo"
  "Fragment下使用"
  "多类型模式"
  "输入url"
  "联动detail"
  "悬浮窗口"
  "多任务支持"
  "列表详情切换"
  "列表带广告模式"
  "硬解码支持"
  "缓存下载支持"
  "Exo特有缓存下载支持"
  "普通 Activity"
  "自定义Exo"
  "Exo自适应清晰度"
  "自定义Exo支持字幕"
  "通用字幕非Exo"
  "音频"
)

main() {
  echo ">>>>> Java cold-smoke regression: ${#ENTRIES[@]} entries"
  for e in "${ENTRIES[@]}"; do
    run_one "$e"
  done
  echo ">>>>> cold-smoke done"
}

main "$@"
