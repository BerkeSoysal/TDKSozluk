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
import com.berke.sozluk.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : Activity() {
    private lateinit var databaseAccess: DatabaseAccess
    var handler: Handler = Handler(Looper.myLooper()!!)
    private var adapter: ArrayAdapter<String>? = null
    private lateinit var binding: ActivityMainBinding
    var last_text_edit: Long = 0
    private val changeSuggestionWhenUserStopped = Runnable {
        changeSuggestions(binding.autoCompleteTextView.text.toString(), adapter!!)
    }
    override fun onDestroy() {
        databaseAccess.close()
        super.onDestroy()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        connectToDatabase()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lst = ArrayList<String>()
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, lst
        )

        val autoCompleteTextView = binding.autoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)
        changeSuggestions(null, adapter!!)



        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
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
        binding.autoCompleteTextView.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchedWord = binding.autoCompleteTextView.getText().toString().trim { it <= ' ' }
                binding.autoCompleteTextView.dismissDropDown()
                fetchAndDisplay(searchedWord)
                return@setOnEditorActionListener true
            }
            false
        }

        binding.autoCompleteTextView.setOnFocusChangeListener { _: View?, _: Boolean ->
            binding.autoCompleteTextView.showDropDown()
        }

        binding.autoCompleteTextView.setOnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
            val searchedWord = binding.autoCompleteTextView.getText().toString().trim { it <= ' ' }
            fetchAndDisplay(searchedWord)
        }

        binding.arama.setOnClickListener {
            val searchedWord = binding.autoCompleteTextView.getText().toString().trim { it <= ' ' }
            fetchAndDisplay(searchedWord)
        }

        binding.listen.setOnClickListener {
            val searchedWord = binding.autoCompleteTextView.getText().toString().trim { it <= ' ' }
            getAudioFromWeb(searchedWord)
            fetchAndDisplay(searchedWord)
        }
    }


    private fun connectToDatabase() {
        databaseAccess = DatabaseAccess.getInstance(this)
        try {
            databaseAccess.open()
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

                        if (list[LISTEN_AUDIO_CODE] == "") {
                            val toast = Toast.makeText(
                                applicationContext,
                                LISTEN_RECORD_NOT_FOUND,
                                Toast.LENGTH_SHORT
                            )
                            toast.show()
                        } else {
                            val urlEnd = "https://sozluk.gov.tr/ses/" + list[LISTEN_AUDIO_CODE] + ".wav"
                            val mp = MediaPlayer()
                            playPronunciation(urlEnd, mp)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, { error: VolleyError ->
                    val toast = Toast.makeText(
                        applicationContext,
                        LISTEN_NO_INTERNET_AVAILABLE,
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
            mp.setOnCompletionListener { mediaPlayer ->
                mediaPlayer.release() // Release MediaPlayer when playback finishes
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun changeSuggestions(word: String?, adapter: ArrayAdapter<String>) {
        val words: Array<String?>
        if (!(word == null || word == "")) {
            words = databaseAccess.getSuggestions(word)
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
        val position = binding.autoCompleteTextView.selectionStart
        binding.autoCompleteTextView.editableText.insert(position, pressedButton.text.toString())
    }

    private fun fetchAndDisplay(word: String) {
        var definition = databaseAccess.getDefinition(word)
        if (definition == null) {
            binding.text.text =
                DEFINITION_NOT_FOUND
        } else {
            definition = definition.replace("</tr>", "</tr><br>")
            binding.text.text = Html.fromHtml(definition, Html.FROM_HTML_MODE_LEGACY)
            binding.text.setTextIsSelectable(true)
        }
    }

    companion object {
        private const val DEFINITION_NOT_FOUND = "Sözcük bulunamadı."
        private const val LISTEN_NO_INTERNET_AVAILABLE = "Bu özelliği kullanabilmek için internet bağlantınızın olması gerekmektedir."
        private const val LISTEN_RECORD_NOT_FOUND = "Aranan sözcük için ses kaydı bulunamadı."
        private const val LISTEN_AUDIO_CODE = "seskod"
    }
}