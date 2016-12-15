package at.buchberger.keyexchangeapp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class GeneratorActivity extends AppCompatActivity {

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    final String KEY2 = "PUBKEY";
    TextView tvPrivKey;
    String ausgelesenerPubKey;

    ImageView qrcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);

        prefs = this.getSharedPreferences("pubkey", MODE_PRIVATE);
        editor = prefs.edit();
        tvPrivKey = (TextView) findViewById(R.id.textView);

        //zum Auslesen des gespeicherten "PRIVKEY" und um die TextView anzupassen
        ausgelesenerPubKey = prefs.getString(KEY2, "leerer PUBLIC KEY");
        tvPrivKey.setText(ausgelesenerPubKey);

        //qrcode
        qrcode = (ImageView) findViewById(R.id.qrcode);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(ausgelesenerPubKey, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrcode.setImageBitmap(bitmap);
        }
        catch(WriterException e){
            e.printStackTrace();
        }
    }



}
