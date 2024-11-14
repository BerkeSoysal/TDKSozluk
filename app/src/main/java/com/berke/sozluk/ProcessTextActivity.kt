package com.berke.sozluk

import android.R.style.Theme_Material_Light_Dialog_NoActionBar
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import com.berke.sozluk.DatabaseAccess.Companion.getInstance

class ProcessTextActivity : Activity() {
    private var databaseAccess: DatabaseAccess? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        databaseAccess = getInstance(this)
        try {
            databaseAccess!!.open()
        } catch (e: RuntimeException) {
            Log.e("app", "exception", e)
        }
        super.onCreate(savedInstanceState)

        val text = intent
            .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        val word = text.toString()
        //Dialog dialog = Dialog.
        val builder =  AlertDialog.Builder(
            this@ProcessTextActivity, Theme_Material_Light_Dialog_NoActionBar
        )
        builder.setPositiveButton(
            "TAMAM"
        ) { dialog, whichButton ->
            finish()
            dialog.dismiss()
        }
        builder.setOnKeyListener { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish()
            }
            true
        }

        builder.setCancelable(true)
        builder.setTitle("TDK SÖZLÜK")

        databaseAccess = getInstance(this)

        val meaning = databaseAccess!!.getDefinition(word)

        //TextView textView = findViewById(R.id.textView);
        //textView.setText(Html.fromHtml(meaning));
        builder.setMessage(Html.fromHtml(meaning, Html.FROM_HTML_MODE_LEGACY))
        builder.create().show()
        // process the text
    }
}