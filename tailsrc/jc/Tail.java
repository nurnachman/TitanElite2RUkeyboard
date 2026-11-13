package jc;
public class Tail {
    static final String TAG = "JC-TAIL";
    static boolean hebrewMode;
    static boolean inited;
    static Object conn;
    static long lastSig = Long.MIN_VALUE;
    static final String PREFS = "hebrew_layout";
    static final String PREF_HEBREW = "hebrew_mode";
    static final int META_ALT_ON = 0x02;
    static final int NON_TEXT_MODIFIERS = 0x02 | 0x04 | 0x08 | 0x1000 | 0x10000;
    static final int OTHER_SWITCH_MODIFIERS = 0x01 | 0x04 | 0x08 | 0x1000 | 0x10000;
    static {
        log("clinit");
    }

    // entry from HDKeyboardService.onKeyDown hook
    public static boolean onKey(Object src, Object ev) {
        try { return onKey0(src, ev); } catch (Throwable t) { log("E " + t); return false; }
    }
    // entry from h3/c.p() hook — same logic
    public static boolean p(Object src, Object ev) { return onKey(src, ev); }

    static boolean onKey0(Object src, Object ev) {
        if (ev == null) return false;
        Long et = (Long) call(ev, "getEventTime");
        long evt = et == null ? -1L : et.longValue();
        Integer act = (Integer) call(ev, "getAction");
        Integer rep = (Integer) call(ev, "getRepeatCount");
        Integer kco = (Integer) call(ev, "getKeyCode");
        if (act == null || rep == null || kco == null) return false;
        int action = act.intValue(), repeat = rep.intValue(), keyCode = kco.intValue();
        long sig = evt ^ (keyCode * 131L) ^ (action * 31L) ^ (repeat * 7L);
        if (sig == lastSig) return false;   // same logical event via cloned dispatch
        lastSig = sig;
        if (!inited) {
            inited = true;
            hebrewMode = readHebrewMode(src);
            setLang(src, "en");
            showLanguage(src);
        }

        Integer ms = (Integer) call(ev, "getMetaState");
        int meta = ms == null ? 0 : ms.intValue();
        if (keyCode == 62 && action == 0 && repeat == 0
                && (meta & META_ALT_ON) != 0 && (meta & OTHER_SWITCH_MODIFIERS) == 0) {
            hebrewMode = !hebrewMode;
            writeHebrewMode(src, hebrewMode);
            setLang(src, "en");
            showLanguage(src);
            return true;
        }

        if (hebrewMode && (meta & NON_TEXT_MODIFIERS) == 0) {
            char output = hebrewCharacter(keyCode, (meta & 0x1) != 0);
            if (output != 0) {
                if (action == 0 && repeat == 0) insertHebrew(src, output);
                return true;
            }
        }
        return false;
    }

    public static boolean longPress(Object service, int keyCode) {
        return false;
    }

    static char hebrewCharacter(int keyCode, boolean shifted) {
        switch (keyCode) {
            case 29: return '\u05E9'; // A
            case 30: return '\u05E0'; // B
            case 31: return '\u05D1'; // C
            case 32: return '\u05D2'; // D
            case 33: return '\u05E7'; // E
            case 34: return '\u05DB'; // F
            case 35: return '\u05E2'; // G
            case 36: return '\u05D9'; // H
            case 37: return '\u05DF'; // I
            case 38: return '\u05D7'; // J
            case 39: return '\u05DC'; // K
            case 40: return '\u05DA'; // L
            case 41: return '\u05E6'; // M
            case 42: return '\u05DE'; // N
            case 43: return '\u05DD'; // O
            case 44: return '\u05E4'; // P
            case 45: return shifted ? '?' : '/'; // Q
            case 46: return '\u05E8'; // R
            case 47: return '\u05D3'; // S
            case 48: return '\u05D0'; // T
            case 49: return '\u05D5'; // U
            case 50: return '\u05D4'; // V
            case 51: return shifted ? '\u05F4' : '\u05F3'; // W
            case 52: return '\u05E1'; // X
            case 53: return '\u05D8'; // Y
            case 54: return '\u05D6'; // Z
            case 55: return shifted ? '<' : '\u05EA'; // comma
            case 56: return shifted ? '>' : '\u05E5'; // period
            case 71: return shifted ? '}' : ']'; // left bracket
            case 72: return shifted ? '{' : '['; // right bracket
            case 74: return shifted ? ':' : '\u05E3'; // semicolon
            case 75: return shifted ? '"' : ','; // apostrophe
            case 76: return shifted ? '?' : '.'; // slash
            default: return 0;
        }
    }

    static void insertHebrew(Object service, char character) {
        Object connection = conn(service);
        if (connection == null) {
            log("cannot insert U+" + Integer.toHexString(character) + ": no InputConnection");
            return;
        }
        callA(connection, "finishComposingText", new Class[0], new Object[0]);
        Object result = callA(connection, "commitText",
            new Class[]{CharSequence.class, int.class},
            new Object[]{String.valueOf(character), Integer.valueOf(1)});
        if (!(result instanceof Boolean) || !((Boolean) result).booleanValue()) {
            log("commitText failed for U+" + Integer.toHexString(character));
        }
    }

    static boolean readHebrewMode(Object service) {
        Object preferences = callA(service, "getSharedPreferences",
            new Class[]{String.class, int.class}, new Object[]{PREFS, Integer.valueOf(0)});
        Object value = preferences == null ? null : callA(preferences, "getBoolean",
            new Class[]{String.class, boolean.class}, new Object[]{PREF_HEBREW, Boolean.TRUE});
        if (value instanceof Boolean) return ((Boolean) value).booleanValue();
        log("could not read saved language; defaulting to Hebrew");
        return true;
    }

    static void writeHebrewMode(Object service, boolean enabled) {
        Object preferences = callA(service, "getSharedPreferences",
            new Class[]{String.class, int.class}, new Object[]{PREFS, Integer.valueOf(0)});
        Object editor = preferences == null ? null : call(preferences, "edit");
        Object result = editor == null ? null : callA(editor, "putBoolean",
            new Class[]{String.class, boolean.class}, new Object[]{PREF_HEBREW, Boolean.valueOf(enabled)});
        if (result == null) {
            log("could not save language preference");
            return;
        }
        call(result, "apply");
    }

    static void showLanguage(Object service) {
        try {
            Class<?> context = Class.forName("android.content.Context");
            Class<?> toast = Class.forName("android.widget.Toast");
            Object message = hebrewMode ? "\u05E2\u05D1\u05E8\u05D9\u05EA" : "English";
            Object notification = toast.getMethod("makeText", context, CharSequence.class, int.class)
                .invoke(null, service, message, Integer.valueOf(0));
            call(notification, "show");
        } catch (Throwable t) {
            log("language indicator failed: " + t);
        }
    }

    // switch Kika language via its own pipeline: enable (y) -> select (q) -> apply (o)
    static void setLang(Object service, String wantPrefix) {
        try {
            Object m3b = getField(service, "h");
            if (m3b == null) { log("setLang no m3b"); return; }
            Object prov = getField(m3b, "e");
            if (prov == null) { log("setLang no prov"); return; }
            Object lang = pickLangObj(prov, wantPrefix);
            if (lang == null) { log("setLang notfound " + wantPrefix); return; }
            Class<?> bCls = lang.getClass();
            Object loc = call(lang, "c");
            if (loc == null) { log("setLang noloc"); return; }
            callA(prov, "y", new Class[]{java.util.Locale.class, boolean.class}, new Object[]{loc, Boolean.TRUE});
            callA(prov, "q", new Class[]{bCls, boolean.class}, new Object[]{lang, Boolean.TRUE});
            callA(m3b, "U", new Class[0], new Object[0]);   // rebuild consumer maps
            try {
                Object h3c = Class.forName("h3.c").getMethod("h").invoke(null);   // latin-family consumer: set its language
                callA(h3c, "t", new Class[]{bCls}, new Object[]{lang});
            } catch (Throwable t) { log("t E " + t); }
            try {
                callA(service, "o", new Class[]{bCls, boolean.class, boolean.class}, new Object[]{lang, Boolean.FALSE, Boolean.TRUE});
            } catch (Throwable t) { log("o E " + t); }
            Object eng = getField(service, "e");   // x2.b engine: reload layout for locale
            if (eng != null) {
                try { callA(eng, "k", new Class[]{java.util.Locale.class}, new Object[]{loc}); log("eng k ok"); }
                catch (Throwable t) { log("eng E " + t); }
            }
            try {   // re-run per-input init so current field picks new language (same as refocus)
                Object ei = call(service, "getCurrentInputEditorInfo");
                if (ei != null) {
                    callA(service, "onStartInput", new Class[]{Class.forName("android.view.inputmethod.EditorInfo"), boolean.class}, new Object[]{ei, Boolean.TRUE});
                    log("reinit ok");
                }
            } catch (Throwable t) { log("reinit E " + t); }
            log("setLang done " + wantPrefix);
        } catch (Throwable t) { log("setLang E " + t); }
    }

    static Object pickLangObj(Object prov, String wantPrefix) {
        try {
            java.lang.reflect.Field gf = prov.getClass().getDeclaredField("g");
            gf.setAccessible(true);
            Object[] all = (Object[]) gf.get(null);
            if (all == null) return null;
            for (Object lang : all) {
                Object loc = lang == null ? null : call(lang, "c");
                Object tag = loc == null ? null : call(loc, "toLanguageTag");
                if (tag != null && tag.toString().startsWith(wantPrefix)) return lang;
            }
        } catch (Throwable t) { log("pick E " + t); }
        return null;
    }

    static Object getField(Object o, String n) {
        try { java.lang.reflect.Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o); }
        catch (Throwable t) { return null; }
    }

    static Object conn(Object src) {
        conn = call(src, "getCurrentInputConnection");
        if (conn == null && !ensureConn(src)) return null;
        return conn;
    }
    static boolean ensureConn(Object h) {
        if (conn != null) return true;
        try {
            java.lang.reflect.Field[] fs = h.getClass().getDeclaredFields();
            for (java.lang.reflect.Field f : fs) {
                if (f.getType().getName().endsWith("InputConnection")) { f.setAccessible(true); conn = f.get(h); if (conn != null) return true; }
            }
            for (java.lang.reflect.Field f : fs) {
                f.setAccessible(true);
                Object v = f.get(h);
                if (v == null) continue;
                if (v.getClass().getName().contains("Render")) {
                    for (java.lang.reflect.Field g : v.getClass().getDeclaredFields()) {
                        if (g.getType().getName().endsWith("InputConnection")) { g.setAccessible(true); conn = g.get(v); if (conn != null) return true; }
                    }
                }
            }
        } catch (Throwable t) {}
        return conn != null;
    }
    static void log(String s) {
        System.err.println(TAG + " " + s);
        try { Class.forName("android.util.Log").getMethod("d", String.class, String.class).invoke(null, TAG, s); } catch (Throwable t) {}
    }
    static Object call(Object o, String m) { try { return o.getClass().getMethod(m).invoke(o); } catch (Throwable t) { return null; } }
    static Object callA(Object o, String m, Class<?>[] cs, Object[] as) { try { return o.getClass().getMethod(m, cs).invoke(o, as); } catch (Throwable t) { return null; } }
}
