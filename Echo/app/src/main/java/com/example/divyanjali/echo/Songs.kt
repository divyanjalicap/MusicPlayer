package com.example.divyanjali.echo

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

@SuppressLint("ParcelCreator")
class Songs(var songsID : Long, var songTitle : String, var artist: String, var  songData : String, var dateAdded: Long): Parcelable{
    override fun writeToParcel(dest: Parcel?, flags: Int) {

    }


    override fun describeContents(): Int {
      return 0
    }
    object Statified{
        var nameCompatator: Comparator<Songs> = Comparator<Songs>{song1, song2 ->
         val SongOne = song1?.songTitle?.toUpperCase()
            val songTwo = song2?.songTitle?.toUpperCase()
            SongOne!!.compareTo(songTwo as String)

        }
        var dateComparator : Comparator<Songs> = Comparator<Songs>{song1 , song2 ->
            val songOne = song1.dateAdded.toDouble()
            val songTwo = song2.dateAdded.toDouble()
            songTwo.compareTo(songOne)
        }
    }

}