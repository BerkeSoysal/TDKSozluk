package com.berke.sozluk

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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.berke.sozluk.databinding.ActivityMainBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: WordViewModel
    private lateinit var wordRepository: WordRepository
    private var adapter: ArrayAdapter<String>? = null
    private lateinit var binding: ActivityMainBinding
    private var debounceJob: Job? = null

    override fun onDestroy() {
        wordRepository.close()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val databaseAccess = DatabaseAccess.getInstance(this)
        val repository = WordRepository(databaseAccess)
        viewModel = ViewModelProvider(this, ViewModelFactory(repository))[WordViewModel::class.java]

        viewModel.definition.observe(this) { definition ->
            binding.text.text = Html.fromHtml(definition, Html.FROM_HTML_MODE_LEGACY)
        }
        val lst = ArrayList<String>()
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, lst
        )

        viewModel.suggestions.observe(this) { suggestions ->
            adapter?.clear()
            adapter?.addAll(suggestions)
            adapter?.notifyDataSetInvalidated()
            adapter?.notifyDataSetChanged()
            adapter?.getFilter()?.filter(binding.autoCompleteTextView.text.toString(), binding.autoCompleteTextView);
        }

        val autoCompleteTextView = binding.autoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Cancel the previous job
                debounceJob?.cancel()

                // Start a new coroutine for debouncing
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    //delay(50) // 1-second debounce
                    val text = s?.toString()
                    changeSuggestions(text)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed here
            }
        })

            //Set keyboard button to fire search event
        binding.autoCompleteTextView.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchedWord = binding.autoCompleteTextView.getText().toString().trim { it <= ' ' }
                binding.autoCompleteTextView.dismissDropDown()
                viewModel.fetchDefinition(searchedWord)
                return@setOnEditorActionListener true
            }
            false
        }

        binding.autoCompleteTextView.setOnFocusChangeListener { _: View?, _: Boolean ->
            binding.autoCompleteTextView.showDropDown()
        }

        binding.autoCompleteTextView.setOnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
            val searchedWord = binding.autoCompleteTextView.getText().toString().trim { it <= ' ' }
            viewModel.fetchDefinition(searchedWord)
        }

        binding.arama.setOnClickListener {
            val searchedWord = binding.autoCompleteTextView.getText().toString().trim { it <= ' ' }
            viewModel.fetchDefinition(searchedWord)

        }

        binding.listen.setOnClickListener {
            val searchedWord = binding.autoCompleteTextView.getText().toString().trim { it <= ' ' }
            getAudioFromWeb(searchedWord)
            viewModel.fetchDefinition(searchedWord)
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

    private fun changeSuggestions(typedWord: String?) {
        viewModel.fetchSuggestions(typedWord!!)
    }

    fun addTurkishCharacter(view: View) {
        val pressedButton = view as Button
        val position = binding.autoCompleteTextView.selectionStart
        binding.autoCompleteTextView.editableText.insert(position, pressedButton.text.toString())
    }

    companion object {
        private const val DEFINITION_NOT_FOUND = "Sözcük bulunamadı."
        private const val LISTEN_NO_INTERNET_AVAILABLE = "Bu özelliği kullanabilmek için internet bağlantınızın olması gerekmektedir."
        private const val LISTEN_RECORD_NOT_FOUND = "Aranan sözcük için ses kaydı bulunamadı."
        private const val LISTEN_AUDIO_CODE = "seskod"
    }
}