package at.buchberger.keyexchangeapp;

/**
 * Created by daniel on 07.11.2016.
 */

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreference {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    // Context
    private Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "pref";
    private static final String KEYS = "keys";

    public MySharedPreference(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void saveKeyList(String scoreString) {
        editor.putString(KEYS, scoreString);
        editor.commit();
    }

    public String getKeyList() {
        return pref.getString(KEYS, "");
    }
}