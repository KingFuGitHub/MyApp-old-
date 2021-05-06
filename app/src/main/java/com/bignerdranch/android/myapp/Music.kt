package com.bignerdranch.android.myapp


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.music.*
import kotlinx.android.synthetic.main.music_popup_window.*
import kotlinx.android.synthetic.main.nav_left_header.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class Music : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    var curFile: Uri? = null
    val imageRef = Firebase.storage.reference
    val songRef = Firebase.storage.reference
    private var currentSongIndex = 0
    private var runnable: Runnable? = null
    private var handler = Handler(Looper.getMainLooper())
    private var autoPlay = true
    private val CHANNEL_ID = "channel_id"
    private val notificationId = 1

    private var mp: MediaPlayer? = null

    private var currentSong = arrayListOf(
        R.raw.congratulations,
        R.raw.in_the_end,
        R.raw.love_me_like_you_do,
        R.raw.dont_turn_back,
        R.raw.young_and_beautiful,
        R.raw.my_heart_will_go_on
    )
    private var picture = arrayListOf<ImageView>()
    private var title = arrayListOf<TextView>()
    private var title2 = arrayListOf<String>()
    private lateinit var currentPicture: ImageView
    private lateinit var currentTitle: TextView
    private var marqueeText = true
    private var totalSong = currentSong.size
    private var addSongCheck = false
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.music)

        window.decorView.toggleVisibility()
        createNotificationChannel()

        picture.add(0, ivCongratulations)
        picture.add(1, ivInTheEnd)
        picture.add(2, ivLoveMeLikeYouDo)
        picture.add(3, ivDontTurnBack)
        picture.add(4, ivYoungAndBeautiful)
        picture.add(5, ivMyHeartWillGoOn)
        currentPicture = picture[0]

        title.add(0, tvCongratulations)
        title.add(1, tvInTheEnd)
        title.add(2, tvLoveMeLikeYouDo)
        title.add(3, tvDontTurnBack)
        title.add(4, tvYoungAndBeautiful)
        title.add(5, tvMyHeartWillGoOn)
        currentTitle = title[0]


        title2.add(0, "Congratulations by PewDiePie")
        title2.add(1, "In The End by Linkin Park")
        title2.add(2, "Love Me Like You Do by Ellie Groulding")
        title2.add(3, "Don't Turn Back by Silent Partner")
        title2.add(4, "Young and Beautiful by Lana Del Rey")
        title2.add(5, "My Heart Will Go On by Celine Dion")


        val actvSearchSong = findViewById<AutoCompleteTextView>(R.id.actvSearchSong)
        val song_Name = resources.getStringArray(R.array.song_name)
        val adapter =
            ArrayAdapter(this, R.layout.auto_complete_search_music, R.id.tvCustom, song_Name)
        actvSearchSong.setAdapter(adapter)

        actvSearchSong.setOnItemClickListener { _, _, position, _ ->
            val value = adapter.getItem(position) ?: String
            if (value == "Congratulations") {
                selectSong(0)

            } else if (value == "In The End") {
                selectSong(1)

            } else if (value == "Love Me Like You Do") {
                selectSong(2)

            } else if(value == "Dont Turn Back"){
                selectSong(3)

            } else if(value == "Young and Beautiful"){
                selectSong(4)

            } else if(value == "My Heart Will Go On"){
                selectSong(5)

            }
            actvSearchSong.setText("")
            actvSearchSong.clearListSelection()
            val inputMethodManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                actvSearchSong.windowToken,
                0
            ) // to hide soft keyboard
            actvSearchSong.isCursorVisible = false
        }


        ivPlay.setOnClickListener {

            if (mp == null) {
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
            }
            if (mp?.isPlaying == true) {
                mp?.pause()
                if (marqueeText == true) {
                    currentTitle.isSelected = false
                }else if(marqueeText == false){
                    currentTitle.isSelected = false
                }
                ivPlay.setImageResource(R.drawable.ic_play)
            } else {
                mp?.start()
                if (marqueeText == true) {
                    currentTitle.isSelected = true
                }else if( marqueeText == false){
                    currentTitle.isSelected = false
                }
                ivPlay.setImageResource(R.drawable.ic_pause)
            }

            sendNotification()
        }


        ivPrevious.setOnClickListener {
            YoYo.with(Techniques.Landing).playOn(ivPrevious)

            currentPicture.toggleVisibility()
            currentTitle.toggleVisibility()

            currentSongIndex = (currentSongIndex + (currentSong.size - 1)) % totalSong

            togglePictureAndTitle()

            mp?.stop()
            mp?.reset()
            mp?.release()
            mp = MediaPlayer.create(this, currentSong[currentSongIndex])
            initialiseSeekBar()
            mp?.start()
            sendNotification()

        }

        ivSkip.setOnClickListener {
            YoYo.with(Techniques.Landing).playOn(ivSkip)
            currentPicture.toggleVisibility()
            currentTitle.toggleVisibility()

            currentSongIndex = (currentSongIndex + 1) % totalSong

            togglePictureAndTitle()

            mp?.stop()
            mp?.reset()
            mp?.release()
            mp = MediaPlayer.create(this, currentSong[currentSongIndex])
            initialiseSeekBar()
            mp?.start()

            sendNotification()
        }

        seekBarMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, change: Boolean) {
                if (change) {
                    mp?.seekTo(progress)
                }
            }


            override fun onStartTrackingTouch(seekBar: SeekBar?) {


            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })


        toggle = ActionBarDrawerToggle(this, drawerMusic, R.string.open, R.string.close)

        drawerMusic.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        downloadImage("profilePicture")

        navViewLeftMusic.setNavigationItemSelectedListener {

            val toast = Toast.makeText(this, "You're currently in music", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)

            when (it.itemId) {
                R.id.miHome -> Intent(this, Home::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                    runnable?.let { it -> handler.removeCallbacks(it) }
                }

                R.id.miChat -> Intent(this, Chat::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                    runnable?.let { it -> handler.removeCallbacks(it) }
                }

                R.id.miGames -> Intent(this, Game::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                    runnable?.let { it -> handler.removeCallbacks(it) }
                }

                R.id.miMusic -> toast.show()

                R.id.miProfile -> Intent(this, Profile::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                    runnable?.let { it -> handler.removeCallbacks(it) }
                }

                R.id.miLogout -> Intent(this, Login::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                    runnable?.let { it -> handler.removeCallbacks(it) }
                }

            }
            true
        }


        navViewMusic.setNavigationItemSelectedListener {
            val toast1 = Toast.makeText(this, "You clicked on a song", Toast.LENGTH_SHORT)
            toast1.setGravity(Gravity.TOP, 0, 0)

            val toast2 = Toast.makeText(this, "Song is currently selected", Toast.LENGTH_SHORT)
            toast2.setGravity(Gravity.TOP, 0, 0)

            when (it.itemId) {
                R.id.miCongratulation -> selectSong(0)

                R.id.miInTheEnd -> selectSong(1)

                R.id.miLoveMeLikeYouDo -> selectSong(2)

                R.id.miDontTurnBack -> selectSong(3)

                R.id.miYoungAndBeautiful -> selectSong(4)

                R.id.miMyHeartWillGoOn -> selectSong(5)

                R.id.miAddSong ->
                    if (addSongCheck == false) {
                        Intent(Intent.ACTION_GET_CONTENT).also {
                            it.type = "audio/*"
                            startActivityForResult(it, 1)
                        }
                    } else {
                        mp?.setDataSource("https://firebasestorage.googleapis.com/v0/b/myapp-7a885.appspot.com/o/songs%2FSong%201?alt=media&token=a1a8351a-2629-4164-ba1b-7f10e46f3f2d")
                        mp?.prepare()
                        mp?.start()
                        Toast.makeText(this, "Song 1 is playing", Toast.LENGTH_SHORT).show()
                    }

            }
            true
        }

        var count = 1
        ibEmptyHeart.setOnClickListener {
            if (count % 2 == 1) {
                ibRedHeart.toggleVisibility()
                YoYo.with(Techniques.Bounce).playOn(ibRedHeart)
                YoYo.with(Techniques.Bounce).playOn(ibEmptyHeart)
                ibEmptyHeart.scaleX = 1.3f
                ibEmptyHeart.scaleY = 1.3f
                val toast1 = Toast.makeText(this, "You liked the song", Toast.LENGTH_SHORT)
                toast1.setGravity(Gravity.BOTTOM, 0, 0)
                toast1.show()
            } else {
                YoYo.with(Techniques.FadeOutDown).duration(1000).playOn(ibRedHeart)
                YoYo.with(Techniques.Shake).duration(1000).playOn(ibEmptyHeart)
                ibEmptyHeart.scaleX = 1.3f
                ibEmptyHeart.scaleY = 1.3f
                val toast2 = Toast.makeText(this, "You unliked the song", Toast.LENGTH_SHORT)
                toast2.setGravity(Gravity.BOTTOM, 0, 0)
                toast2.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    ibRedHeart.toggleVisibility()
                }, 1000)
            }
            count++
        }

        ibMoreMusic.setOnClickListener {
            currentTitle.isSelected = marqueeText
            val popup = PopupMenu(this, ibMoreMusic)
            popup.menuInflater.inflate(R.menu.pop_up_music, popup.menu)
            popup.setOnMenuItemClickListener {
                currentTitle.isSelected = marqueeText
                when (it.itemId) {
                    R.id.miMusicSettings -> {
                        val view =
                            LayoutInflater.from(this).inflate(R.layout.music_popup_window, null)
                        val switchCompat1 =
                            view.findViewById<SwitchCompat>(R.id.scAutoPlayMusic)
                        val switchCompat2 = view.findViewById<SwitchCompat>(R.id.scMarqeeText)

                        switchCompat1.isChecked = autoPlay
                        switchCompat1.setOnCheckedChangeListener { _, isChecked ->
                            autoPlay = isChecked
                        }

                        currentTitle.isSelected = marqueeText
                        switchCompat2.isChecked = marqueeText
                        switchCompat2.setOnCheckedChangeListener { _, isChecked ->
                            marqueeText = isChecked
                        }

                        AlertDialog.Builder(this)
                            .setView(view)
                            .show()
                    }
                }
                true
            }
            popup.show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mp?.stop()
        mp?.release()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun chooseImage(view: View) {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            startActivityForResult(it, 0)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            data?.data?.let {
                curFile = it
                ivProfilePicture.setImageURI(it)
                uploadImageToStorage("profilePicture") // to upload profile picture to Firebase Cloud Storage
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            data?.data?.let {
                curFile = it
                ivSong1.setImageURI(curFile)
                uploadSongToStorage("Song 1") // to upload song to Firebase Cloud Storage
            }
        }
    }

    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Music, "Successfully uploaded image", Toast.LENGTH_LONG)
                        .show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Music, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadSongToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                songRef.child("songs/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Music, "Successfully uploaded song", Toast.LENGTH_LONG)
                        .show()
                    val menu: Menu = navViewMusic.menu
                    val addSong = menu.findItem(R.id.miAddSong)
                    addSong.title = "Song 1"
                    addSong.setIcon(R.drawable.ic_note)
                    addSongCheck = true
                    totalSong++
                    title.add(3, tvSong1)
                    picture.add(3, ivSong1)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Music, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun downloadImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            withContext(Dispatchers.Main) {
                ivProfilePicture.setImageBitmap(bmp)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Music, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initialiseSeekBar() {

        seekBarMusic.max = mp!!.duration

        runnable = Runnable {
            seekBarMusic.progress = mp!!.currentPosition
            runnable?.let { handler.postDelayed(it, 0) }
            if (mp?.isPlaying == true) {
                ivPlay.setImageResource(R.drawable.ic_pause)
                currentTitle.isSelected = marqueeText
            } else {
                ivPlay.setImageResource(R.drawable.ic_play)
            }
        }
        handler.postDelayed(runnable!!, 0)

        mp?.setOnCompletionListener {
            if (autoPlay == true) {
                YoYo.with(Techniques.Landing).playOn(ivSkip)

                currentPicture.toggleVisibility()
                currentTitle.toggleVisibility()

                currentSongIndex = (currentSongIndex + 1) % totalSong

                togglePictureAndTitle()
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
                mp?.start()
                sendNotification()
            }
        }

    }


    private fun View.toggleVisibility() {
        if (visibility == View.VISIBLE) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
        }
    }

    private fun selectSong(jumpSongIndex: Int) {
        val toast2 = Toast.makeText(this, "Song is currently selected", Toast.LENGTH_SHORT)
        toast2.setGravity(Gravity.TOP, 0, 0)

        if (jumpSongIndex == currentSongIndex && mp != null) {
                toast2.show()
            } else {
                currentPicture.toggleVisibility()
                currentTitle.toggleVisibility()
                currentSongIndex = jumpSongIndex
                togglePictureAndTitle()
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
                mp?.start()
                sendNotification()
            }
    }

    private fun togglePictureAndTitle() {
        picture[currentSongIndex].toggleVisibility()
        title[currentSongIndex].toggleVisibility()
        currentPicture = picture[currentSongIndex]
        currentTitle = title[currentSongIndex]
        currentTitle.isSelected = marqueeText
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Closing Activity")
            .setMessage("Are you sure you want to close this activity?")
            .setPositiveButton(
                "Yes"
            ) { dialog, which ->
                run {
                    runnable?.let { handler.removeCallbacks(it) }
                    finish()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        val intent = Intent(this, Music::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val bitmapLargeIcon =
            BitmapFactory.decodeResource(resources, R.drawable.ic_music)


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_note)
            .setContentTitle(title2[currentSongIndex])
            .setContentText("Description")
            .setLargeIcon(bitmapLargeIcon)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.red))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

}



