package id.ilham.cashflow;

import java.time.LocalDateTime;

/**
 * Uji mandiri logika jadwal pengingat (berjalan di JVM biasa, tanpa Android).
 * Jalankan:  javac -d out android/app/src/main/java/id/ilham/cashflow/ReminderMath.java tools/ReminderSelfTest.java
 *            java -cp out id.ilham.cashflow.ReminderSelfTest
 * Kolom: tanggal, jam, menit, "sekarang", "pemicu berikutnya yang diharapkan".
 */
public class ReminderSelfTest {
    private static final Object[][] CASES = {
        {31, 9, 0, "2026-02-10T08:00", "2026-02-28T09:00"},
        {31, 9, 0, "2028-02-29T09:00", "2028-03-31T09:00"},
        {31, 9, 0, "2028-02-29T08:59", "2028-02-29T09:00"},
        {30, 23, 59, "2026-12-31T23:59", "2027-01-31T23:59"},
        {29, 0, 0, "2026-01-31T00:00", "2026-02-28T00:00"},
        {1, 0, 0, "2026-12-31T12:00", "2027-01-01T00:00"},
        {5, 8, 30, "2026-10-22T08:00", "2026-11-05T08:30"},
        {22, 7, 0, "2026-10-22T08:00", "2026-11-22T07:00"},
        {22, 8, 0, "2026-10-22T08:00", "2026-11-22T08:00"},
        {22, 8, 1, "2026-10-22T08:00", "2026-10-22T08:01"},
        {28, 12, 0, "2027-02-28T12:00", "2027-03-28T12:00"},
        {28, 12, 0, "2027-02-28T11:59", "2027-02-28T12:00"},
        {31, 18, 0, "2026-10-22T08:00", "2026-10-31T18:00"},
        {15, 9, 0, "2100-02-15T09:00", "2100-03-15T09:00"},
        {31, 6, 0, "2100-02-28T06:01", "2100-03-31T06:00"}
    };

    public static void main(String[] args) {
        int bad = 0;
        for (Object[] c : CASES) {
            LocalDateTime got = ReminderMath.nextTrigger((Integer) c[0], (Integer) c[1], (Integer) c[2], LocalDateTime.parse((String) c[3]));
            LocalDateTime exp = LocalDateTime.parse((String) c[4]);
            if (!got.equals(exp)) {
                bad++;
                System.out.println("GAGAL: hari=" + c[0] + " " + c[1] + ":" + c[2] + " sekarang=" + c[3] + " -> " + got + " (seharusnya " + exp + ")");
            }
        }
        int[] t = ReminderMath.parseTime("08:30");
        if (t == null || t[0] != 8 || t[1] != 30) { bad++; System.out.println("GAGAL: parseTime 08:30"); }
        if (ReminderMath.parseTime("24:00") != null || ReminderMath.parseTime("8:30") != null || ReminderMath.parseTime(null) != null) { bad++; System.out.println("GAGAL: parseTime tidak valid"); }
        System.out.println(CASES.length + " kasus, gagal: " + bad);
        if (bad > 0) System.exit(1);
    }
}
