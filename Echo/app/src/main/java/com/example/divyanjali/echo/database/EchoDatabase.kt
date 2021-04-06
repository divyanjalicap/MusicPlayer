package com.example.divyanjali.echo.database

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.divyanjali.echo.Songs
import java.lang.Exception

class EchoDatabase: SQLiteOpenHelper{

    var  _songList = ArrayList<Songs>()



    object Staticated{
        var DB_VERSION = 1
        val DB_NAME = "FavoriteDatabase"
        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        sqLiteDatabase?.execSQL("CREATE TABLE " + Staticated.TABLE_NAME + " ( " + Staticated.COLUMN_ID + " INTEGER," + Staticated.COLUMN_SONG_ARTIST +
                " STRING," + Staticated.COLUMN_SONG_TITLE + " STRING," + Staticated.COLUMN_SONG_PATH + " STRING);") //executable file for the database it create the table in database

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(
        context,
        name,
        factory,
        version
    )
    constructor(context: Activity?) : super(context, Staticated.DB_NAME , null, Staticated.DB_VERSION)

    fun storeAsFavorite(id: Int?, artist: String?, songTitle: String?, path: String?){
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(Staticated.COLUMN_ID, id)
        contentValues.put(Staticated.COLUMN_SONG_ARTIST, artist)
        contentValues.put(Staticated.COLUMN_SONG_PATH, songTitle)
        contentValues.put(Staticated.COLUMN_SONG_TITLE, path)
        db.insert(Staticated.TABLE_NAME, null,contentValues)
        db.close() 
    }

    fun queryDBList(): ArrayList<Songs>?{
        try {
            val db = this.readableDatabase //if data to be writable then use this.writableDatbase
            val query_params = "SELECT * FROM " + Staticated.TABLE_NAME
            var cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()){
                do{
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))         //if column exist then throw it otherwise throw error
                    var _aritst = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_PATH))
                    _songList.add(Songs(_id.toLong(), _title, _aritst, _songPath, 0))

                }while (cSor.moveToNext())
            }else{
                return null
            }

        }catch (e:Exception){
            e.printStackTrace()
        }
        return _songList
    }

    fun cheackifIdExist(_id: Int): Boolean{
        var storeId = -1090                 //it is a default value
        val db = this.readableDatabase
        val query_params = "SELECT * FROM " + EchoDatabase.Staticated.TABLE_NAME+ " WHERE SongId = '$_id'"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()){
            do {
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))
            }while (cSor.moveToNext())
        }else{
            return false
        }
        return  storeId !=-1090
    }

    fun deletFavourit(_id: Int){
        val db = this.writableDatabase
        db.delete(Staticated.TABLE_NAME, Staticated.COLUMN_ID + "=" + _id,null)//to delet the song from favourit
        db.close()
    }

    fun cheackSize(): Int{
        var counter = 0
        val db = this.readableDatabase
        var query_params = "SELECT * FROM " + Staticated.TABLE_NAME
        val cSor = db.rawQuery(query_params, null)
        if(cSor.moveToFirst()){
            do {
                counter = counter+1
            }while (cSor.moveToNext())
        }
        else{
            return 0
        }
        return counter
    }
}