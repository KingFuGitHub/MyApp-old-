package com.bignerdranch.android.myapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.games.*
import kotlinx.android.synthetic.main.music.*
import kotlinx.android.synthetic.main.music.navView
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
    var currentSongIndex = 0

    private var mp: MediaPlayer? = null

    private var currentSong = arrayListOf(
        R.raw.congratulations,
        R.raw.in_the_end,
        R.raw.love_me_like_you_do
    )
    private var picture = arrayListOf<View>()
    private var title = arrayListOf<View>()
    private lateinit var currentPicture: View
    private lateinit var currentTitle: View


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.music)

        window.decorView.toggleVisibility()

        picture.add(0, ivCongratulation)
        picture.add(1, ivInTheEnd)
        picture.add(2, ivLoveMeLikeYouDo)
        currentPicture = picture[0]

        title.add(0, tvCongratulation)
        title.add(1, tvInTheEnd)
        title.add(2, tvLoveMeLikeYouDo)
        currentTitle = title[0]


        val actvSearchSong = findViewById<AutoCompleteTextView>(R.id.actvSearchSong)
        val song_Name = resources.getStringArray(R.array.song_name)
        val adapter = ArrayAdapter(this, R.layout.auto_complete_search_music, R.id.tvCustom, song_Name)
        actvSearchSong.setAdapter(adapter)

        actvSearchSong.setOnItemClickListener { _, _, position, _ ->
            if (!actvSearchSong.isCursorVisible) {
            }
            val value = adapter.getItem(position) ?: ""
            if (value == "Congratulation") {
                selectSong(0)
            } else if (value == "In The End") {
                selectSong(1)
            } else if (value == "Love Me Like You Do") {
                selectSong(2)
            }
            actvSearchSong.setText("")
            actvSearchSong.clearListSelection()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(actvSearchSong.windowToken, 0) // to hide soft keyboard
            actvSearchSong.isCursorVisible = false
        }



        ivPlay.setOnClickListener {
            YoYo.with(Techniques.Landing).duration(1000).playOn(ivPlay)
            if (mp == null) {
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
            }
            mp?.start()
            togglePlayAndPauseButton()

        }

        ivPause.setOnClickListener {
            YoYo.with(Techniques.Landing).duration(1000).playOn(ivPause)
            if (mp != null) {
                mp?.pause()
            }
            togglePlayAndPauseButton()
        }

        mp?.setOnCompletionListener() {
            Toast.makeText(this, "song finished", Toast.LENGTH_LONG).show()
        }

        ivPrevious.setOnClickListener {
            YoYo.with(Techniques.Landing).playOn(ivPrevious)

            currentPicture.toggleVisibility()
            currentTitle.toggleVisibility()

            if (mp == null) {
                togglePlayAndPauseButton()
                mp = MediaPlayer.create(this, currentSong[currentSong.size - 1])
                initialiseSeekBar()
                mp?.start()

                togglePictureAndTitle(2)

                currentSongIndex = currentSong.size - 1
            } else if (mp != null) {
                if (mp?.isPlaying == false) {
                    togglePlayAndPauseButton()
                }
                if (currentSongIndex % 3 == 0) {

                    togglePictureAndTitle(2)

                } else if (currentSongIndex % 3 == 1) {

                    togglePictureAndTitle(0)

                } else if (currentSongIndex % 3 == 2) {

                    togglePictureAndTitle(1)

                }
                currentSongIndex = currentSongIndex + (currentSong.size - 1)
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = MediaPlayer.create(this, currentSong[currentSongIndex % 3])
                initialiseSeekBar()
                mp?.start()
            }
        }

        ivSkip.setOnClickListener {
            YoYo.with(Techniques.Landing).playOn(ivSkip)

            currentPicture.toggleVisibility()
            currentTitle.toggleVisibility()

            if (mp == null) {
                currentSongIndex++
                togglePlayAndPauseButton()
                mp = MediaPlayer.create(this, currentSong[1])
                initialiseSeekBar()
                mp?.start()

                togglePictureAndTitle(1)

            } else if (mp != null) {

                if (mp?.isPlaying == false) {
                    togglePlayAndPauseButton()
                }
                if (currentSongIndex % 3 == 0) {

                    togglePictureAndTitle(1)

                } else if (currentSongIndex % 3 == 1) {

                    togglePictureAndTitle(2)

                } else if (currentSongIndex % 3 == 2) {

                    togglePictureAndTitle(0)
                }
                currentSongIndex++
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = MediaPlayer.create(this, currentSong[currentSongIndex % 3])
                initialiseSeekBar()
                mp?.start()
            }

        }

        seekBarMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
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

        navView.setNavigationItemSelectedListener {

            val toast = Toast.makeText(this, "You're currently in music", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)

            when (it.itemId) {
                R.id.miHome -> Intent(this, Home::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                }

                R.id.miCapture -> Intent(this, Capture::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                }

                R.id.miChat -> Intent(this, Chat::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                }

                R.id.miGames -> Intent(this, Games::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                }

                R.id.miMusic -> toast.show()

                R.id.miSetting -> Intent(this, Settings::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
                }

                R.id.miLogout -> Intent(this, Login::class.java).also {
                    startActivity(it)
                    finish()
                    mp?.stop()
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

            }
            true
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
                ivImage.setImageURI(it)
                uploadImageToStorage("profilePicture") // to upload profile picture to Firebase Cloud Storage
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

    private fun downloadImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            withContext(Dispatchers.Main) {
                ivImage.setImageBitmap(bmp)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Music, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initialiseSeekBar() {

        seekBarMusic.max = mp!!.duration

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    seekBarMusic.progress = mp!!.currentPosition
                    handler.postDelayed(this, 1000)
                } catch (e: java.lang.Exception) {
                    seekBarMusic.progress = 0
                }
            }
        }, 0)
    }

    private fun View.toggleVisibility() {
        if (visibility == View.VISIBLE) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
        }
    }

    private fun togglePlayAndPauseButton() {
        ivPlay.toggleVisibility()
        ivPause.toggleVisibility()
    }

    private fun selectSong(num: Int) {
        val toast2 = Toast.makeText(this, "Song is currently selected", Toast.LENGTH_SHORT)
        toast2.setGravity(Gravity.TOP, 0, 0)

        if (num == 0) {
            if (currentSongIndex % 3 == 0 && mp != null) {
                toast2.show()
            } else if (mp == null) {
                if (mp?.isPlaying == true) {
                    togglePlayAndPauseButton()
                }
                currentSongIndex = 0
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
                mp?.start()
            } else {
                if (mp?.isPlaying == false) {
                    togglePlayAndPauseButton()
                }
                currentPicture.toggleVisibility()
                currentTitle.toggleVisibility()
                currentSongIndex = 0
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
                mp?.start()

                togglePictureAndTitle(0)
            }
        } else if (num == 1) {
            if (currentSongIndex % 3 == 1 && mp != null) {
                toast2.show()
            } else if (mp == null) {
                if (mp?.isPlaying == false) {
                    togglePlayAndPauseButton()
                }
                currentPicture.toggleVisibility()
                currentTitle.toggleVisibility()
                currentSongIndex = 1
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
                mp?.start()

                togglePictureAndTitle(1)

            } else {
                if (mp?.isPlaying == false) {
                    togglePlayAndPauseButton()
                }
                currentPicture.toggleVisibility()
                currentTitle.toggleVisibility()
                currentSongIndex = 1
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
                mp?.start()

                togglePictureAndTitle(1)

            }
        } else if (num == 2) {
            if (currentSongIndex % 3 == 2 && mp != null) {
                toast2.show()
            } else if (mp == null) {
                if (mp?.isPlaying == false) {
                    togglePlayAndPauseButton()
                }
                currentPicture.toggleVisibility()
                currentTitle.toggleVisibility()
                currentSongIndex = 2
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
                mp?.start()

                togglePictureAndTitle(2)

            } else {
                if (mp?.isPlaying == false) {
                    togglePlayAndPauseButton()
                }
                currentPicture.toggleVisibility()
                currentTitle.toggleVisibility()
                currentSongIndex = 2
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
                mp?.start()

                togglePictureAndTitle(2)

            }
        }
    }

    private fun togglePictureAndTitle(num: Int){
        picture[num].toggleVisibility()
        title[num].toggleVisibility()
        currentPicture = picture[num]
        currentTitle = title[num]
    }
}






