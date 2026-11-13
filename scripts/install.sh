#!/usr/bin/env bash
# Step 3: install the clone on the device and bind it.
# Hebrew is the default; Alt+Space switches Hebrew and English.
set -euo pipefail
cd "$(dirname "$0")/.."
APK=dist/kika-he.apk
IME=com.iqqijni.bbkeyboardhe/com.iqqijni.bbkeyboard.keyboard_service.view.HDKeyboardService

[ -f "$APK" ] || { echo "no $APK — run scripts/build.sh first"; exit 1; }
adb uninstall com.iqqijni.bbkeyboardhe >/dev/null 2>&1 || true
adb install --no-incremental "$APK"

# cold-start race: launch the settings activity once, then bind
adb shell am start -n com.iqqijni.bbkeyboardhe/com.iqqijni.bbkeyboard.imesettings.ImeMainSettingsActivity >/dev/null 2>&1 || true
sleep 5
adb shell ime enable "$IME" >/dev/null
adb shell ime set "$IME"

cat <<'EOF'

Installed and selected. Notes:
- Hebrew is the default. Alt+Space toggles Hebrew/English; the selected mode is saved.
- Ctrl+Space shows/hides the on-screen keyboard (stock behavior).
- After a phone reboot the ROM resets the default IME to the stock Kika —
  reselect "Kika עברית" in Settings (or rerun this script).
EOF
