package at.buchberger.keyexchangeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import java.security.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AllPublicKeys extends AppCompatActivity {

    private ArrayList<PubKey> keys;
    private MySharedPreference sharedPreference;
    private Gson gson;
    private ListView listKeys;
    public final static String KEY_ID = "KEY_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_public_keys);

        gson = new Gson();
        sharedPreference = new MySharedPreference(getApplicationContext());
        listKeys = (ListView)findViewById(R.id.list);

        getKeyListFromSharedPreference(); //get data
        //set adapter for listview and visible it
        ListViewAdapter adapter = new ListViewAdapter(AllPublicKeys.this, R.layout.item_listview, keys);
        listKeys.setAdapter(adapter);
        listKeys.setOnItemClickListener(listener);
    }

    OnItemClickListener listener = new OnItemClickListener (){
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Intent intent = new Intent(getApplicationContext(), DetailKey.class);
            intent.putExtra(KEY_ID, Integer.toString(position));
            startActivity(intent);
        }
    };

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
}
