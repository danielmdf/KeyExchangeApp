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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {
    private Button btn_response;
    TextView tvResp;
    SharedPreferences prefs;
    String privkey;
    final String KEY1 = "PRIVKEY";
    PrivateKey privateKey;
    private String challenge;
    private String loesungstr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvResp = (TextView)findViewById(R.id.tv_response);

        btn_response = (Button)findViewById(R.id.btn_response);
        final Activity activity = this;
        btn_response.setOnClickListener(new View.OnClickListener(){
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

    /** eigene Keys Button Methode */
    public void ownPrivKeys(View view) {
        Intent intent = new Intent(this, OwnKeyActivity.class);
        startActivity(intent);
    }

    /** eigene Keys Button Methode */
    public void ownPubKeys(View view) {
        Intent intent = new Intent(this, OwnPubKeyActivity.class);
        startActivity(intent);
    }

    /** alle Keys Button Methode */
    public void publicKeys(View view) {
        Intent intent = new Intent(this, AllPublicKeys.class);
        startActivity(intent);
    }

    /** public Keys add Button Methode */
    public void addPubKey(View view) {
        Intent intent = new Intent(this, AddPubKey.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents()== null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Challenge wurde erfolgreich gescannt", Toast.LENGTH_LONG).show();

                //um den derzeitigen Privkey auszulesen
                prefs = this.getSharedPreferences("privkey", MODE_PRIVATE);
                privkey = prefs.getString(KEY1, "leerer PRIVATE KEY");

                byte[] decodeValue = Base64.decode(privkey.getBytes(), Base64.DEFAULT);
                EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(decodeValue);
                try {
                    KeyFactory keyFact = KeyFactory.getInstance("RSA");
                    try {
                        privateKey = keyFact.generatePrivate(privKeySpec);
                        Log.d("TEST_MainActivity", "privateKey = " + privateKey.toString());
                        Log.d("TEST_MainActivity", "Algorithm = " + privateKey.getAlgorithm());
                        Log.d("TEST_MainActivity", "Format = " + privateKey.getFormat());
                    }
                    catch (InvalidKeySpecException e) {
                        System.err.println(e.getMessage());
                    }
                }
                catch (NoSuchAlgorithmException e) {
                    System.err.println(e.getMessage());
                }

                challenge = result.getContents();
                //ruft die loesen methode auf
                try {
                    Log.d("TEST", "LOESUNG_ENC: " + challenge);
                    byte[] loesung = decrypt(privateKey, hexStringToByteArray(challenge));
                    String loesungstr = new String(loesung);
                    Log.d("TEST", "LOESUNG_DEC: " + loesungstr);

                    tvResp.setText(loesungstr.toString());

                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public byte[] decrypt(PrivateKey key, byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher dec = Cipher.getInstance("RSA");
        dec.init(Cipher.DECRYPT_MODE, key);
        return dec.doFinal(ciphertext);
    }

    /** Byte2String */
    private static String bytes2String(byte[] bytes) {
        StringBuilder string = new StringBuilder();
        for (byte b : bytes) {
            String hexString = Integer.toHexString(0x00FF & b);
            string.append(hexString.length() == 1 ? "0" + hexString : hexString);
        }
        return string.toString();
    }

    private static String hexToASCII(String hexValue)
    {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2)
        {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

}
