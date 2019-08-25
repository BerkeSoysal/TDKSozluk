package com.berke.tdkszlk;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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

import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.BreadcrumbBuilder;

public class MainActivity extends Activity {

    private static final String DEFINITON_NOT_FIND = "Sözcük bulunamadı.";
    private AutoCompleteTextView autoCompleteTextView;
    private DatabaseAccess databaseAccess;
    private TextView definitionTextView;
    private ClipboardManager clipboardManager;
    private AdView mAdView;
    private String audioLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        databaseAccess = DatabaseAccess.getInstance(this);
        try {
            databaseAccess.open();
        }catch (RuntimeException e){
            Log.e("app", "exception", e);
        }

        super.onCreate(savedInstanceState);
        Sentry.init("https://e55ca330bc4140379d90c70c1cb7a0ed@sentry.io/1538691", new AndroidSentryClientFactory(this));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-3794325276172450~3851339933");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        Button copy = findViewById(R.id.copy);
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

        //String[] placeHolder = new String[1]; //To initialize array adapter.
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
        autoCompleteTextView.setOnEditorActionListener(new EditText.OnEditorActionListener(){
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

        autoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                autoCompleteTextView.showDropDown();
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                final String searchedWord = autoCompleteTextView.getText().toString().trim();
                fetchAndDisplay(searchedWord);

            }
        });

        ara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //autoCompleteTextView.dismissDropDown();
                final String searchedWord = autoCompleteTextView.getText().toString().trim();
                fetchAndDisplay(searchedWord);
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clipData = ClipData.newPlainText("meaning", definitionTextView.getText());
                clipboardManager.setPrimaryClip(clipData);
                Toast toast =Toast.makeText(getApplicationContext(),"Panoya Kopyalandı.",Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        listen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String searchedWord = autoCompleteTextView.getText().toString().trim();
                getAudioFromWeb(searchedWord);
            }
        });

    }




    private void getAudioFromWeb(String word) {
        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage("User made a listen "+word).build()
        );
        Sentry.capture("capture lısten");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://sozluk.gov.tr/yazim?ara="+word;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray reader = new JSONArray(response);
                            JSONObject list = (JSONObject)reader.get(0);
                            String audiolink = (String)list.get("seskod");
                            if(audiolink!= null && !audiolink.equals("")) {
                                audioLink = "http://sozluk.gov.tr/ses/" + audiolink + ".wav";
                                Uri uri = Uri.parse(audioLink);
                                MediaPlayer mp = new MediaPlayer();
                                try {
                                    mp.reset();
                                    mp.setDataSource(getApplicationContext(), uri);
                                    mp.prepare();
                                    mp.start();
                                } catch (IOException e) {

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast =Toast.makeText(getApplicationContext(),"Sistem hatası",Toast.LENGTH_SHORT);
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

        //adapter.notifyDataSetChanged();

        adapter.getFilter().filter("", null);

    }


    public void turkish(View view) {
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

    public void fetchAndDisplay(String word){
       // linearLayout.removeAllViews()
        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage("User searched for "+word).build()
        );
        Sentry.capture("capture search");
        String definition = databaseAccess.getDefinition(word);
        if (definition == null) {
            definitionTextView.setText(DEFINITON_NOT_FIND);
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