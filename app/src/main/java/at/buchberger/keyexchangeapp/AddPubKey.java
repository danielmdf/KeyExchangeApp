package at.buchberger.keyexchangeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.security.*;
import java.security.spec.*;
import javax.crypto.Cipher;
import javax.crypto.spec.PBEKeySpec;

import android.util.Base64;



import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class AddPubKey extends AppCompatActivity {

    private Button scanpubkey;
    TextView tvFPubKey;
    String fpubkeyausgelesen;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    final String KEY3 = "FPUBKEY";

    private ArrayList<PubKey> keys;
    private EditText et_name;
    private TextView tv_name;
    private Button savepubkey;
    private MySharedPreference sharedPreference;
    private Gson gson;
    private LinearLayout linearLayout;

    private String schluesselstring;
    private String schluessel;
    private String name;
    private String checksum;
    private String version;
    private String fingerprint;
    private String[] parts;
    private String[] versions;
    public PublicKey publicKey;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pub_key);
        prefs = this.getSharedPreferences("pubkey", MODE_PRIVATE);
        editor = prefs.edit();
        tvFPubKey = (TextView) findViewById(R.id.tvfpubkey);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        scanpubkey = (Button) findViewById(R.id.scanpubkey);
        final Activity activity = this;

        //part vom tst
        keys = new ArrayList<PubKey>();
        gson = new Gson();
        sharedPreference = new MySharedPreference(getApplicationContext());
        getKeyListFromSharedPreference();
        et_name = (EditText)findViewById(R.id.et_name);
        tv_name = (TextView)findViewById(R.id.tv_name);
        savepubkey = (Button)findViewById(R.id.savepubkey);

        //set event for button savepubkey
        savepubkey.setOnClickListener(onConfirmListener());

        scanpubkey.setOnClickListener(new View.OnClickListener(){
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
        linearLayout.setVisibility(View.VISIBLE);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){

            if (result.getContents()== null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Key wurde erfolgreich gescannt", Toast.LENGTH_LONG).show();

                //setzt leerkey auf das Resultat des QRScans als String
                schluesselstring = result.getContents().toString();

                //setzt den gespeicherten Wert in SharedPrefs auf den leerkey
                editor.putString("FPUBKEY", schluesselstring);
                editor.commit();

                //zum Auslesen/Aktualisieren der TextView
                fpubkeyausgelesen = prefs.getString(KEY3, "leerer PUBLIC KEY");
                tvFPubKey.setText(fpubkeyausgelesen);

                scanpubkey.setEnabled(false);
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Save list of scores to own sharedpref
     * @param keyList
     */
    private void saveKeyListToSharedpreference(ArrayList<PubKey> keyList) {
        //convert ArrayList object to String by Gson
        String jsonScore = gson.toJson(keyList);

        //save to shared preference
        sharedPreference.saveKeyList(jsonScore);
    }

    /**
     * Retrieving data from sharepref
     */
    private void getKeyListFromSharedPreference() {
        //retrieve data from shared preference
        String jsonScore = sharedPreference.getKeyList();
        Type type = new TypeToken<List<PubKey>>(){}.getType();
        keys = gson.fromJson(jsonScore, type);

        if (keys == null) {
            keys = new ArrayList<>();
        }
    }

    private View.OnClickListener onConfirmListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = et_name.getText().toString();
                if (!schluesselstring.equals("") && !name.equals("")) {
                    // TESTSTRING
                    //schluesselstring = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdQudusozLmogBfU2LCO+WcM59ycup9SxMsBNCku23PxrPMO6u//QjtWPz7istE9vkQfa6tQn1Or+SDxeHLMxEesF0xiBEgFUhg7vjOF2SnFQQEADgUyizUIBBn1UgKNA8eP24Ux0P0M2aHMn78HIHsRcupNGUNW7p51HOVoIPJQIDAQAB";

                    byte[] decodeValue = Base64.decode(schluesselstring.getBytes(), Base64.DEFAULT);
                    X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decodeValue);
                    try {
                        KeyFactory keyFact = KeyFactory.getInstance("RSA");
                        try {
                            publicKey = keyFact.generatePublic(pubKeySpec);
                            Log.d("TEST", "publicKey = " + publicKey.toString());
                            Log.d("TEST", "Algorithm = " + publicKey.getAlgorithm());
                            Log.d("TEST", "Format = " + publicKey.getFormat());

                            //not null input
                            PubKey newPubKey = new PubKey();
                            newPubKey.setSchluessel(schluesselstring);
                            newPubKey.setPlayerName(name);
                            newPubKey.setChecksum("checksum");
                            newPubKey.setVersion(publicKey.getFormat());
                            newPubKey.setFingerprint(publicKey.getAlgorithm());
                            newPubKey.setSigned(false);


                            keys.add(newPubKey); //add to keys list
                            saveKeyListToSharedpreference(keys); //save to share pref

                            //tst für toast
                            Toast.makeText(getApplicationContext(),"Key wurde erfolgreich gespeichert!" , Toast.LENGTH_LONG).show();
                            //clear edit text data
                            name = "";
                            schluessel ="";
                            version = "";
                            fingerprint ="";
                            checksum ="";
                            et_name.setText(name);
                            successKey();
                        }
                        catch (InvalidKeySpecException  e) {
                            //System.err.println("Error with KeySpec!");
                            System.err.println(e.getMessage());
                        }
                    }
                    catch (NoSuchAlgorithmException e) {
                        //System.err.println("Error with Algorithm!");
                        System.err.println(e.getMessage());
                    }
                    //parts = schluesselstring.split("\\n");
                    //version = parts[1];
                    //versions = version.split(":");
                    //version = versions[1];
                    //schluessel = parts[3];
                    //checksum = parts[4];
                    //fingerprint = schluessel.substring(schluessel.length()-8, schluessel.length());
                } else {
                    Log.e("Activity", "null input");
                    Toast.makeText(getApplicationContext(), "ERROR!", Toast.LENGTH_LONG).show();
                }

            }
        };
    }

    public void successKey() {
        Intent intent = new Intent(this, AllPublicKeys.class);
        startActivity(intent);
    }
}


