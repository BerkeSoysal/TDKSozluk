package com.berke.sozluk

class WordRepository(private val databaseAccess: DatabaseAccess) {

    fun getAllWords(): List<String> {
        return databaseAccess.getAllWords()
    }

    fun getDefinition(word: String): String? {
        return databaseAccess.getDefinition(word)
    }

    fun close() {
        return databaseAccess.close()
    }

    fun fetchRandomWord(): List<String>? {
        return databaseAccess.getRandomWord()
    }
}
