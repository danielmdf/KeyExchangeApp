package at.buchberger.keyexchangeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DetailKey extends AppCompatActivity {

    private TextView tv_id2;
    private String message;
    private TextView tv_name2;
    private TextView tv_pkey2;
    private TextView tv_version2;
    private TextView tv_fingerprint2;
    private EditText et_challenge;
    private Button btn_challenge;
    private String challenge;
    private Button btn_createchall;
    private LinearLayout ll_challenge;
    private Button btn_hidechallenge;
    private Button btn_sign;
    private String ausgelesenerPubKey;
    private String ausgelesenerPrivKey;
    private String ausgelesenerOwnPubKey;
    private LinearLayout ll_signature;
    private TextView tv_signature2;
    private String challengeenc;

    private ArrayList<PubKey> keys;
    private MySharedPreference sharedPreference;
    private Gson gson;

    public Integer kid;
    private Button btn_send;

    SharedPreferences prefs;
    final String KEY1 = "PRIVKEY";
    final String KEY2 = "PUBKEY";
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey ownpublicKey;
    public final static String CHALLENGE = "CHALLENGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_key);
        Intent intent = getIntent();
        message = intent.getStringExtra(AllPublicKeys.KEY_ID);
        kid = Integer.parseInt(message);

        //anbindung db
        gson = new Gson();
        sharedPreference = new MySharedPreference(getApplicationContext());
        getKeyListFromSharedPreference(); //get data

        tv_id2 = (TextView)findViewById(R.id.tv_id2);
        tv_id2.setText(message);

        tv_name2 = (TextView)findViewById(R.id.tv_name2);
        tv_name2.setText(keys.get(kid).getPlayerName().toString());

        tv_version2 = (TextView)findViewById(R.id.tv_version2);
        tv_version2.setText(keys.get(kid).getVersion());

        tv_fingerprint2 = (TextView)findViewById(R.id.tv_fingerprint2);
        tv_fingerprint2.setText(keys.get(kid).getFingerprint());

        tv_pkey2 = (TextView)findViewById(R.id.tv_pkey2);

        ll_signature = (LinearLayout)findViewById(R.id.ll_signature);
        ll_signature.setVisibility(View.GONE);
        tv_signature2 = (TextView)findViewById(R.id.tv_signature2);

        //eigenen Pubkey auslesen und PublicKey Object zuweisen
        prefs = this.getSharedPreferences("pubkey", MODE_PRIVATE);
        ausgelesenerOwnPubKey = prefs.getString(KEY2, "leerer OWN PUBLIC KEY");
        byte[] decodeOwnValue = Base64.decode(ausgelesenerOwnPubKey.getBytes(), Base64.DEFAULT);
        X509EncodedKeySpec ownpubKeySpec = new X509EncodedKeySpec(decodeOwnValue);
        try {
            KeyFactory keyFact = KeyFactory.getInstance("RSA");
            try {
                ownpublicKey = keyFact.generatePublic(ownpubKeySpec);
                Log.d("TEST_EIGENEN_PUBKEY_A", "eigener publicKey = " + ownpublicKey.toString());
                Log.d("TEST_EIGENEN_PUBKEY_A", "eigener publicKey Algorithm = " + ownpublicKey.getAlgorithm());
                Log.d("TEST_EIGENEN_PUBKEY_A", "eigenes publicKey Format = " + ownpublicKey.getFormat());
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
        //fremden publickey auslesen und PublicKey Object zuweisen
        ausgelesenerPubKey = keys.get(kid).getSchluessel();
        byte[] decodeValue = Base64.decode(ausgelesenerPubKey.getBytes(), Base64.DEFAULT);
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decodeValue);
        try {
            KeyFactory keyFact = KeyFactory.getInstance("RSA");
            try {
                publicKey = keyFact.generatePublic(pubKeySpec);
                Log.d("TEST_FREMD_PUBKEY", "publicKey = " + publicKey.toString());
                Log.d("TEST_FREMD_PUBKEY", "Algorithm = " + publicKey.getAlgorithm());
                Log.d("TEST_FREMD_PUBKEY", "Format = " + publicKey.getFormat());
                Log.d("TEST_FREMD_PUBKEY", "signiert = " + keys.get(kid).getSigned());

                //VERIFY SIGNATURE
                if(keys.get(kid).getSigned()) {
                    boolean verified = verifySig(publicKey.getEncoded(), ownpublicKey, keys.get(kid).getSignature());
                    Log.d("TEST_AFTER_VERIFY", "verified = " + verified);
                    //das LinearLayout sichtbar zu machen & die TextView mit der Signatur zu fuellen
                    if(verified){
                       anzeigeSignatur();
                    }
                }
            }
            catch (InvalidKeySpecException e) {
                //System.err.println("Error with KeySpec!");
                System.err.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (NoSuchAlgorithmException e) {
            //System.err.println("Error with Algorithm!");
            System.err.println(e.getMessage());
        }
        tv_pkey2.setText(publicKey.toString());

        btn_send = (Button)findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                sendEmail();
            }
        });

        //sign Button
        btn_sign = (Button)findViewById(R.id.btn_sign);
        if(keys.get(kid).getSigned()){
            btn_sign.setClickable(false);
            btn_sign.setText("Signed");
        }

        et_challenge = (EditText)findViewById(R.id.et_challenge);

        ll_challenge = (LinearLayout)findViewById(R.id.ll_challenge);
        btn_createchall = (Button)findViewById(R.id.btn_createchall);
        btn_hidechallenge = (Button)findViewById(R.id.btn_hidechall);

        btn_challenge = (Button)findViewById(R.id.btn_challenge);
        btn_challenge.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), GeneratorVarActivity.class);
                challenge = et_challenge.getText().toString();

                //die challenge - das verschluesseln der eingabe
                //Log.d("TEST", "CHALLENGE_UNENC: " + challenge);
                //byte[] secret2 = encrypt(publicKey, challenge.getBytes(StandardCharsets.UTF_8));
                //challengeenc = bytes2String(secret2);
                //Log.d("TEST", "CHALLENGE_ENC: " + challengeenc);
                challengeenc = "HALLO";

                String hallo = "hallo";
                Log.d("HALLO", "vorher: " + hallo);
                Log.d("HALLO", "nachher: " + publicKey);
                Log.d("HALLO", "nachher: " + privateKey);
                Log.d("HALLO", "nachher: " + decrypt(encrypt(hallo, publicKey), privateKey));


                intent.putExtra(CHALLENGE, challengeenc);
                startActivity(intent);
            }
        });
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

    public void signKey(View view){
        //um den private key auszulesen fuer die Signatur
        prefs = this.getSharedPreferences("privkey", MODE_PRIVATE);
        ausgelesenerPrivKey = prefs.getString(KEY1, "leerer PRIVATE KEY");
        byte[] decodeValue2 = Base64.decode(ausgelesenerPrivKey.getBytes(), Base64.DEFAULT);

        EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(decodeValue2);
        try {
            KeyFactory keyFact = KeyFactory.getInstance("RSA");
            try {
                privateKey = keyFact.generatePrivate(privKeySpec);
                Log.d("TEST_EIGENEN_PRIVKEY_A", "privateKey = " + privateKey.toString());
                Log.d("TEST_EIGENEN_PRIVKEY_A", "Algorithm = " + privateKey.getAlgorithm());
                Log.d("TEST_EIGENEN_PRIVKEY_A", "Format = " + privateKey.getFormat());
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

        //WIRKLICHE SIGN METHODE:
        if(publicKey.getAlgorithm() == "RSA"){
        try{
            Signature sigRSA = Signature.getInstance("SHA1withRSA");
            sigRSA.initSign(privateKey);
            sigRSA.update(publicKey.getEncoded());
            keys.get(kid).setSignature(sigRSA.sign());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        //boolean setzen dass signiert worden ist, clickable des sign buttons deaktivieren
        keys.get(kid).setSigned(Boolean.TRUE);
        btn_sign.setClickable(Boolean.FALSE);
        btn_sign.setText("Signed");

        Toast.makeText(getApplicationContext(), "Key wurde erfolgreich signiert!", Toast.LENGTH_LONG).show();
        Log.d("SIGNATURE", "Signatur = " + bytes2String(keys.get(kid).getSignature()) );
        Log.d("SIGNATURE", "signiert = " + keys.get(kid).getSigned().toString() );
        saveKeyListToSharedpreference(keys); //save to share pref
        anzeigeSignatur();
        }
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

    /** verifySig */
    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initVerify(key);
        signer.update(data);
        Log.d("TEST_VERIFYSIG", "verified = " + signer);
        return (signer.verify(sig));

    }

    /** Retrieving data from sharepref */
    private void getKeyListFromSharedPreference() {
        //retrieve data from shared preference
        String jsonScore = sharedPreference.getKeyList();
        Type type = new TypeToken<List<PubKey>>(){}.getType();
        keys = gson.fromJson(jsonScore, type);

        if (keys == null) {
            keys = new ArrayList<>();
        }
    }

    /** encrypt method */
    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /** decrypt methode unused */
    public static String decrypt(byte[] text, PrivateKey key) {
        byte[] dectyptedText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            dectyptedText = cipher.doFinal(text);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(dectyptedText);
    }

    /** del Public Key */
    public void delPubKey(View view) {
        int hint = kid;
        keys.remove(hint);

        saveKeyListToSharedpreference(keys);
        Toast.makeText(getApplicationContext(), "Key wurde erfolgreich entfernt!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, AllPublicKeys.class);
        startActivity(intent);
    }

    /** Save list of scores to own sharedpref */
    private void saveKeyListToSharedpreference(ArrayList<PubKey> keyList) {
        //convert ArrayList object to String by Gson
        String jsonScore = gson.toJson(keyList);
        //save to shared preference
        sharedPreference.saveKeyList(jsonScore);
    }

    /** Mail send Methode */
    protected void sendEmail() {
        Log.i("Send email", "");
        String[] TO = {""};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PUBLIC KEY " + keys.get(kid).getPlayerName().toString());
        emailIntent.putExtra(Intent.EXTRA_TEXT, "-----BEGIN PUBLIC KEY-----\n" +  keys.get(kid).getSchluessel().toString() + "\n-----END PUBLIC KEY-----" + "\n\n-----BEGIN SIGNATURE-----\n" + bytes2String(keys.get(kid).getSignature()) + "\n-----END SIGNATURE-----" );

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email", "");
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(DetailKey.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /** methode um die Signatur (TextView + LinearLayout) anzuzeigen*/
    public void anzeigeSignatur(){
        ll_signature.setVisibility(View.VISIBLE);
        tv_signature2.setText(bytes2String(keys.get(kid).getSignature()));
    }

    /** methode um die Challenge anzuzeigen */
    public void createChallenge(View view){
        ll_challenge.setVisibility(View.VISIBLE);
        btn_hidechallenge.setVisibility(View.VISIBLE);
        btn_createchall.setVisibility(View.GONE);
    }

    /** methode um die Challenge zu verstecken */
    public void hideChallenge(View view){
        ll_challenge.setVisibility(View.GONE);
        btn_createchall.setVisibility(View.VISIBLE);
        btn_hidechallenge.setVisibility(View.GONE);
    }
}
