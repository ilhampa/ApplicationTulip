package id.ilham.cashflow;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Perhitungan jadwal pengingat bulanan (murni Java, tanpa API Android agar bisa diuji di JVM).
 * Aturan sama dengan aplikasi web: tanggal 29-31 berarti "akhir bulan" (hari terakhir bulan itu).
 */
final class ReminderMath {
    private ReminderMath() {}

    /** Hari efektif pada bulan tertentu: day >= 29 -> hari terakhir bulan; lainnya apa adanya (dibatasi panjang bulan). */
    static int effectiveDay(int day, YearMonth ym) {
        int len = ym.lengthOfMonth();
        if (day >= 29) return len;
        if (day < 1) return 1;
        return Math.min(day, len);
    }

    /** Kejadian pada bulan ym pada jam:menit yang ditentukan. */
    static LocalDateTime occurrence(YearMonth ym, int day, int hour, int minute) {
        return ym.atDay(effectiveDay(day, ym)).atTime(hour, minute);
    }

    /** Waktu pemicu pertama yang SETELAH 'after' (waktu lokal). */
    static LocalDateTime nextTrigger(int day, int hour, int minute, LocalDateTime after) {
        YearMonth ym = YearMonth.from(after);
        LocalDateTime c = occurrence(ym, day, hour, minute);
        if (!c.isAfter(after)) {
            c = occurrence(ym.plusMonths(1), day, hour, minute);
        }
        return c;
    }

    static long toEpochMillis(LocalDateTime t, ZoneId zone) {
        return t.atZone(zone).toInstant().toEpochMilli();
    }

    /** Memecah "HH:mm"; mengembalikan {jam, menit} atau null bila tidak valid. */
    static int[] parseTime(String s) {
        if (s == null || !s.matches("([01]\\d|2[0-3]):[0-5]\\d")) return null;
        return new int[]{Integer.parseInt(s.substring(0, 2)), Integer.parseInt(s.substring(3, 5))};
    }
}
