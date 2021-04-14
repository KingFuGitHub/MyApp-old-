package com.bignerdranch.android.myapp

import android.app.Activity
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
import android.widget.SeekBar
import android.widget.Toast
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

    private var currentSong =
        listOf(R.raw.congratulations, R.raw.in_the_end, R.raw.love_me_like_you_do)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.music)

        window.decorView.toggleVisibility()

        ivPlay.setOnClickListener {
            YoYo.with(Techniques.Landing).duration(1000).playOn(ivPlay)
            if (mp == null) {
                mp = MediaPlayer.create(this, currentSong[currentSongIndex])
                initialiseSeekBar()
            }
            mp?.start()
            ivPlay.toggleVisibility()
            ivPause.toggleVisibility()
        }


        ivPause.setOnClickListener {
            YoYo.with(Techniques.Landing).duration(1000).playOn(ivPause)
            if (mp != null) {
                mp?.pause()
            }
            ivPlay.toggleVisibility()
            ivPause.toggleVisibility()
        }

        mp?.setOnCompletionListener(){
               Toast.makeText(this, "song finished", Toast.LENGTH_LONG).show()
        }

        ivPrevious.setOnClickListener {
            YoYo.with(Techniques.Landing).playOn(ivPrevious)
            if (mp == null) {
               Toast.makeText(this@Music, "You can't go backward if you never go forward.", Toast.LENGTH_LONG).show()
            } else if (mp != null) {
                currentSongIndex++
                if (mp?.isPlaying == false) {
                    ivPlay.toggleVisibility()
                    ivPause.toggleVisibility()
                }
                if (currentSongIndex % 3 == 1) {
                    currentSongIndex++
                    ivCongratulation.toggleVisibility()
                    ivLoveMeLikeYouDo.toggleVisibility()
                    tvCongratulation.toggleVisibility()
                    tvLoveMeLikeYouDo.toggleVisibility()
                } else if (currentSongIndex % 3 == 2) {
                    currentSongIndex++
                    ivInTheEnd.toggleVisibility()
                    ivCongratulation.toggleVisibility()
                    tvInTheEnd.toggleVisibility()
                    tvCongratulation.toggleVisibility()
                } else if (currentSongIndex % 3 == 0) {
                    currentSongIndex++
                    ivInTheEnd.toggleVisibility()
                    ivLoveMeLikeYouDo.toggleVisibility()
                    tvInTheEnd.toggleVisibility()
                    tvLoveMeLikeYouDo.toggleVisibility()
                }
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
            if (mp == null) {
                ivPlay.toggleVisibility()
                ivPause.toggleVisibility()
                mp = MediaPlayer.create(this, currentSong[1])
                initialiseSeekBar()
                mp?.start()
                ivCongratulation.toggleVisibility()
                ivInTheEnd.toggleVisibility()
                tvCongratulation.toggleVisibility()
                tvInTheEnd.toggleVisibility()
                currentSongIndex++
            } else if (mp != null) {
                currentSongIndex++
                if (mp?.isPlaying == false) {
                    ivPlay.toggleVisibility()
                    ivPause.toggleVisibility()
                }
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = MediaPlayer.create(this, currentSong[currentSongIndex % 3])
                initialiseSeekBar()
                mp?.start()
                if (currentSongIndex % 3 == 1) {
                    ivCongratulation.toggleVisibility()
                    ivInTheEnd.toggleVisibility()
                    tvCongratulation.toggleVisibility()
                    tvInTheEnd.toggleVisibility()
                } else if (currentSongIndex % 3 == 2) {
                    ivInTheEnd.toggleVisibility()
                    ivLoveMeLikeYouDo.toggleVisibility()
                    tvInTheEnd.toggleVisibility()
                    tvLoveMeLikeYouDo.toggleVisibility()
                } else if (currentSongIndex % 3 == 0) {
                    ivLoveMeLikeYouDo.toggleVisibility()
                    ivCongratulation.toggleVisibility()
                    tvLoveMeLikeYouDo.toggleVisibility()
                    tvCongratulation.toggleVisibility()
                }
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
            val toast = Toast.makeText(this, "You clicked on a song", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)

            when (it.itemId) {
                R.id.miCongratulation -> toast.show()

                R.id.miInTheEnd -> toast.show()

                R.id.miLoveMeLikeYouDo -> toast.show()
            }
            true
        }

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



}




