#!/bin/bash
set -u

ADB="adb -s emulator-5554"
PKG="com.example.gsyvideoplayer"
DUMP="/tmp/gsy_ui.xml"
LOG_TAG="GSYVideoPlayer:"

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
  for _ in 1 2 3 4 5 6 7 8 9 10; do
    dump_ui
    if grep -q "text=\"${target}\"" "$DUMP"; then return 0; fi
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
if m:
    x = (int(m.group(1)) + int(m.group(3))) // 2
    y = (int(m.group(2)) + int(m.group(4))) // 2
    print(f"{x} {y}")
PY
}

id_center() {
  python3 - "$1" <<'PY'
import re, sys
target = sys.argv[1]
xml = open('/tmp/gsy_ui.xml').read()
m = re.search(rf'resource-id="com\.example\.gsyvideoplayer:id/{re.escape(target)}"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
if m:
    x = (int(m.group(1)) + int(m.group(3))) // 2
    y = (int(m.group(2)) + int(m.group(4))) // 2
    print(f"{x} {y}")
PY
}

id_bounds() {
  python3 - "$1" <<'PY'
import re, sys
target = sys.argv[1]
xml = open('/tmp/gsy_ui.xml').read()
m = re.search(rf'resource-id="com\.example\.gsyvideoplayer:id/{re.escape(target)}"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
if m:
    print(f"{m.group(1)} {m.group(2)} {m.group(3)} {m.group(4)}")
PY
}

tap_text() {
  local target="$1"
  local xy
  xy=$(text_center "$target")
  if [ -z "$xy" ]; then return 1; fi
  $ADB shell input tap $xy
}

show_controls() {
  $ADB shell input tap 540 545
  sleep 0.6
}

wait_for_player() {
  local timeout=${1:-10}
  for _ in $(seq 1 $timeout); do
    dump_ui
    if grep -q "com.example.gsyvideoplayer:id/surface_container" "$DUMP"; then return 0; fi
    sleep 1
  done
  return 1
}

start_play() {
  show_controls
  $ADB shell input tap 540 545
  sleep 3
}

assert_logcat() {
  local pattern="$1"
  local label="$2"
  local hits
  hits=$($ADB logcat -d 2>/dev/null | grep -cE "$pattern")
  if [ "$hits" -ge 1 ]; then
    echo "  [PASS] $label (hits=$hits)"
    return 0
  else
    echo "  [FAIL] $label (no match for: $pattern)"
    return 1
  fi
}

test_A_play_pause() {
  echo "===== A: play / pause ====="
  $ADB logcat -c
  show_controls
  $ADB shell input tap 540 545
  sleep 2
  assert_logcat "onClickStop|CURRENT_STATE_PAUSE" "A1 pause"
  $ADB logcat -c
  show_controls
  $ADB shell input tap 540 545
  sleep 2
  assert_logcat "onClickResume|CURRENT_STATE_PLAYING" "A2 resume"
}

test_B_seek() {
  echo "===== B: drag progress seekbar ====="
  show_controls
  dump_ui
  local b
  b=$(id_bounds progress)
  if [ -z "$b" ]; then echo "  [FAIL] B no progress bounds"; return 1; fi
  read x1 y1 x2 y2 <<<"$b"
  local cy=$(( (y1 + y2) / 2 ))
  local sx=$(( x1 + (x2 - x1) / 5 ))
  local ex=$(( x1 + (x2 - x1) * 4 / 5 ))
  $ADB logcat -c
  $ADB shell input swipe $sx $cy $ex $cy 1500
  sleep 2
  assert_logcat "onClickSeekbar" "B1 onClickSeekbar"
  assert_logcat "onSeekComplete" "B2 onSeekComplete"
  show_controls
  dump_ui
  b=$(id_bounds progress)
  if [ -n "$b" ]; then
    read x1 y1 x2 y2 <<<"$b"
    cy=$(( (y1 + y2) / 2 ))
    sx=$(( x1 + (x2 - x1) * 4 / 5 ))
    ex=$(( x1 + (x2 - x1) / 5 ))
    $ADB shell input swipe $sx $cy $ex $cy 1200
    sleep 1
  fi
}

test_C_fullscreen() {
  echo "===== C: fullscreen toggle ====="
  local fxy=""
  for _ in 1 2 3 4; do
    show_controls
    dump_ui
    fxy=$(id_center fullscreen)
    if [ -n "$fxy" ]; then break; fi
    sleep 0.5
  done
  if [ -z "$fxy" ]; then echo "  [FAIL] C no fullscreen"; return 1; fi
  $ADB logcat -c
  $ADB shell input tap $fxy
  sleep 4
  assert_logcat "orientation change.*DetailPlayer|conditionName=orientation" "C1 enter fullscreen"
  $ADB shell input keyevent KEYCODE_BACK
  sleep 2
}

test_D_change_core() {
  echo "===== D: change core (IJK -> EXO -> System -> IJK) ====="
  reset_app
  scroll_to_text "IJK 内核" || scroll_to_text "EXO 内核" || scroll_to_text "系统 内核" || { echo "  [FAIL] no kernel button"; return 1; }
  local kxy
  for current in "IJK 内核" "EXO 内核" "系统 内核"; do
    dump_ui
    kxy=$(text_center "$current")
    if [ -n "$kxy" ]; then
      echo "  [INFO] current=$current center=$kxy"
      break
    fi
  done
  $ADB shell input tap $kxy
  sleep 1
  dump_ui
  local now1
  now1=$(grep -oE 'text="[^"]*内核[^"]*"' "$DUMP" | head -1)
  echo "  [INFO] after click 1 -> $now1"
  $ADB shell input tap $kxy
  sleep 1
  dump_ui
  local now2
  now2=$(grep -oE 'text="[^"]*内核[^"]*"' "$DUMP" | head -1)
  echo "  [INFO] after click 2 -> $now2"
  $ADB shell input tap $kxy
  sleep 1
  dump_ui
  local now3
  now3=$(grep -oE 'text="[^"]*内核[^"]*"' "$DUMP" | head -1)
  echo "  [INFO] after click 3 -> $now3"
  if [ -n "$now1" ] && [ -n "$now2" ] && [ -n "$now3" ] && [ "$now1" != "$now2" ] && [ "$now2" != "$now3" ]; then
    echo "  [PASS] D core 3-state cycle"
    return 0
  else
    echo "  [FAIL] D core cycle ($now1 / $now2 / $now3)"
    return 1
  fi
}

enter_detail_player() {
  local entry_text="${1:-DETAIL模式}"
  local upper="$(python3 -c "import sys; print(sys.argv[1].upper())" "$entry_text")"
  local lower="$(python3 -c "import sys; print(sys.argv[1].lower())" "$entry_text")"
  reset_app
  scroll_to_text "$upper" || scroll_to_text "$entry_text" || scroll_to_text "$lower" || { echo "no $entry_text entry"; exit 1; }
  tap_text "$upper" || tap_text "$entry_text" || tap_text "$lower"
  sleep 3
  wait_for_player 10 || { echo "player not loaded"; exit 1; }
  start_play
}

main() {
  local entry="${1:-DETAIL模式}"
  echo ">>>>> J-round Java basic capability regression"
  echo ">>>>> entry: $entry"
  echo ">>>>> device: $($ADB shell getprop ro.product.model | tr -d '\r') / Android $($ADB shell getprop ro.build.version.release | tr -d '\r')"
  enter_detail_player "$entry"
  test_A_play_pause
  test_B_seek
  test_C_fullscreen
  test_D_change_core
  echo ">>>>> done [$entry]"
}

main "$@"
