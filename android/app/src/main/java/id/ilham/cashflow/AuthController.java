package id.ilham.cashflow;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;

import java.util.concurrent.Executor;

/**
 * Verifikasi identitas memakai sidik jari / wajah / PIN / pola / kata sandi layar HP.
 * - Android 11+ : satu dialog BiometricPrompt (biometrik atau kredensial layar).
 * - Android 10  : BiometricPrompt biometrik + tombol "Gunakan PIN layar" -> layar konfirmasi kredensial.
 * - Tanpa biometrik terdaftar: langsung layar konfirmasi kredensial.
 */
final class AuthController {
    interface Callback {
        void onResult(String id, boolean ok, String message);
    }

    static final int REQ_CREDENTIAL = 4711;
    private static final String TITLE = "Buka CashFlow";

    private final Activity act;
    private final Callback cb;
    private String pendingId;
    private CancellationSignal signal;

    AuthController(Activity act, Callback cb) {
        this.act = act;
        this.cb = cb;
    }

    private KeyguardManager keyguard() {
        return (KeyguardManager) act.getSystemService(Context.KEYGUARD_SERVICE);
    }

    /** True bila HP punya kunci layar (PIN/pola/kata sandi); sidik jari/wajah hanya bisa terdaftar bila ini aktif. */
    boolean canAuthenticate() {
        KeyguardManager km = keyguard();
        return km != null && km.isDeviceSecure();
    }

    void authenticate(String id) {
        if (signal != null) {
            try {
                signal.cancel();
            } catch (RuntimeException ignored) {
            }
            signal = null;
        }
        if (pendingId != null) finish(false, "Dibatalkan");
        pendingId = id;
        if (!canAuthenticate()) {
            finish(false, "Kunci layar HP belum diatur. Atur PIN, pola, atau sidik jari di Pengaturan HP.");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            showPrompt(true);
        } else if (biometricAvailableLegacy()) {
            showPrompt(false);
        } else {
            askCredential();
        }
    }

    @SuppressWarnings("deprecation")
    private boolean biometricAvailableLegacy() {
        BiometricManager bm = act.getSystemService(BiometricManager.class);
        return bm != null && bm.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void showPrompt(boolean withCredential) {
        final String id = pendingId;
        Executor ex = act.getMainExecutor();
        BiometricPrompt.Builder b = new BiometricPrompt.Builder(act)
                .setTitle(TITLE)
                .setSubtitle("Verifikasi identitas Anda");
        if (withCredential) {
            b.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK
                    | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            b.setNegativeButton("Gunakan PIN layar", ex, (dialog, which) -> {
                if (id != null && id.equals(pendingId)) askCredential();
            });
        }
        signal = new CancellationSignal();
        try {
            b.build().authenticate(signal, ex, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    if (id != null && id.equals(pendingId)) finish(true, "");
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    if (id != null && id.equals(pendingId)) {
                        finish(false, errString == null ? "Verifikasi dibatalkan" : errString.toString());
                    }
                }
            });
        } catch (RuntimeException e) {
            askCredential();
        }
    }

    private void askCredential() {
        KeyguardManager km = keyguard();
        Intent i = km == null ? null : km.createConfirmDeviceCredentialIntent(TITLE,
                "Masukkan PIN, pola, atau kata sandi layar HP");
        if (i == null) {
            finish(false, "Kunci layar HP belum diatur.");
            return;
        }
        act.startActivityForResult(i, REQ_CREDENTIAL);
    }

    /** Diteruskan dari Activity.onActivityResult. Mengembalikan true bila ditangani. */
    boolean onActivityResult(int requestCode, int resultCode) {
        if (requestCode != REQ_CREDENTIAL) return false;
        if (pendingId != null) {
            boolean ok = resultCode == Activity.RESULT_OK;
            finish(ok, ok ? "" : "Verifikasi dibatalkan");
        }
        return true;
    }

    void destroy() {
        if (signal != null) {
            try {
                signal.cancel();
            } catch (RuntimeException ignored) {
            }
            signal = null;
        }
        pendingId = null;
    }

    private void finish(boolean ok, String msg) {
        String id = pendingId;
        pendingId = null;
        if (signal != null && ok) {
            signal = null;
        }
        if (id != null) cb.onResult(id, ok, msg);
    }
}
