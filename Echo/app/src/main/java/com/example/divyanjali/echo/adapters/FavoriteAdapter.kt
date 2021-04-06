package com.example.divyanjali.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.divyanjali.echo.R
import com.example.divyanjali.echo.Songs
import com.example.divyanjali.echo.fragments.SongPlayingFragment

class FavoriteAdapter(_songDetalis: ArrayList<Songs>, _context: Context) : RecyclerView.Adapter<FavoriteAdapter.MyViewHolder>(){
    var songDetails:ArrayList<Songs>?= null
    var mContext : Context? = null
    init {
        this.songDetails = _songDetalis
        this.mContext= _context
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val songobject = songDetails?.get(position)

        holder.trackTitle?.setText(songobject?.songTitle)
        holder.trackArtists?.setText(songobject?.artist)
        holder.contentHolder?.setOnClickListener( {//on click lisner method
            var args = Bundle()
            val songPlayingFragment = SongPlayingFragment()

            args.putString("songArtist", songobject?.artist)
            args.putString("path" , songobject?.songData)
            args.putString("songTitle" , songobject?.songTitle)
            args.putInt("SongId" , songobject?.songsID?. toInt() as Int)
            args.putInt("songPostinon" , position)
            args.putParcelableArrayList("songData" , songDetails)

            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .addToBackStack("SongPlayingFragmentFavorits")
                .commit()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.row_custom_mainscreen_adapter, parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if (songDetails == null){
            return 0
        }else {
            return (songDetails as ArrayList<Songs>).size
        }
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        var trackTitle: TextView? = null
        var trackArtists : TextView? = null
        var contentHolder : RelativeLayout?= null

        init {
            trackTitle= view.findViewById<TextView>(R.id.trackTitle)
            trackArtists = view.findViewById<TextView>(R.id.trackTitle)
            contentHolder = view.findViewById<RelativeLayout>(R.id.contentRow)
        }
    }
}