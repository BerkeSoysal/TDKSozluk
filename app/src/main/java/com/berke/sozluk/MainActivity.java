package com.berke.sozluk;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String DEFINITION_NOT_FOUND = "Sözcük bulunamadı.";
    private AutoCompleteTextView autoCompleteTextView;
    private DatabaseAccess databaseAccess;
    private TextView definitionTextView;
    private AdView mAdView;
    Handler handler = new Handler();
    private ArrayAdapter<String> adapter;
    long last_text_edit = 0;


    private Runnable changeSuggestionWhenUserStopped = new Runnable() {
        public void run() {
            if(System.currentTimeMillis() > last_text_edit + 90);
            changeSuggestions(autoCompleteTextView.getText().toString(), adapter);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        connectToDatabase();

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        createAdBanner();

        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        definitionTextView = new TextView(MainActivity.this);
        definitionTextView.setTextSize(20);
        linearLayout.addView(definitionTextView);

        Button listen = findViewById(R.id.listen);
        Button ara = findViewById(R.id.arama);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setTextSize(20);

        ArrayList<String> lst = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, lst);

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(adapter);
        changeSuggestions(null, adapter);



        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable mEdit) {
                last_text_edit = System.currentTimeMillis();
                if (mEdit.length() > 0) {
                    handler.postDelayed(changeSuggestionWhenUserStopped, 100);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /*
                Dont need this
                */
                //You need to remove this to run only once
                handler.removeCallbacks(changeSuggestionWhenUserStopped);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*
                Dont need this either
                */
            }
        });

        //Set keyboard button to fire search event
        autoCompleteTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                final String searchedWord = autoCompleteTextView.getText().toString().trim();
                autoCompleteTextView.dismissDropDown();
                fetchAndDisplay(searchedWord);
                return true;
            }
            return false;
        });

        autoCompleteTextView.setOnFocusChangeListener((view, b) -> autoCompleteTextView.showDropDown());

        autoCompleteTextView.setOnItemClickListener((parent, arg1, pos, id) -> {
            final String searchedWord = autoCompleteTextView.getText().toString().trim();
            fetchAndDisplay(searchedWord);

        });

        ara.setOnClickListener(v -> {
            final String searchedWord = autoCompleteTextView.getText().toString().trim();
            fetchAndDisplay(searchedWord);
        });

        listen.setOnClickListener(v -> {
            String searchedWord = autoCompleteTextView.getText().toString().trim();
            getAudioFromWeb(searchedWord);
            fetchAndDisplay(searchedWord);
        });

    }

    private void createAdBanner() {
        //test ad ca-app-pub-3940256099942544/6300978111
        // real ad ca-app-pub-3794325276172450/8957033162
        MobileAds.initialize(this, initializationStatus -> { });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void connectToDatabase() {
        databaseAccess = DatabaseAccess.getInstance(this);
        try {
            databaseAccess.open();
        } catch (RuntimeException e) {
            Log.e("app", "exception", e);
        }
    }

    private void getAudioFromWeb(String word) {
        Thread thread = new Thread(() -> {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://sozluk.gov.tr/yazim?ara=" + word;

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,

                    response -> {
                        try {
                            JSONArray reader = new JSONArray(response);
                            JSONObject list = (JSONObject) reader.get(0);

                            if(list.get("seskod").equals("")) {
                                Toast toast = Toast.makeText(getApplicationContext(), "Aranan sözcük için ses kaydı bulunmuyor.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            else {
                                Uri uri = Uri.parse("https://sozluk.gov.tr/ses/" + list.get("seskod") + ".wav");
                                MediaPlayer mp = new MediaPlayer();
                                playPronunciation(uri, mp);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                Toast toast = Toast.makeText(getApplicationContext(), "Bu özelliği kullanabilmek için internet bağlantınızın olması gerekmektedir.", Toast.LENGTH_LONG);
                error.printStackTrace();
                toast.show();
            }
            );
            queue.add(stringRequest);
        });

        thread.start();

    }

    private void playPronunciation(Uri uri, MediaPlayer mp) {
        try {
            mp.reset();
            mp.setDataSource(getApplicationContext(), uri);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }

    private void changeSuggestions(String word, ArrayAdapter<String> adapter) {
        String[] words;
        if (!(word == null || word.equals(""))) {
            words = databaseAccess.getSuggestions(word);
            adapter.clear();


            for (String word1 : words) {
                if (word1 != null) {
                    adapter.add(word1);
                }
            }

            adapter.getFilter().filter("", null);
        }
    }

    public void addTurkishCharacter(View view) {
        Button pressedButton = (Button) view;
        int position = autoCompleteTextView.getSelectionStart();
        String text = autoCompleteTextView.getText().toString();
        String poststring = "";
        if (text.length() > position)
            poststring = text.substring(position);
        String newword = text.substring(0, position) + pressedButton.getText().toString() + poststring;
        autoCompleteTextView.setText(newword);
        autoCompleteTextView.setSelection(position + 1);
    }

    public void fetchAndDisplay(String word) {
        String definition = databaseAccess.getDefinition(word);
        if (definition == null) {
            definitionTextView.setText(DEFINITION_NOT_FOUND);
        } else {
            definition = definition.replace("</tr>", "</tr><br>");
            definitionTextView.setText(Html.fromHtml(definition));
            definitionTextView.setTextIsSelectable(true);
        }
        // Save to database
    }

}