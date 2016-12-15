package at.buchberger.keyexchangeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class OwnPubKeyActivity extends AppCompatActivity {

    private Button scanownpubkey;
    String leerkeypub;
    String ausgelesenpub;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    TextView tvPubKey;
    final String KEY2 = "PUBKEY";
    PublicKey publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_pub_key);
        prefs = this.getSharedPreferences("pubkey", MODE_PRIVATE);
        editor = prefs.edit();
        tvPubKey = (TextView) findViewById(R.id.tvpubkey);

        //zum Auslesen/Aktualisieren der TextView
        ausgelesenpub = prefs.getString(KEY2, "leerer PUBLIC KEY");
        tvPubKey.setText(ausgelesenpub);

        scanownpubkey = (Button) findViewById(R.id.scanownpubkey);
        final Activity activity = this;
        scanownpubkey.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("SCAN");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents()== null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Key wurde erfolgreich gescannt", Toast.LENGTH_LONG).show();

                //setzt leerkey auf das Resultat des QRScans als String
                leerkeypub = result.getContents().toString();
                //1204 RSA PUBLIC TEST-KEY
                //leerkeypub = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv0OvUGU7v69R91BBZm1aDZb/dh1EYMoEQI5H+qpSyX6yxpBQbxuxcc8Ndq0CYZe1vInIXtt6HLvK+/T1FOnDh+/buZXR4kmSkG9aRQi9KZzs7sxnMCMXBwtSbA+cWr9nrfa5IPCN9utDRTuyt9bfzurPJpn7Rluj4mlir9vEDTjN0i/YX9/9uQpxLW2S0OpV/laVr+d4f2zCly+BAUbwPlkMKZ5/FGJ6XnitrQc+1v8Fm/b1XjwIo0Wv8Gz7lxdcnL4u9jvMxuH67yXv2PKGz/3oehq1ObTui5sg22wDRw3jWM5SGi18VjikFC9tFuFJODtJPzS3YBcqRg3VMHql6QIDAQAB";

                byte[] decodeValue = Base64.decode(leerkeypub.getBytes(), Base64.DEFAULT);
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decodeValue);
                try {
                    KeyFactory keyFact = KeyFactory.getInstance("RSA");
                    try {
                        publicKey = keyFact.generatePublic(pubKeySpec);
                        //
                        Log.d("TESTownPubKey", "publicKey = " + publicKey.toString());
                        Log.d("TESTownPubKey", "Algorithm = " + publicKey.getAlgorithm());
                        Log.d("TESTownPubKey", "Format = " + publicKey.getFormat());
                    }
                    catch (InvalidKeySpecException e) {
                        //System.err.println("Error with KeySpec!");
                        System.err.println(e.getMessage());
                    }
                }
                catch (NoSuchAlgorithmException e) {
                    //System.err.println("Error with Algorithm!");
                    System.err.println(e.getMessage());
                }

                //setzt den gespeicherten Wert in SharedPrefs auf den leerkey
                editor.putString("PUBKEY", leerkeypub);
                editor.commit();

                //zum Auslesen/Aktualisieren der TextView
                tvPubKey.setText(publicKey.toString());
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /** um zu Show GeneratorActivity zu wechseln */
    public void genKey(View view) {
        Intent intent = new Intent(this, GeneratorActivity.class);
        startActivity(intent);
    }
}
