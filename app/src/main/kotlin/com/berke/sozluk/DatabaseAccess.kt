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

    fun getAllWords(): List<String> {
        val words = mutableListOf<String>()
        val cursor = database!!.rawQuery("SELECT word FROM words", null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            words.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        return words
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

    companion object {
        private lateinit var instance: DatabaseAccess

        fun getInstance(context: Context): DatabaseAccess {
            if (!::instance.isInitialized) {
                instance = DatabaseAccess(context)
            }
            return instance
        }
    }
}