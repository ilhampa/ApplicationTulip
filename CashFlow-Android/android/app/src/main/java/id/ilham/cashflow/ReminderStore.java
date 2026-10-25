package id.ilham.cashflow;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

/** Menyimpan daftar pengingat terakhir agar alarm bisa dibuat ulang setelah perangkat restart. */
final class ReminderStore {
    private static final String PREFS = "cashflow_reminders";
    private static final String KEY = "list";

    private ReminderStore() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static List<Reminder> load(Context c) {
        return Reminder.parseList(prefs(c).getString(KEY, "[]"));
    }

    static void save(Context c, List<Reminder> list) {
        prefs(c).edit().putString(KEY, Reminder.toJson(list)).apply();
    }
}
