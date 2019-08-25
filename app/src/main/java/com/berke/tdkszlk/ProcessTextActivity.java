package com.berke.tdkszlk;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class ProcessTextActivity extends Activity {
    private DatabaseAccess databaseAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        databaseAccess = DatabaseAccess.getInstance(this);
        try {
            databaseAccess.open();
        }catch (RuntimeException e){
            Log.e("app", "exception", e);
        }
        super.onCreate(savedInstanceState);

        CharSequence text = getIntent()
                .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        String word = text.toString();
        //Dialog dialog = Dialog.
        AlertDialog.Builder builder;
        if(Build.VERSION.SDK_INT >= 21)
            builder = new AlertDialog.Builder(ProcessTextActivity.this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        else
            builder = new AlertDialog.Builder(ProcessTextActivity.this);
        builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
                dialog.dismiss();
            }
        });
        builder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                }
                return true;
            }
        });

        builder.setCancelable(true);
        builder.setTitle("TDK SÖZLÜK");

        databaseAccess = DatabaseAccess.getInstance(this);

        String meaning = databaseAccess.getDefinition(word);

        //TextView textView = findViewById(R.id.textView);
        //textView.setText(Html.fromHtml(meaning));
        builder.setMessage(Html.fromHtml(meaning));
        builder.create().show();
        // process the text
    }
}
