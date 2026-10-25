package id.ilham.cashflow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import org.json.JSONObject;

import java.io.OutputStream;

/**
 * Cangkang Android untuk aplikasi web CashFlow.
 * - Memuat assets/www/index.html lewat https://appassets.androidplatform.net (origin aman -> IndexedDB persisten, WebCrypto aktif).
 * - Tanpa izin INTERNET; navigasi ke luar aplikasi diblokir.
 * - AndroidBridge: saveFile, canAuthenticate/authenticate (sidik jari & kunci layar), syncReminders/notifStatus/requestNotifPermission.
 */
public class MainActivity extends Activity {

    private static final String HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + HOST + "/assets/www/index.html";
    private static final int REQ_PICK_FILE = 1001;
    private static final int REQ_NOTIF = 1002;
    private static final String PREFS_UI = "cashflow_ui";
    private static final String KEY_NOTIF_ASKED = "notif_asked";

    private WebView web;
    private ValueCallback<Uri[]> filePathCallback;
    private AuthController auth;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Notifier.ensureChannel(this);

        auth = new AuthController(this, (id, ok, msg) -> js("window.__nativeAuth&&window.__nativeAuth("
                + JSONObject.quote(id) + "," + (ok ? "true" : "false") + "," + JSONObject.quote(msg == null ? "" : msg) + ")"));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#2F9E80"));
        setContentView(web);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // localStorage + IndexedDB
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setTextZoom(100);                    // ukuran teks tidak ikut skala font sistem agar tata letak konsisten

        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                return host == null || !HOST.equals(host);   // blokir semua URL di luar aplikasi
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, "Pilih file backup"), REQ_PICK_FILE);
                } catch (Exception e) {
                    filePathCallback = null;
                    callback.onReceiveValue(null);
                    return false;
                }
                return true;
            }
        });

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(START_URL);
    }

    /** Menjalankan JavaScript di halaman (selalu di thread UI). */
    private void js(final String code) {
        runOnUiThread(() -> {
            if (web != null) web.evaluateJavascript(code, null);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);   // dibuka dari notifikasi: cukup membawa aplikasi ke depan
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (auth != null && auth.onActivityResult(requestCode, resultCode)) return;
        if (requestCode == REQ_PICK_FILE && filePathCallback != null) {
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null) {
                result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            }
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF) {
            js("window.__notifPerm&&window.__notifPerm()");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onPause() {
        // kunci aplikasi: tutup tampilan saat ke latar belakang (aplikasi web memutuskan perlu PIN atau tidak)
        js("window.__appHidden&&window.__appHidden()");
        super.onPause();
        web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
        js("window.__appShown&&window.__appShown()");
        js("window.__notifPerm&&window.__notifPerm()");   // izin notifikasi mungkin diubah dari Pengaturan HP
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();   // menutup bottom-sheet yang sedang terbuka
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (auth != null) auth.destroy();
        if (web != null) web.destroy();
        super.onDestroy();
    }

    // ------------------------------------------------------------------ notifikasi

    private boolean notificationsGranted() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        boolean enabled = nm != null && nm.areNotificationsEnabled();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            enabled = enabled && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return enabled;
    }

    private void openNotificationSettings() {
        try {
            Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
        }
    }

    private void requestNotifications() {
        if (notificationsGranted()) {
            js("window.__notifPerm&&window.__notifPerm()");
            return;
        }
        SharedPreferences sp = getSharedPreferences(PREFS_UI, MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            boolean asked = sp.getBoolean(KEY_NOTIF_ASKED, false);
            boolean canAskAgain = !asked || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
            if (canAskAgain) {
                sp.edit().putBoolean(KEY_NOTIF_ASKED, true).apply();
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
                return;
            }
        }
        openNotificationSettings();   // sudah ditolak permanen / dimatikan di Pengaturan
    }

    // ------------------------------------------------------------------ jembatan JavaScript

    /** Dipanggil dari JavaScript (thread "JavaBridge", bukan thread UI). */
    public class Bridge {
        /** AndroidBridge.saveFile(nama, mime, base64) -> folder Download. */
        @JavascriptInterface
        public boolean saveFile(String name, String mime, String base64) {
            try {
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                String safe = name == null ? "cashflow.dat" : name.replaceAll("[\\\\/:*?\"<>|]", "_");
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, safe);
                values.put(MediaStore.Downloads.MIME_TYPE, mime);
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                final Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return false;
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os == null) return false;
                    os.write(data);
                }
                if ("text/calendar".equals(mime)) {
                    // berkas kalender: langsung tawarkan membuka di aplikasi Kalender
                    runOnUiThread(() -> {
                        try {
                            Intent v = new Intent(Intent.ACTION_VIEW);
                            v.setDataAndType(uri, "text/calendar");
                            v.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(v);
                        } catch (Exception ignored) {
                            // tidak ada aplikasi kalender: berkas tetap tersimpan di Download
                        }
                    });
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /** true bila HP punya kunci layar (PIN/pola/sandi/sidik jari). */
        @JavascriptInterface
        public boolean canAuthenticate() {
            return auth != null && auth.canAuthenticate();
        }

        /** Memulai verifikasi; hasil dikirim ke window.__nativeAuth(id, ok, pesan). */
        @JavascriptInterface
        public void authenticate(final String id) {
            if (id == null || !id.matches("[A-Za-z0-9_-]{1,40}")) return;
            runOnUiThread(() -> {
                if (auth != null) auth.authenticate(id);
            });
        }

        /** Menerima daftar pengingat (JSON) dan menjadwalkan alarm. */
        @JavascriptInterface
        public void syncReminders(String json) {
            ReminderScheduler.sync(getApplicationContext(), json);
        }

        /** "granted" bila notifikasi diizinkan, selain itu "denied". */
        @JavascriptInterface
        public String notifStatus() {
            return notificationsGranted() ? "granted" : "denied";
        }

        /** Meminta izin notifikasi (Android 13+) atau membuka pengaturan notifikasi aplikasi. */
        @JavascriptInterface
        public void requestNotifPermission() {
            runOnUiThread(MainActivity.this::requestNotifications);
        }
    }
}
