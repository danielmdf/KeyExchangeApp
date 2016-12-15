package at.buchberger.keyexchangeapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class GeneratorVarActivity extends AppCompatActivity {

    private String challenge;
    TextView tvPrivKey;
    ImageView qrcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);

        tvPrivKey = (TextView) findViewById(R.id.textView);

        Intent intent = getIntent();
        challenge = intent.getStringExtra(DetailKey.CHALLENGE);
        tvPrivKey.setText(challenge);

        //qrcode
        qrcode = (ImageView) findViewById(R.id.qrcode);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(challenge, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrcode.setImageBitmap(bitmap);
        }
        catch(WriterException e){
            e.printStackTrace();
        }
    }


}
