package com.example.nfcscanner;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] nfcIntentFilters;
    private TextView uidText;
    private TextView errorText;
    private EditText editTextUid;
    private String scannedUid = "";
    private MediaPlayer matchSound;
    private MediaPlayer mismatchSound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uidText = findViewById(R.id.uidText);
        errorText = findViewById(R.id.errorText);
        editTextUid = findViewById(R.id.editTextUid);
        matchSound = MediaPlayer.create(this, R.raw.sound_match);
        mismatchSound = MediaPlayer.create(this, R.raw.sound_mismatch);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device is not supported", Toast.LENGTH_LONG).show();
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Enable NFC", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        );
        nfcIntentFilters = new IntentFilter[] {
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        };
    }
    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] uid = tag.getId();
            scannedUid = bytesToHex(uid);
            uidText.setText("UID: " + scannedUid);
            String enteredUid = editTextUid.getText().toString();
            if (scannedUid.equals(enteredUid)) {
                if (matchSound != null) {
                    matchSound.start();
                }
                errorText.setText("Match!");
                errorText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                if (mismatchSound != null) {
                    mismatchSound.start();
                }
                errorText.setText("No match");
                errorText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (matchSound != null) {
            matchSound.release();
        }
        if (mismatchSound != null) {
            mismatchSound.release();
        }
    }
}