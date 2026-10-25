package id.ilham.cashflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Dipanggil AlarmManager saat pengingat jatuh tempo: tampilkan notifikasi lalu jadwalkan bulan berikutnya. */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ReminderScheduler.ACTION_FIRE.equals(intent.getAction())) return;
        String id = intent.getStringExtra(ReminderScheduler.EXTRA_ID);
        if (id == null) return;
        long firedFor = intent.getLongExtra(ReminderScheduler.EXTRA_WHEN, 0L);
        for (Reminder r : ReminderStore.load(context)) {
            if (!id.equals(r.id)) continue;
            Notifier.show(context, r);
            // jadwal berikutnya dihitung dari waktu yang dijadwalkan (atau sekarang bila lebih akhir) + 1 menit,
            // supaya alarm yang datang sedikit lebih awal tidak memicu notifikasi ganda
            long base = Math.max(System.currentTimeMillis(), firedFor);
            LocalDateTime after = LocalDateTime.ofInstant(Instant.ofEpochMilli(base), ZoneId.systemDefault()).plusMinutes(1);
            ReminderScheduler.scheduleNext(context, r, after);
            return;
        }
        // pengingat sudah dihapus/dimatikan: tidak melakukan apa pun
    }
}
