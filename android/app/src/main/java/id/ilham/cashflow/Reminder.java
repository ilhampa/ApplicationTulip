package id.ilham.cashflow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Satu pengingat kantong (dikirim dari aplikasi web lewat AndroidBridge.syncReminders). */
final class Reminder {
    final String id;
    final String title;
    final String body;
    final int day;
    final int hour;
    final int minute;

    Reminder(String id, String title, String body, int day, int hour, int minute) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    /** Mem-parse daftar JSON; entri yang tidak valid dilewati. Tidak pernah melempar exception. */
    static List<Reminder> parseList(String json) {
        List<Reminder> out = new ArrayList<>();
        if (json == null || json.isEmpty()) return out;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String id = o.optString("id", "");
                int[] hm = ReminderMath.parseTime(o.optString("time", ""));
                int day = o.optInt("day", 0);
                if (id.isEmpty() || hm == null || day < 1 || day > 31) continue;
                String title = o.optString("title", "CashFlow");
                String body = o.optString("body", "Waktunya mengisi kantong");
                out.add(new Reminder(id, title, body, day, hm[0], hm[1]));
            }
        } catch (JSONException e) {
            // JSON rusak: abaikan seluruhnya
            return new ArrayList<>();
        }
        return out;
    }

    static String toJson(List<Reminder> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Reminder r : list) {
                JSONObject o = new JSONObject();
                o.put("id", r.id);
                o.put("title", r.title);
                o.put("body", r.body);
                o.put("day", r.day);
                o.put("time", String.format(java.util.Locale.US, "%02d:%02d", r.hour, r.minute));
                arr.put(o);
            }
        } catch (JSONException e) {
            return "[]";
        }
        return arr.toString();
    }
}
