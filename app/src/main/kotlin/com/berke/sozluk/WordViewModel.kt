package com.berke.sozluk

import Trie
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WordViewModel(private val repository: WordRepository): ViewModel() {

    private val _definition = MutableLiveData<String>()
    val definition: LiveData<String> = _definition
    private val _word = MutableLiveData<String>()
    val word: LiveData<String> = _word

    private val _suggestions = MutableLiveData<List<String>>()
    val suggestions: LiveData<List<String>> = _suggestions

    private val trie = Trie()

    init {
        viewModelScope.launch {
            val allWords = repository.getAllWords()
            allWords.forEach{ trie.insert(it) }
        }
    }

    fun fetchDefinition(word: String) {
        viewModelScope.launch {
            val result = repository.getDefinition(word)
            _definition.postValue(result ?: "Sözcük bulunamadı.")
        }
    }

    fun fetchSuggestions(prefix: String) {
        val results = trie.searchByPrefix(prefix)
        Log.d("WordViewModel", "Suggestions: $results")
        _suggestions.postValue(results)
    }

    fun fetchRandomWord() {
        viewModelScope.launch {
            val result = repository.fetchRandomWord()
            _definition.postValue(result?.get(1) ?: "Sözcük bulunamadı.")
            _word.postValue(result?.get(0) ?: "")
        }
    }
}