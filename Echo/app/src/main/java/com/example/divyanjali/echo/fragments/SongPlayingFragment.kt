package com.example.divyanjali.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.divyanjali.echo.CurrentSongHelper
import com.example.divyanjali.echo.R
import com.example.divyanjali.echo.R.id.seekBar
import com.example.divyanjali.echo.Songs
import com.example.divyanjali.echo.database.EchoDatabase
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.util.*
import java.util.concurrent.TimeUnit


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {
    object  Statified{
        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playpauseImageButton: ImageButton? = null
        var perviousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var songArtistsView: TextView? = null
        var songTitleView: TextView? = null
        var shuffleImageButton: ImageButton? = null
        var currentPostition: Int = 0
        var fetchSong: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var audioVisualization: AudioVisualization?= null //object for the waves on song
        var glView:GLAudioVisualizationView?= null //object for the waves on song
        var fab: ImageButton?= null// databse for favourit databse
        var favoritContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListner: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongTime = object : Runnable{
            override fun run() {
                val getCurrent= Statified.mediaPlayer?.currentPosition
                Statified.startTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long)-
                            TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()))))
                Statified.seekbar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this,1000)
            }

        }

    }



    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun  onSongCompletion()
        {
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("playNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            }else
            {
                if (Statified.currentSongHelper?.isLoop as Boolean){
                    Statified.currentSongHelper?.isPlaying = true
                    var nextSong = Statified.fetchSong?.get(Statified.currentPostition)

                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.currentPostition = Statified.currentPostition
                    Statified.currentSongHelper?.songId = nextSong?.songsID as Long
                    updateTextView(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
                    Statified.mediaPlayer?.reset()

                    try {
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer) //fixing bugs
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }else{
                    playNext("playNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }
            if (Statified.favoritContent?.cheackifIdExist(Statified.currentSongHelper?.songId?.toInt()as Int)as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_on))
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_off))
            }
        }

        fun updateTextView(songtitle: String , songArtist: String){
            var songTitleUpdated = songtitle
            var songArtistUpadated= songArtist
            if (songtitle.equals("<unknown>", true)){
                songTitleUpdated = "unknown"
            }
            if (songArtist.equals("<unknown>", true)){
                songArtistUpadated = "unknown"
            }
            Statified.songTitleView?.setText(songTitleUpdated)
            Statified.songArtistsView?.setText(songArtist)
        }


        fun processInformation(mediaPlayer: MediaPlayer){
            val finalTime =mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekbar?.max = finalTime
            Statified.startTimeText?.setText(String.format("%d: %d",
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(startTime.toLong())- TimeUnit.MILLISECONDS.toMinutes(1000)))


            Statified.endTimeText?.setText(String.format("%d: %d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong() - TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()))))

            Statified.seekbar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }

        fun playNext(check: String) {
            if (check.equals("playNextLikeNormalShuffle", true)) {
                Statified.currentPostition = Statified.currentPostition + 1
            } else if (check.equals("playNextLinkNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSong?.size?.plus(1) as Int)
                Statified.currentPostition = randomPosition

            }
            if (Statified.currentPostition == Statified.fetchSong?.size){
                Statified.currentPostition = 0
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSong?.get(Statified.currentPostition)
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.currentPostition = Statified.currentPostition
            Statified.currentSongHelper?.songId = nextSong?.songsID as Long
            updateTextView(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)   //fixing bugs okh
            }catch (e: Exception){
                e.printStackTrace()
            }
            if (Statified.favoritContent?.cheackifIdExist(Statified.currentSongHelper?.songId?.toInt()as Int)as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_on))
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_off))
            }
        }

    }

    var mAccelaration: Float = 0f       // for the Acceration or for time changing
    var mAccelarationCurrent: Float = 0f    // for the same
    var mAccelarationLast: Float = 0f     // for the same the time of the song

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now playing"//polishing
       Statified.seekbar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playpauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.perviousImageButton = view?.findViewById(R.id.previousButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.songArtistsView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.glView = view?.findViewById(R.id.visualizer_view) //wave object decleared

        Statified.fab = view?.findViewById(R.id.favoritIcons)
        Statified.fab?.alpha = 0.8f                       // this line is used for dim the color of the red icon of the faviroit icon not complusury as other
        return view
    }


   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

     override fun onResume() {              //call for wave on resume
        super.onResume()
         Statified.audioVisualization?.onResume()
         Statified.mSensorManager?.registerListener(Statified.mSensorListner,Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
             SensorManager.SENSOR_DELAY_NORMAL)
    }

     override fun onPause() {                  //call for on pause method
        super.onPause()
         Statified.audioVisualization?.onPause()
         Statified.mSensorManager?.unregisterListener(Statified.mSensorListner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Statified.audioVisualization?.release()
        super.onDestroyView()
    }

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
         mAccelaration = 0.0f
         mAccelarationCurrent = SensorManager.GRAVITY_EARTH
         mAccelarationLast = SensorManager.GRAVITY_EARTH
         bindShakeListner()

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

     override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
         val item:MenuItem? = menu?.findItem(R.id.action_redirect)
         item?.isVisible = true
         val item2: MenuItem? = menu?.findItem(R.id.action_sort)
         item2?.isVisible = false

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.action_redirect ->{
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favoritContent = EchoDatabase(Statified.myActivity) //i have changed it to context as by pressing the alt+enter
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments?.getString("path") //may be error occure here on this part of code invideo there is no ? but in my code it required
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("SongId")!!.toLong()
            Statified.currentPostition = arguments?.getInt("songPosition")!!
            Statified.fetchSong = arguments?.getParcelableArrayList("songData")


            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.currentPostition = Statified.currentPostition

            Staticated.updateTextView(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromFavBottomBar !=null){
            Statified.mediaPlayer = FavourateFragmant.Statified.mediaPlayer
        }else {

            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(
                    Statified.myActivity,
                    Uri.parse(path)
                ) //unified resource identifier
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer )//bug fixing
        if(Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }else{
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongCompletion()
         }

        clickHandler()
       var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0 )
        Statified.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean){
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }else{
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean){
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        }else{
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            Statified.currentSongHelper?.isLoop = false
        }
        if (Statified.favoritContent?.cheackifIdExist(Statified.currentSongHelper?.songId?.toInt()as Int)as Boolean){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_on))
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_off))
        }

    }

    fun clickHandler() {

        Statified.fab?.setOnClickListener({if (Statified.favoritContent?.cheackifIdExist(Statified.currentSongHelper?.songId?.toInt()as Int)as Boolean){//to color the favirot icon on or off
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_off))
            Statified.favoritContent?.deletFavourit(Statified.currentSongHelper?.songId?.toInt() as Int)
            Toast.makeText(Statified.myActivity,"Remove from favorites", Toast.LENGTH_SHORT).show()
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_on))
            Statified.favoritContent?.storeAsFavorite(Statified.currentSongHelper?.songId?.toInt(), Statified.currentSongHelper?.songArtist,Statified.currentSongHelper?.songTitle,Statified.currentSongHelper?.songPath)
            Toast.makeText(Statified.myActivity,"added to favriots", Toast.LENGTH_SHORT).show()
        }})


        Statified.shuffleImageButton?.setOnClickListener({  //on click lisner method here
            var editorShuffel = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var ediotLoop= Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isShuffle as Boolean){
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffel?.putBoolean("feature", false)
                editorShuffel?.apply()
            }else{
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffel?.putBoolean("feature" , true)
                editorShuffel?.apply()
                ediotLoop?.putBoolean("feature", false)
                ediotLoop?.apply()
            }
        })
        Statified.nextImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if(Statified.currentSongHelper?.isShuffle as Boolean){
                Staticated.playNext("playNextLikeNormalShuffle")}
            else{
                Staticated.playNext("playNextLikeNormalShuffle")
            }

        })
        Statified.perviousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isLoop as Boolean)
            {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })
        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isLoop as Boolean){
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()

            }else{
                Statified.currentSongHelper?.isLoop as Boolean
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature",true)
                editorLoop?.apply()
            }
        })
        Statified.playpauseImageButton?.setOnClickListener({
            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })

    }



    fun playPrevious(){
        Statified.currentPostition = Statified.currentPostition-1
        if (Statified.currentPostition == 1){
            Statified.currentPostition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)//play icon is pause icon
        }else{
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)//pause icon as play icon
        }
        Statified.currentSongHelper?.isLoop= false
        var nextSong = Statified.fetchSong?.get(Statified.currentPostition)
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.currentPostition = Statified.currentPostition
        Statified.currentSongHelper?.songId = nextSong?.songsID as Long
        Staticated.updateTextView(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
        Statified.mediaPlayer?.reset()

        try {
            Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer) //fixing bugs
        }catch (e: Exception){
            e.printStackTrace()
        }
        if (Statified.favoritContent?.cheackifIdExist(Statified.currentSongHelper?.songId?.toInt()as Int)as Boolean){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_on))
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!,R.drawable.favorite_off))
        }

    }

fun bindShakeListner(){
    Statified.mSensorListner = object : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
        override fun onSensorChanged(p0: SensorEvent) {
            val x = p0.values[0]
            val y = p0.values[1]
            val z = p0.values[2]

            mAccelarationLast = mAccelarationCurrent
            mAccelarationCurrent = Math.sqrt(((x*x + y*y + z*z).toDouble())).toFloat()
            val delta = mAccelarationCurrent-mAccelarationLast
            mAccelaration = mAccelaration* 0.9f + delta

            if(mAccelaration > 12){
                val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                val isAllowed = prefs?.getBoolean("feature", false)
                if(isAllowed as Boolean){
                    Staticated.playNext("PlayNextNormal")
                }

            }
        }



    }

}


}
