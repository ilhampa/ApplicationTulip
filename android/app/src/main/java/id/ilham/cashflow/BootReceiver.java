package id.ilham.cashflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Membuat ulang alarm setelah perangkat restart, aplikasi diperbarui, atau jam/zona waktu berubah. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_MY_PACKAGE_REPLACED:
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
                ReminderScheduler.rescheduleAll(context);
                break;
            default:
                break;
        }
    }
}
