# CashFlow (Android)

Aplikasi web **CashFlow** v1.3.0 yang dibungkus menjadi aplikasi Android (APK). Tanpa izin internet; semua data tersimpan di HP.

Fitur khusus Android (tidak ada di versi browser):

| Fitur | Cara kerja |
|---|---|
| **Sidik jari / wajah / PIN layar HP** | Pengaturan → Keamanan → Kunci aplikasi → pilih *Kunci layar HP*, atau *PIN aplikasi* + izinkan sidik jari. Android 11+: satu dialog (biometrik atau PIN/pola layar). Android 10: biometrik, tombol "Gunakan PIN layar" untuk cadangan. |
| **Notifikasi pengingat kantong** | Tab Kantong → ketuk kantong → *Ubah kantong* → aktifkan notifikasi, pilih tanggal & jam. Notifikasi muncul tiap bulan sebagai notifikasi HP, juga saat aplikasi tertutup, dan dijadwal ulang setelah HP restart. Tanggal 29–31 = hari terakhir bulan. |
| **Kalender** | Pengaturan → Notifikasi & kalender → *Ekspor ke Kalender (.ics)* → aplikasi Kalender terbuka dan menawarkan menambah acara bulanan berulang dengan alarm. |
| **Backup ke folder Download** | Pengaturan → Export semua data (JSON). Import lewat pemilih berkas. |

## Isi folder

```
android/            proyek Android (Java, Gradle)
webapp/www/         taruh index.html (= CashFlow_v1.3.0.html) di sini, ATAU taruh file CashFlow_v1.3.0.html di root repository
.github/workflows/  build otomatis APK di GitHub Actions
tools/              uji mandiri jadwal pengingat
```

## Cara membuat APK (GitHub Actions, tanpa install apa pun di PC)

1. Ekstrak zip ini.
2. Buat repository **Private** di GitHub (berkas penanda tangan APK ikut di dalamnya).
3. Unggah **seluruh isi** folder hasil ekstrak. Pastikan folder `.github` ikut. Bila tidak ikut, buat manual lewat *Add file → Create new file* dengan nama `.github/workflows/build-apk.yml`, lalu salin isi berkas `build-apk.yml` yang dikirim terpisah.
4. Unggah juga **`CashFlow_v1.3.0.html`** ke root repository (berkas itu didapat dari percakapan sebelumnya). Workflow otomatis menyalinnya menjadi `webapp/www/index.html`.
5. Buka tab **Actions → Build APK CashFlow → Run workflow**. Tunggu sekitar 4–8 menit.
6. Buka run yang selesai → bagian **Artifacts** → unduh **CashFlow_APK**. Berkas itu berupa **zip**: ekstrak dulu, lalu pasang `CashFlow_v1.3.0.apk`.
7. Di HP: buka APK → izinkan "Install dari sumber ini" → Install.

Bila build gagal, buka langkah yang merah dan kirim log-nya.

## Cara alternatif: Android Studio

Buat `webapp/www/index.html` dari berkas CashFlow_v1.3.0.html, buka folder `android/` di Android Studio (JDK 17), lalu **Build → Build APK(s)**. Hasil: `android/app/build/outputs/apk/debug/app-debug.apk`.

## Persyaratan & catatan

- **Android 10 (API 29) atau lebih baru.** Bila muncul "Ada masalah saat mengurai paket", versi Android HP lebih rendah, atau APK belum diekstrak dari zip artifact.
- ID aplikasi: `id.ilham.cashflow`. Ini aplikasi baru, jadi data tidak terbawa dari versi browser. **Export** di versi browser, lalu **Import** di aplikasi.
- Data tersimpan di penyimpanan privat aplikasi. **Uninstall menghapus data**; lakukan Export dulu.
- Memperbarui: bangun APK baru dari repository yang sama (tanda tangan tetap) dan pasang di atas yang lama; data tidak hilang.
- Alarm pengingat tidak berjalan bila aplikasi dihentikan paksa dari Pengaturan HP ("Hentikan paksa") sampai aplikasi dibuka lagi. Beberapa merek HP (Xiaomi, Oppo, Vivo, Samsung) memerlukan pengecualian penghemat baterai agar alarm tepat waktu.
- Berkas penanda tangan (`android/app/cashflow.jks`) hanya untuk pemakaian pribadi dan bukan untuk Play Store.

## Daftar uji di HP (sekitar 10 menit)

1. **Instal & ikon**: ikon tulip tampil di layar utama; splash hijau lalu animasi logo.
2. **Backup**: Pengaturan → Export semua data → berkas muncul di folder *Download*. Import berkas yang sama → data sama.
3. **Izin notifikasi** (Android 13+): aktifkan notifikasi pada satu kantong → dialog izin muncul → *Izinkan*. Pengaturan menunjukkan "Diizinkan".
4. **Notifikasi tepat waktu**: atur satu kantong ke jam 3 menit dari sekarang pada tanggal hari ini → kunci layar HP → notifikasi muncul. Di layar kunci, nominal disembunyikan.
5. **Restart**: restart HP → tunggu hingga jadwal → notifikasi tetap muncul tanpa membuka aplikasi.
6. **Sidik jari**: Pengaturan → Kunci aplikasi → Kunci layar HP → verifikasi. Tutup aplikasi (Segera setelah ditinggalkan) → buka lagi → dialog sidik jari muncul. Coba batalkan → tidak terbuka.
7. **PIN aplikasi + sidik jari**: pilih PIN aplikasi, aktifkan sidik jari → layar PIN punya tombol sidik jari.
8. **Kalender**: Ekspor ke Kalender (.ics) → Kalender menawarkan menambah acara bulanan berulang.
9. **Tombol Back**: menutup lembar (sheet) yang terbuka sebelum keluar aplikasi.
