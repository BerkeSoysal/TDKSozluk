package com.berke.sozluk

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseAccess
private constructor(context: Context) {
    private val openHelper: SQLiteOpenHelper = DatabaseOpenHelper(context)
    private var database: SQLiteDatabase? = null

    fun open() {
        this.database = openHelper.writableDatabase
    }

    fun close() {
        if (database != null) {
            database!!.close()
        }
    }

    fun getDefinition(word: String): String? {
        val cursor = database!!.rawQuery(
            "SELECT meaning FROM words WHERE TRIM(word) = '$word' COLLATE NOCASE",
            null
        )
        cursor.moveToFirst()

        if (cursor.count > 0) {
            val definition = cursor.getString(0)
            cursor.close()
            return definition
        }
        return null
    }

    fun getSuggestions(word: String): Array<String?> {
        val words = arrayOfNulls<String>(5)
        val dbWord = "$word%"
        val cursor = database!!.rawQuery(
            "SELECT word FROM words WHERE TRIM(word) LIKE ? LIMIT 5",
            arrayOf(dbWord)
        )
        cursor.moveToFirst()
        var i = 0
        while (i < words.size && i < cursor.count) {
            words[i] = cursor.getString(0)
            cursor.moveToNext()
            i++
        }
        cursor.close()
        return words
    }

    companion object {
        private var instance: DatabaseAccess? = null

        fun getInstance(context: Context): DatabaseAccess? {
            if (instance == null) {
                instance = DatabaseAccess(context)
            }
            return instance
        }
    }
}