package com.berke.sozluk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseAccess
{
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    private DatabaseAccess(Context context)
    {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if( instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public void open()
    {
        this.database = openHelper.getWritableDatabase();
    }

    public void close()
    {
        if(database != null)
        {
            this.database.close();
        }
    }

    public String getDefinition(String word)
    {
        Cursor cursor = database.rawQuery("SELECT meaning FROM words WHERE TRIM(word) = '"+ word + "' COLLATE NOCASE",null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            String definition = cursor.getString(0);
            cursor.close();
            return definition;
        }
        return null;
    }

    public String[] getSuggestions(String word)
    {
        String[] words= new String[5];

        Cursor cursor = database.rawQuery("SELECT word FROM words WHERE TRIM(word) LIKE '"+word+"%' LIMIT 5",null);
        cursor.moveToFirst();
        for(int i =0; i<words.length && i < cursor.getCount(); i++)
        {
            words[i] = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();
        return words;
    }

    //TODO implement this method
    public void saveToCache(String word)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("word", word);
        long value = database.insert("CACHE",null, contentValues);
        value += 3;

        Cursor cursor = database.rawQuery("SELECT word FROM CACHE",null);
        cursor.moveToFirst();
        for(int i=0; i< cursor.getCount();i++){
            String a = cursor.getString(0);
            a+="";
            cursor.moveToNext();
        }
    }

    public String[] getSuggestionsFromCache()
    {
        String[] words= new String[5];

        Cursor cursor = database.rawQuery("SELECT word FROM CACHE LIMIT 5",null);
        cursor.moveToFirst();
        for(int i =0; i<words.length && i < cursor.getCount(); i++)
        {
            words[i] = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();
        return words;
    }
}
