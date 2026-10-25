package id.ilham.cashflow;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/** Membuat saluran dan menampilkan notifikasi pengingat kantong. */
final class Notifier {
    static final String CHANNEL_ID = "pengingat_kantong";

    private Notifier() {}

    static void ensureChannel(Context ctx) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Pengingat kantong", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Pengingat mengisi kantong keuangan sesuai tanggal dan jam yang diatur");
        nm.createNotificationChannel(ch);
    }

    static void show(Context ctx, Reminder r) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        ensureChannel(ctx);
        Intent open = new Intent(ctx, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(ctx, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Versi publik (layar kunci): tanpa nominal
        Notification pub = new Notification.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_bell)
                .setContentTitle("CashFlow")
                .setContentText("Pengingat kantong")
                .build();

        Notification n = new Notification.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_bell)
                .setContentTitle(r.title)
                .setContentText(r.body)
                .setStyle(new Notification.BigTextStyle().bigText(r.body))
                .setContentIntent(content)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setColor(0xFF2F9E80)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setPublicVersion(pub)
                .build();
        nm.notify(r.id.hashCode(), n);
    }
}
