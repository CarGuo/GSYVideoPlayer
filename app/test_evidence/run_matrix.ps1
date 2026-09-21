param(
    [string]$Component = "xml",   # xml | compose
    [string]$OutDir = "app/test_evidence/matrix"
)

$pkg = "com.example.gsyvideoplayer"
if ($Component -eq "xml") {
    $activity = "$pkg/.FoldDetailActivity"
} else {
    $activity = "$pkg/.compose.host.FoldComposeActivity"
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

# posture: 0 flat / 1 book / 2 tabletop ; rotation: 0 landscape, 1 portrait
$postures = @(
    @{ id = 0; name = "flat" },
    @{ id = 1; name = "book" },
    @{ id = 2; name = "tabletop" }
)
$rotations = @(
    @{ id = 0; name = "land" },
    @{ id = 1; name = "port" }
)

$results = @()

function Get-FullscreenBtn {
    param(
        [int]$Retries = 3,
        [string]$RotName = "land"
    )
    for ($i = 0; $i -lt $Retries; $i++) {
        adb shell uiautomator dump /sdcard/m.xml | Out-Null
        adb pull /sdcard/m.xml "$OutDir/tmp.xml" | Out-Null
        $c = Get-Content "$OutDir/tmp.xml" -Raw
        foreach ($n in ([regex]::Matches($c, '<node[^>]*>') | ForEach-Object { $_.Value })) {
            $isBtn = if ($Component -eq "compose") {
                $n -match 'text="全屏"'
            } else {
                $n -match ':id/fullscreen'
            }
            if ($isBtn -and $n -match 'bounds="\[([0-9-]+),([0-9-]+)\]\[([0-9-]+),([0-9-]+)\]"') {
                $cx = [int](([int]$Matches[1] + [int]$Matches[3]) / 2)
                $cy = [int](([int]$Matches[2] + [int]$Matches[4]) / 2)
                return @($cx, $cy)
            }
        }
        # 控件可能处于隐藏态：轻点播放器后重试
        if ($i -lt $Retries - 1) {
            if ($RotName -eq "land") { adb shell input tap 500 900 } else { adb shell input tap 500 500 }
            Start-Sleep -Milliseconds 700
        }
    }
    return $null
}

function Test-FullPresent {
    adb shell uiautomator dump /sdcard/mf.xml | Out-Null
    adb pull /sdcard/mf.xml "$OutDir/tmpf.xml" | Out-Null
    return ((Get-Content "$OutDir/tmpf.xml" -Raw) -match ':id/full_id')
}

foreach ($p in $postures) {
    foreach ($r in $rotations) {
        $tag = "$Component`_" + $p.name + "_" + $r.name

        adb shell am force-stop $pkg
        adb shell settings put system accelerometer_rotation 0
        adb shell settings put system user_rotation $r.id
        Start-Sleep -Milliseconds 800
        adb shell am start -n $activity --ei extra_posture $p.id | Out-Null
        Start-Sleep -Seconds 4

        # 非全屏截图
        adb shell screencap -p /sdcard/n.png
        adb pull /sdcard/n.png "$OutDir/$tag`_normal.png" | Out-Null

        $btn = Get-FullscreenBtn -RotName $r.name -Retries 2
        $entered = $false
        for ($attempt = 0; $attempt -lt 3 -and -not $entered; $attempt++) {
            # 每轮重新取按钮，规避控件在点击前自动隐藏导致点空
            $b = if ($attempt -eq 0) { $btn } else { Get-FullscreenBtn -RotName $r.name -Retries 2 }
            if ($b -eq $null) {
                Start-Sleep -Milliseconds 400
                continue
            }
            adb shell input tap $b[0] $b[1]
            Start-Sleep -Milliseconds 1400
            $entered = Test-FullPresent
        }
        if ($entered) {
            adb shell screencap -p /sdcard/f.png
            adb pull /sdcard/f.png "$OutDir/$tag`_full.png" | Out-Null
        }

        $results += [pscustomobject]@{
            combo = $tag
            fullscreenEntered = $entered
        }
        Write-Output ("{0,-28} fullscreen={1}" -f $tag, $entered)

        # 退出全屏清理
        if ($entered) { adb shell input keyevent 4; Start-Sleep -Milliseconds 800 }
    }
}

Write-Output "`n===== SUMMARY ($Component) ====="
$results | Format-Table -AutoSize
