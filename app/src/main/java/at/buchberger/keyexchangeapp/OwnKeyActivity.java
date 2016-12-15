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
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class OwnKeyActivity extends AppCompatActivity {

    private Button scanownkey;
    String leerkey;
    String ausgelesen;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    TextView tvPrivKey;
    final String KEY1 = "PRIVKEY";
    PrivateKey privateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_key);
        prefs = this.getSharedPreferences("privkey", MODE_PRIVATE);
        editor = prefs.edit();
        tvPrivKey = (TextView) findViewById(R.id.tvprivkey);

        //zum Auslesen/Aktualisieren der TextView
        ausgelesen = prefs.getString(KEY1, "leerer PRIVATE KEY");
        tvPrivKey.setText(ausgelesen);

        scanownkey = (Button) findViewById(R.id.scanownkey);
        final Activity activity = this;
        scanownkey.setOnClickListener(new View.OnClickListener(){
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
                leerkey = result.getContents().toString();

                //1024 RSA PRIVATE TEST-KEY
                //leerkey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC/Q69QZTu/r1H3UEFmbVoNlv92HURgygRAjkf6qlLJfrLGkFBvG7Fxzw12rQJhl7W8iche23ocu8r79PUU6cOH79u5ldHiSZKQb1pFCL0pnOzuzGcwIxcHC1JsD5xav2et9rkg8I3260NFO7K31t/O6s8mmftGW6PiaWKv28QNOM3SL9hf3/25CnEtbZLQ6lX+VpWv53h/bMKXL4EBRvA+WQwpnn8UYnpeeK2tBz7W/wWb9vVePAijRa/wbPuXF1ycvi72O8zG4frvJe/Y8obP/eh6GrU5tO6LmyDbbANHDeNYzlIaLXxWOKQUL20W4Uk4O0k/NLdgFypGDdUweqXpAgMBAAECggEAPbr6ulfyEMvlCI0+jnB8wCYcDWMkzphlBvDlCmIulhnuWWf6jeHCQ5kwHz13p0fvwAo4QWVU5DE4Mlm9QCKymQ0xPZS9SOu2AifuWpmXc/bSAcC3DTJGHF/rGjVvoVplfCBFX/xD1Y3ZCF9cLzmKwp3Gfg70qqCpJEofL+MeQ6Wi7YRUiCORaAHC4yfIa/oEJ002hqQhceId8ZKfhklvmHJHh3aYTtY5Z6gERYLzwdHWNnyGEsorOlrqARvbDpHraPyc+9phO6FewZIETes5sI+b9TJoL7pnc4feCd/S7IQ079lQ0v+8jHjxfVbchONVyLWilYIcUok+m5acQCjgAQKBgQDg3KDH0LETrl8XYiIv8HfsnrWf1beK7hJ+jhudlBviTtNvu3Nl6mqErrcRsG7HC56UcYY9q3R93F5tC/R1JFEWsnAc/F19d4MmLcu9A4iDMnz+PLGb9sxF3sQBWbcB3MJLjgWgyM28TPmXvjjEHAtogc8OhK2RI9CVg78mzBCcYQKBgQDZwAZTaO/dHva9p+v46CnK9IammrxqyLE3BDrMRlSd/EelBsi6mVUyhFzE4DxfuX5cJrpYPTvA8W5fmacL8BmdHhrgG0ewQQJWE0FXPWhfdFaMdQG5L+ZxH64+qZ++Q6xl+dxvt2suRD3pNbebFrjlNt2RvAUw1GoBJGw21Kq2iQKBgQC0Fv+OCM5JfPYbvDS1QFpL4DCGepwtYM7fHOFxKxXKAmfErNSXY088RNHKEQwnzl6LdQCWk6MQylW18EUxLIzlKLQyAsy9l+IpybPLBfFnYiBJXkLBLsAblXZVvoybqTrTWEOZqjlb/ipIJclBB1T7tjsm3YxFGfIMTR/i6rzDAQKBgQCi2togUfBriXfyun9i5ogzUZTPUBUxLC5WPmSXWNZ6Xi9bPmqsHHe604HqgObGlR3rX8+opQtmr2rkNy/XXthZSSXCjMSeDtDnfXk1/Shtk74TSINkdG2+F3qjRQvDKivrDOeP6jdQIBvJqrJKXMEmNVWbZGrhbUxm1E3W6FFROQKBgGETtCaguYZ9t+qP/flh6/UjcFdEUrLuOzejL3VjXE/3vN485sMBbH1zkz8iSe29a9R7HayC0IF5ZchYXZysI3iJjXiMsDNKDoVDZiNTxIQ05HThYAwBNHLmXzecm8FArmiuH1wxsIUDivc88ghxeV+b6fzu4qlvG7CXltU4DCAP";

                byte[] decodeValue = Base64.decode(leerkey.getBytes(), Base64.DEFAULT);
                EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(decodeValue);
                try {
                    KeyFactory keyFact = KeyFactory.getInstance("RSA");
                    try {
                        privateKey = keyFact.generatePrivate(privKeySpec);
                        Log.d("TEST_ownKeyActivity", "privateKey = " + privateKey.toString());
                        Log.d("TEST_ownKeyActivity", "Algorithm = " + privateKey.getAlgorithm());
                        Log.d("TEST_ownKeyActivity", "Format = " + privateKey.getFormat());
                    }
                    catch (InvalidKeySpecException e) {
                        System.err.println(e.getMessage());
                    }
                }
                catch (NoSuchAlgorithmException e) {
                    System.err.println(e.getMessage());
                }
                //setzt den gespeicherten Wert in SharedPrefs auf den leerkey
                editor.putString("PRIVKEY", leerkey);
                editor.commit();
                //zum Auslesen/Aktualisieren der TextView
                tvPrivKey.setText(privateKey.toString());
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

}
