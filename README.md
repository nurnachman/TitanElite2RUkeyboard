# Titan 2 Elite Hebrew Keyboard

Hebrew (Israel) physical-key input for the Unihertz Titan 2 Elite, implemented
as a clone of the stock Kika/IQQI IME. The stock IME remains installed and can
be restored from Android's keyboard settings.

## What this branch does

- Maps Titan 2 Elite physical QWERTY key events to the standard Israeli Hebrew
  keyboard layout and inserts Unicode text through the active `InputConnection`.
- Maps all 22 Hebrew letters and the five final forms, including the dedicated
  `,` → `ת` (`U+05EA`) and `.` → `ץ` (`U+05E5`) mappings.
- Starts in Hebrew; `Alt+Space` toggles Hebrew/English and saves the choice.
  A short language indicator is shown when the mode is initialized or changed.
- Lets unmapped keys, editing/navigation shortcuts, and modified symbol layers
  continue through Kika/Android rather than consuming every key.
- Leaves Hebrew/English ordering to Android's bidirectional text engine; it does
  not reverse strings or insert invisible bidi controls.

Hebrew is emitted directly from the physical key event handler. It does not
depend on a Hebrew locale or Hebrew language model being present in Kika. The
stock Kika IME continues to handle English, suggestions, the on-screen
keyboard, and native editing behavior.

## Layout

The physical key position determines the output. Final forms have independent
keys; they are not substituted based on word position.

```text
Q=/  W=׳  E=ק  R=ר  T=א  Y=ט  U=ו  I=ן  O=ם  P=פ  [=]  ]=[
 A=ש  S=ד  D=ג  F=כ  G=ע  H=י  J=ח  K=ל  L=ך  ;=ף  '=,
  Z=ז  X=ס  C=ב  V=ה  B=נ  N=מ  M=צ  ,=ת  .=ץ  /=.
```

Shift does not change Hebrew letters. It selects the shifted punctuation for
the punctuation keys (including `Q` `/` → `?` and `W` geresh → gershayim).
Alt, Sym/Fn, Ctrl, and system/navigation keys are passed through. The full
mapping is in [reference/titan_hebrew.json](reference/titan_hebrew.json).

## Build and install

Prerequisites: a Titan 2 Elite with USB debugging, `adb`, Docker, and a
connected device. The scripts pull the stock IME from that device.

```bash
./scripts/decode-patch.sh
./scripts/build.sh
./scripts/install.sh
```

The clone is installed as `com.iqqijni.bbkeyboardhe` beside the stock IME.
After a reboot, the ROM may restore the stock IME as default; select **Kika
עברית** again in Android's input-method settings. Uninstalling this clone does
not remove the stock keyboard.

Run the host-side mapping checks with:

```bash
javac -encoding UTF-8 -d /tmp/titan-hebrew-tests tailsrc/jc/Tail.java tests/KeyboardLayoutTest.java
java -cp /tmp/titan-hebrew-tests jc.KeyboardLayoutTest
```

## Hardware validation still required

The mapping and Java handler are covered by host-side tests, but this
environment has no Titan 2 Elite attached. The branch is therefore not
considered device-validated. Before treating it as complete, verify on actual
hardware:

- The device emits Android keycodes for every expected physical key, especially
  comma, period, brackets, semicolon, and apostrophe.
- The comma produces `ת` and period produces `ץ` without duplicate commits.
- All 22 letters and five final forms, punctuation, and Shift/Alt/Sym layers.
- Hebrew/English switching, preference persistence, mixed RTL/LTR text,
  cursor/selection/deletion, and behavior in multiple apps after reboot.
- Physical navigation and the touchpad. The ROM currently allowlists flick
  typing for the stock IME package only, so the renamed clone cannot preserve
  that firmware feature without a separate OEM/root-level change.

The current patch preserves the stock navigation and editing paths by
intercepting only mapped unmodified text keys in Hebrew mode. It does not
disable the touchscreen, remap system controls, or require root. If an actual
device reports different keycodes, update the mapping only after recording the
hardware event stream and validating the new positions.

## Reference material

- [Hebrew mapping specification](reference/titan_hebrew.json)
- [Stock Titan 2 Elite keylayout/keychar files](reference/TitanKey.kl)
- [Kika engine notes](reference/kika-internals.md)
- [Flick-typing firmware limitation](reference/flick-typing-notes.md)
- [Original Russian-layout findings](reference/stock-layout-map.md)

## License

MIT — see [LICENSE](LICENSE). The patched APK derives from Unihertz's
preinstalled IQQI/Kika IME and is intended for use on the owner's device.
