package com.berke.sozluk

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : Activity() {
    private var autoCompleteTextView: AutoCompleteTextView? = null
    private var databaseAccess: DatabaseAccess? = null
    private var definitionTextView: TextView? = null
    var handler: Handler = Handler(Looper.myLooper()!!)
    private var adapter: ArrayAdapter<String>? = null
    var last_text_edit: Long = 0


    private val changeSuggestionWhenUserStopped = Runnable {
        changeSuggestions(autoCompleteTextView!!.text.toString(), adapter!!)
    }

    override fun onDestroy() {
        databaseAccess?.close()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        connectToDatabase()

        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        definitionTextView = findViewById(R.id.text)
        definitionTextView?.textSize = 20f

        val listen = findViewById<Button>(R.id.listen)
        val ara = findViewById<Button>(R.id.arama)
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        autoCompleteTextView?.textSize = 20f

        val lst = ArrayList<String>()
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, lst
        )

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        autoCompleteTextView?.setAdapter(adapter)
        changeSuggestions(null, adapter!!)



        autoCompleteTextView?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(mEdit: Editable) {
                last_text_edit = System.currentTimeMillis()
                if (mEdit.length > 0) {
                    handler.postDelayed(changeSuggestionWhenUserStopped, 100)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                /*
                Dont need this
                */
                //You need to remove this to run only once
                handler.removeCallbacks(changeSuggestionWhenUserStopped)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                /*
                Dont need this either
                */
            }
        })

        //Set keyboard button to fire search event
        autoCompleteTextView?.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchedWord = autoCompleteTextView?.getText().toString().trim { it <= ' ' }
                autoCompleteTextView?.dismissDropDown()
                fetchAndDisplay(searchedWord)
                return@setOnEditorActionListener true
            }
            false
        }

        autoCompleteTextView?.setOnFocusChangeListener {
            view: View?, b: Boolean ->
            autoCompleteTextView?.showDropDown()
        }

        autoCompleteTextView?.setOnItemClickListener { parent: AdapterView<*>?, arg1: View?, pos: Int, id: Long ->
            val searchedWord = autoCompleteTextView?.getText().toString().trim { it <= ' ' }
            fetchAndDisplay(searchedWord)
        }

        ara.setOnClickListener { v: View? ->
            val searchedWord = autoCompleteTextView?.getText().toString().trim { it <= ' ' }
            fetchAndDisplay(searchedWord)
        }

        listen.setOnClickListener { v: View? ->
            val searchedWord = autoCompleteTextView?.getText().toString().trim { it <= ' ' }
            getAudioFromWeb(searchedWord)
            fetchAndDisplay(searchedWord)
        }
    }


    private fun connectToDatabase() {
        databaseAccess = DatabaseAccess.getInstance(this)
        try {
            databaseAccess?.open()
        } catch (e: RuntimeException) {
            Log.e("app", "exception", e)
        }
    }

    private fun getAudioFromWeb(word: String) {
        val thread = Thread {
            val queue = Volley.newRequestQueue(this)
            val url = "https://sozluk.gov.tr/yazim?ara=$word"

            val stringRequest = StringRequest(
                Request.Method.POST, url,

                { response: String? ->
                    try {
                        val reader = JSONArray(response)
                        val list = reader[0] as JSONObject

                        if (list["seskod"] == "") {
                            val toast = Toast.makeText(
                                applicationContext,
                                "Aranan sözcük için ses kaydı bulunmuyor.",
                                Toast.LENGTH_SHORT
                            )
                            toast.show()
                        } else {
                            val urlEnd = "https://sozluk.gov.tr/ses/" + list["seskod"] + ".wav"
                            val mp = MediaPlayer()
                            playPronunciation(urlEnd, mp)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, { error: VolleyError ->
                    val toast = Toast.makeText(
                        applicationContext,
                        "Bu özelliği kullanabilmek için internet bağlantınızın olması gerekmektedir.",
                        Toast.LENGTH_LONG
                    )
                    error.printStackTrace()
                    toast.show()
                }
            )
            queue.add(stringRequest)
        }

        thread.start()
    }

    private fun playPronunciation(uri: String, mp: MediaPlayer) {
        try {
            mp.reset()
            mp.setDataSource(uri)
            mp.prepare()
            mp.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun changeSuggestions(word: String?, adapter: ArrayAdapter<String>) {
        val words: Array<String?>
        if (!(word == null || word == "")) {
            words = databaseAccess!!.getSuggestions(word)
            adapter.clear()


            for (word1 in words) {
                if (word1 != null) {
                    adapter.add(word1)
                }
            }

            adapter.filter.filter("", null)
        }
    }

    fun addTurkishCharacter(view: View) {
        val pressedButton = view as Button
        val position = autoCompleteTextView!!.selectionStart
        val text = autoCompleteTextView!!.text.toString()
        var poststring = ""
        if (text.length > position) poststring = text.substring(position)
        val newword = text.substring(0, position) + pressedButton.text.toString() + poststring
        autoCompleteTextView!!.setText(newword)
        autoCompleteTextView!!.setSelection(position + 1)
    }

    fun fetchAndDisplay(word: String) {
        runOnUiThread {
            var definition = databaseAccess!!.getDefinition(word)
            if (definition == null) {
                definitionTextView!!.text =
                    DEFINITION_NOT_FOUND
            } else {
                definition = definition.replace("</tr>", "</tr><br>")
                definitionTextView!!.text = Html.fromHtml(definition, Html.FROM_HTML_MODE_LEGACY)
                definitionTextView!!.setTextIsSelectable(true)
            }
        }
    }

    companion object {
        private const val DEFINITION_NOT_FOUND = "Sözcük bulunamadı."
    }
}