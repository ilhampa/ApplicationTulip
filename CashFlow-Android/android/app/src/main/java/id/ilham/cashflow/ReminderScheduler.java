package id.ilham.cashflow;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/** Menjadwalkan alarm bulanan per pengingat memakai AlarmManager (tepat waktu, tetap jalan saat Doze). */
final class ReminderScheduler {
    static final String ACTION_FIRE = "id.ilham.cashflow.REMINDER_FIRE";
    static final String EXTRA_ID = "id";
    static final String EXTRA_WHEN = "when";

    private ReminderScheduler() {}

    /** Dipanggil saat aplikasi web mengirim daftar terbaru: batalkan yang lama, simpan, jadwalkan yang baru. */
    static synchronized void sync(Context ctx, String json) {
        List<Reminder> fresh = Reminder.parseList(json);
        for (Reminder old : ReminderStore.load(ctx)) cancel(ctx, old.id);
        ReminderStore.save(ctx, fresh);
        for (Reminder r : fresh) scheduleNext(ctx, r, LocalDateTime.now());
    }

    /** Dipanggil setelah boot / update aplikasi / perubahan jam atau zona waktu. */
    static synchronized void rescheduleAll(Context ctx) {
        for (Reminder r : ReminderStore.load(ctx)) scheduleNext(ctx, r, LocalDateTime.now());
    }

    /** Jadwalkan kemunculan berikutnya yang SETELAH 'after'. */
    static void scheduleNext(Context ctx, Reminder r, LocalDateTime after) {
        LocalDateTime next = ReminderMath.nextTrigger(r.day, r.hour, r.minute, after);
        long when = ReminderMath.toEpochMillis(next, ZoneId.systemDefault());
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pending(ctx, r.id, when);
        try {
            boolean exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms();
            if (exact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
            }
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
        }
    }

    static void cancel(Context ctx, String id) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pending(ctx, id, 0L);
        am.cancel(pi);
        pi.cancel();
    }

    /** PendingIntent yang identik untuk id yang sama (dibedakan lewat data Uri) agar bisa dibatalkan/diganti. */
    private static PendingIntent pending(Context ctx, String id, long when) {
        Intent i = new Intent(ctx, ReminderReceiver.class);
        i.setAction(ACTION_FIRE);
        i.setData(Uri.parse("cashflow://reminder/" + Uri.encode(id)));
        i.putExtra(EXTRA_ID, id);
        i.putExtra(EXTRA_WHEN, when);
        return PendingIntent.getBroadcast(ctx, id.hashCode(), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
