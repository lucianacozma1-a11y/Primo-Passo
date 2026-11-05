#!/bin/bash
# Prova automatica: installa l'app, la apre e cattura le schermate principali.
set -x
PKG=it.primopasso
ACT=$PKG/.MainActivity
mkdir -p smoke
adb logcat -c
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS || true

shot() { sleep "${2:-4}"; adb exec-out screencap -p > "smoke/$1.png"; }

tap_text() {
  adb shell uiautomator dump /sdcard/ui.xml > /dev/null
  adb pull /sdcard/ui.xml smoke/ui.xml > /dev/null
  python3 - "$1" <<'PY'
import re, sys, subprocess
t = sys.argv[1]
xml = open('smoke/ui.xml', encoding='utf-8').read()
for m in re.finditer(r'<node [^>]*>', xml):
    n = m.group(0)
    tx = re.search(r' text="([^"]*)"', n)
    if tx and tx.group(1).strip() and (tx.group(1) == t or t in tx.group(1)):
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        x = (int(b.group(1)) + int(b.group(3))) // 2
        y = (int(b.group(2)) + int(b.group(4))) // 2
        subprocess.run(['adb', 'shell', 'input', 'tap', str(x), str(y)])
        print('tap', t, x, y)
        break
else:
    print('NON TROVATO', t)
PY
}

sleep 25
tap_text "Wait"
adb shell am start -W -n $ACT
shot 01_benvenuto 6
tap_text "Wait"
tap_text "Iniziamo"
shot 02_intro_quiz 3
tap_text "Iniziamo"
shot 03_domanda1 3
tap_text "So cosa fare"
tap_text "Continua"
tap_text "Mi sembra una montagna"
tap_text "Ho paura di sbagliare"
shot 04_multi 2
tap_text "Continua"
tap_text "Quando qualcuno"
tap_text "Continua"
tap_text "Senso di colpa"
tap_text "Continua"
tap_text "Scorro il telefono"
tap_text "Continua"
tap_text "Lavoro"
tap_text "Continua"
tap_text "Tarda mattina"
tap_text "Circa cinque minuti"
tap_text "La musica"
tap_text "Un timer"
tap_text "Continua"
tap_text "Mi critico duramente"
tap_text "Spesso"
tap_text "A volte"
tap_text "Spesso"
tap_text "Il tempo per me"
tap_text "Continua"
tap_text "Più fiducia"
tap_text "Continua"
shot 06_ritratto 4
adb shell input swipe 540 1900 540 400 400
shot 07_ritratto_giu 2
tap_text "Vedi il tuo piano"
shot 07b_piano 3
adb shell input swipe 540 1900 540 500 400
shot 07c_piano_giu 2
tap_text "Continua"
shot 08a_promemoria 3
tap_text "Non ora"
shot 08_oggi 4
adb shell am start -W -n $ACT --ei open_tab 0 --es open_route start
shot 09_inizia 3
tap_text "Lavoro"
shot 10_inizia_lavoro 2
adb shell input swipe 540 1900 540 400 400
tap_text "Inizia ora"
shot 11_timer 6
tap_text "Ho fatto"
shot 12_iniziato 3
tap_text "Ho finito tutto"
shot 13_fatto 3
adb shell am start -W -n $ACT --ei open_tab 2 --es open_route lesson
shot 14_lezione 3
for t in 1 2 3 4 5 6; do
  adb shell am start -W -n $ACT --ei open_tab $t
  shot 2${t}_scheda 3
done
adb shell am start -W -n $ACT --ei open_tab 3 --es open_route kit --es kit k_ridicolo
shot 30_kit 4
adb shell am start -W -n $ACT --ei open_tab 0
sleep 2
adb shell input swipe 540 1900 540 600 400
shot 31_oggi_giu 2
adb shell am start -W -n $ACT --ei open_tab 0 --es open_route checkin
shot 32_checkin 3
adb shell am start -W -n $ACT --ei open_tab 5 --es open_route thought
tap_text "Non ce la farò mai"
tap_text "Continua"
shot 33_pensiero 3

adb shell pidof $PKG > smoke/pid.txt || echo "NON IN ESECUZIONE" > smoke/pid.txt
adb logcat -d > smoke/logcat.txt
adb logcat -d -b crash > smoke/crash.txt
echo "=== CRASH ==="
grep -A 25 "$PKG" smoke/crash.txt | head -60
echo "=== PID ==="
cat smoke/pid.txt
exit 0
