package com.berke.sozluk;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

public class MainActivity extends Activity
{
    private static final String DEFINITION_NOT_FOUND = "Sözcük bulunamadı.";
    private AutoCompleteTextView autoCompleteTextView;
    private DatabaseAccess databaseAccess;
    private TextView definitionTextView;
    private ClipboardManager clipboardManager;
    private AdView mAdView;
    private String audioLink;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        connectToDatabase();

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        createAdBanner();

        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        //Button copy = findViewById(R.id.copy);
        CardView initial = new CardView(MainActivity.this);
        definitionTextView = new TextView(MainActivity.this);
        definitionTextView.setTextSize(20);
        initial.addView(definitionTextView);
        linearLayout.addView(initial);

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        Button listen = findViewById(R.id.listen);
        Button ara = findViewById(R.id.arama);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setTextSize(20);

        ArrayList<String> lst = new ArrayList<>();
        final ArrayAdapter<String> adapter =new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, lst);

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(adapter);
        changeSuggestions(null,adapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable mEdit)
            {
                String text = mEdit.toString();
                if(text != null)
                {
                    changeSuggestions(text,adapter);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }

            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        //Set keyboard button to fire search event
        autoCompleteTextView.setOnEditorActionListener(new EditText.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    final String searchedWord = autoCompleteTextView.getText().toString().trim();
                    autoCompleteTextView.dismissDropDown();
                    fetchAndDisplay(searchedWord);
                    return true;
                }
                return false;
            }
        });

        autoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b) {
                autoCompleteTextView.showDropDown();
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                final String searchedWord = autoCompleteTextView.getText().toString().trim();
                fetchAndDisplay(searchedWord);

            }
        });

        ara.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                final String searchedWord = autoCompleteTextView.getText().toString().trim();
                fetchAndDisplay(searchedWord);
            }
        });

        /*
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clipData = ClipData.newPlainText("meaning", definitionTextView.getText());
                clipboardManager.setPrimaryClip(clipData);
                Toast toast =Toast.makeText(getApplicationContext(),"Panoya Kopyalandı.",Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        */
        listen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String searchedWord = autoCompleteTextView.getText().toString().trim();
                getAudioFromWeb(searchedWord);
            }
        });

    }

    private void createAdBanner()
    {
        //test ad ca-app-pub-3940256099942544/6300978111
        // real ad ca-app-pub-3794325276172450/8957033162
        MobileAds.initialize(this, "ca-app-pub-3940256099942544/6300978111");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void connectToDatabase()
    {
        databaseAccess = DatabaseAccess.getInstance(this);
        try {
            databaseAccess.open();
        }catch (RuntimeException e){
            Log.e("app", "exception", e);
        }
    }

    private void getAudioFromWeb(String word)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://sozluk.gov.tr/yazim?ara="+word;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray reader = new JSONArray(response);
                            JSONObject list = (JSONObject)reader.get(0);
                            String audiolink = (String)list.get("seskod");
                            if(audiolink!= null && !audiolink.equals("")) {
                                audioLink = "https://sozluk.gov.tr/ses/" + audiolink + ".wav";
                                Uri uri = Uri.parse(audioLink);
                                MediaPlayer mp = new MediaPlayer();
                                try {
                                    mp.reset();
                                    mp.setDataSource(getApplicationContext(), uri);
                                    mp.prepare();
                                    mp.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast =Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT);
                error.printStackTrace();
                toast.show();
            }
        }
        );
        queue.add(stringRequest);
    }

    private void changeSuggestions(String word,ArrayAdapter<String> adapter) {
        String[] words;
        if(word==null || word.equals("")){
            words = databaseAccess.getSuggestionsFromCache();
        }
        else {
            words = databaseAccess.getSuggestions(word);
        }
        adapter.clear();
        for (String word1 : words) {
            if (word1 != null) {
                adapter.add(word1);
            }
        }

        adapter.getFilter().filter("", null);

    }

    public void addTurkishCharacter(View view) {
        Button pressedButton = (Button)view;
        int position = autoCompleteTextView.getSelectionStart();
        String text = autoCompleteTextView.getText().toString();
        String poststring = "";
        if(text.length() > position)
            poststring = text.substring(position);
        String newword = text.substring(0,position) + pressedButton.getText().toString() + poststring;
        autoCompleteTextView.setText(newword);
        autoCompleteTextView.setSelection(position+1);
    }

    public void fetchAndDisplay(String word)
    {
        String definition = databaseAccess.getDefinition(word);
        if (definition == null) {
            definitionTextView.setText(DEFINITION_NOT_FOUND);
        }
        else {
            definition = definition.replaceAll("</tr>", "</tr><br>");
            definitionTextView.setText(Html.fromHtml(definition));
            definitionTextView.setTextIsSelectable(true);
            databaseAccess.saveToCache(word);
        }
        // Save to database
    }

}