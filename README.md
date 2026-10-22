# Cashflow Tulip 175

Aplikasi cashflow & kantong keuangan (Ilham · Putri · Bersama) yang berjalan **offline**.
Database lokal (IndexedDB) ada di dalam aplikasi, tanpa server, tanpa izin internet.

```
webapp/www/   Aplikasi web (UI + logika + database lokal)  <- satu-satunya sumber kode UI
android/      Cangkang Android (WebView) -> dibungkus jadi APK
.github/      Workflow GitHub Actions: build APK otomatis di cloud
```

## Jalur A — APK lewat GitHub Actions (tanpa install apa pun di PC)
1. Buat repository **Private** di GitHub (data awal berisi angka cashflow, jangan Public).
2. Upload seluruh isi folder ini (pastikan folder `.github` ikut terunggah; jika tidak, buat manual lewat *Add file > Create new file* dengan nama `.github/workflows/build-apk.yml` lalu salin isinya).
3. Buka tab **Actions** -> **Build APK Cashflow Tulip 175** -> **Run workflow**.
4. Tunggu ±3–6 menit -> buka run yang selesai -> unduh artifact **Cashflow_Tulip_175_APK** (zip berisi `Cashflow_Tulip_175.apk`).
5. Kirim APK ke HP -> buka -> izinkan "Install dari sumber ini" -> Install.

## Jalur B — Android Studio
Buka folder `android/` di Android Studio (Hedgehog atau lebih baru, JDK 17) -> **Build > Build APK(s)**.
Hasil: `android/app/build/outputs/apk/debug/app-debug.apk`.

## Jalur C — PWA (tanpa APK)
Hosting folder `webapp/www/` di alamat **HTTPS** (Netlify / Cloudflare Pages / GitHub Pages), buka di Chrome Android -> menu **Pasang aplikasi / Add to Home screen**.
Catatan: situs publik = data awal ikut terlihat publik; kosongkan data awal dulu bila perlu.

## Data & backup
- Data tersimpan di penyimpanan privat aplikasi. **Menghapus/uninstall aplikasi menghapus data** -> rutin **Laporan > Backup JSON** (tersimpan di folder Download).
- **Pulihkan backup** untuk memindah data ke HP lain.
- Persyaratan: Android 10 (API 29) atau lebih baru.

## Memperbarui aplikasi
Ubah `webapp/www/index.html` -> push -> jalankan workflow lagi -> install APK baru **di atas** yang lama.
APK memakai keystore tetap (`android/app/tulip175.jks`), jadi data tidak hilang saat update. Tetap disarankan Backup JSON sebelum update.
Keystore ini untuk pemakaian pribadi (sideload); repository wajib **Private**.
