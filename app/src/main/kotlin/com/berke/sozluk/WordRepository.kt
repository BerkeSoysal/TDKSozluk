package com.berke.sozluk

class WordRepository(private val databaseAccess: DatabaseAccess) {

    fun getAllWords(): List<String> {
        return databaseAccess.getAllWords()
    }

    fun getDefinition(word: String): String? {
        return databaseAccess.getDefinition(word)
    }
}
