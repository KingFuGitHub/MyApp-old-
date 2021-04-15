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
import kotlinx.android.synthetic.main.capture.*
import kotlinx.android.synthetic.main.games.*
import kotlinx.android.synthetic.main.games.navView
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.music.*
import kotlinx.android.synthetic.main.nav_left_header.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*


class Games : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    var curFile: Uri? = null
    val imageRef = Firebase.storage.reference
    var slashAttack = 0
    var currentMonster = 0
    var RANDOM: Int = 0
    var GOLD: Int = 0
    var obtainSkillTwo = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.games)

        val mediaPlayer = MediaPlayer.create(this, R.raw.demonslayer_op)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        toggle = ActionBarDrawerToggle(this, drawerGames, R.string.open, R.string.close)

        drawerGames.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        downloadImage("profilePicture")


        btnTapAttack.setOnClickListener {
            attack(slashAttack)
            slashAttack++
        }

        seekBarHeathBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                healthBar.progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })


        val uriZenitsu: Uri = Uri.parse("android.resource://$packageName/${R.raw.zenitsu}")
        vvZenitsu.setVideoURI(uriZenitsu)

        ivSkillOne.setOnClickListener {
            cvSkillOne.toggleVisibility()
            cvSkillOneBorder.toggleVisibility()
            vvZenitsu.toggleVisibility()
            vvZenitsu.start()
            YoYo.with(Techniques.FadeOut).delay(11000).playOn(vvZenitsu)
            Handler(Looper.getMainLooper()).postDelayed({
                for (ATTACK in slashAttack..slashAttack + 20) {
                    skillOne(ATTACK)
                }
                cvSkillOne.toggleVisibility()
                cvSkillOneBorder.toggleVisibility()
                vvZenitsu.toggleVisibility()
                YoYo.with(Techniques.FadeIn).playOn(cvSkillOneBorder)
                YoYo.with(Techniques.FadeIn).playOn(cvSkillOne)
            }, 11500)
        }

        val uriSukuna: Uri = Uri.parse("android.resource://$packageName/${R.raw.sukuna_domain_expansion_video}")
        vvSukuna.setVideoURI(uriSukuna)

        ivSkillTwo.setOnClickListener {
            ivSkillTwo.setOnClickListener {
                cvSkillTwo.toggleVisibility()
                cvSkillTwoBorder.toggleVisibility()
                vvSukuna.toggleVisibility()
                vvSukuna.start()
                YoYo.with(Techniques.FadeOut).delay(11200).playOn(vvSukuna)
                Handler(Looper.getMainLooper()).postDelayed({
                    for (ATTACK in slashAttack..slashAttack + 20) {
                        skillOne(ATTACK)
                    }
                    cvSkillTwo.toggleVisibility()
                    cvSkillTwoBorder.toggleVisibility()
                    vvSukuna.toggleVisibility()
                    YoYo.with(Techniques.FadeIn).playOn(cvSkillTwoBorder)
                    YoYo.with(Techniques.FadeIn).playOn(cvSkillTwo)
                }, 11300)
            }

        }

        navView.setNavigationItemSelectedListener { it ->

            val toast = Toast.makeText(this, "You're currently in Games", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)

            when (it.itemId) {
                R.id.miHome -> Intent(this, Home::class.java).also {
                    startActivity(it)
                    finish()
                    mediaPlayer.stop()
                }

                R.id.miCapture -> Intent(this, Capture::class.java).also {
                    startActivity(it)
                    finish()
                    mediaPlayer.stop()
                }

                R.id.miChat -> Intent(this, Chat::class.java).also {
                    startActivity(it)
                    finish()
                    mediaPlayer.stop()
                }

                R.id.miGames -> toast.show()

                R.id.miMusic -> Intent(this, Music::class.java).also {
                    startActivity(it)
                    finish()
                    mediaPlayer.stop()
                }

                R.id.miSetting -> Intent(this, Settings::class.java).also {
                    startActivity(it)
                    finish()
                    mediaPlayer.stop()
                }

                R.id.miLogout -> Intent(this, Login::class.java).also {
                    startActivity(it)
                    finish()
                    mediaPlayer.stop()
                }
            }
            true
        }

        navViewGames.setNavigationItemSelectedListener {
            val toast1 = Toast.makeText(this, "Skill 2 is already acquired", Toast.LENGTH_SHORT)
            toast1.setGravity(Gravity.TOP, 0, 0)
            val toast2 = Toast.makeText(this, "Not enough gold", Toast.LENGTH_SHORT)
            toast2.setGravity(Gravity.TOP, 0, 0)

            when (it.itemId) {
                R.id.miSkillTwo -> {
                    if (GOLD >= 2500 && obtainSkillTwo == false) {
                        cvSkillTwoBorder.toggleVisibility()
                        cvSkillTwo.toggleVisibility()
                        ivSkillTwo.toggleVisibility()
                        tvCoins.text = ""+(GOLD - 2000)
                        YoYo.with(Techniques.Shake).playOn(tvCoins)
                        obtainSkillTwo = true
                    } else if(GOLD < 2500 && obtainSkillTwo == false){
                        toast2.show()
                    }else{
                        toast1.show()
                    }
                }
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
                    Toast.makeText(this@Games, "Successfully uploaded image", Toast.LENGTH_LONG)
                        .show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Games, e.message, Toast.LENGTH_LONG).show()
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
                Toast.makeText(this@Games, e.message, Toast.LENGTH_LONG).show()
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

    private fun attack(ATTACK: Int) {
        if (ATTACK % 6 == 0) {
            RANDOM = Random().nextInt(97000000)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            slashToggle(ATTACK)
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash1)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash1)
            YoYo.with(Techniques.TakingOff).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.Wobble).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 1) {
            RANDOM = Random().nextInt(97000000)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            slashToggle(ATTACK)
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash2)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash2)
            YoYo.with(Techniques.Landing).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.Swing).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 2) {
            RANDOM = Random().nextInt(97000000)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            slashToggle(ATTACK)
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash3)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash3)
            YoYo.with(Techniques.Tada).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.Wave).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 3) {
            RANDOM = Random().nextInt(97000000)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            slashToggle(ATTACK)
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash4)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash4)
            YoYo.with(Techniques.Shake).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.ZoomOutUp).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 4) {
            RANDOM = Random().nextInt(97000000)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            slashToggle(ATTACK)
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash5)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash5)
            YoYo.with(Techniques.Tada).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.Wave).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 5) {
            RANDOM = Random().nextInt(97000000)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            slashToggle(ATTACK)
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash6)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash6)
            YoYo.with(Techniques.Shake).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.ZoomOutUp).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }
    }

    private fun slashToggle(ATTACK: Int) {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (ATTACK % 6 == 0) {
                    ivSlash1.toggleVisibility()
                } else if (ATTACK % 6 == 1) {
                    ivSlash2.toggleVisibility()
                } else if (ATTACK % 6 == 2) {
                    ivSlash3.toggleVisibility()
                } else if (ATTACK % 6 == 3) {
                    ivSlash4.toggleVisibility()
                } else if (ATTACK % 6 == 4) {
                    ivSlash5.toggleVisibility()
                } else {
                    ivSlash6.toggleVisibility()
                }
            }, 500
        )
    }

    private fun skillOne(ATTACK: Int) {
        if (ATTACK % 6 == 0) {
            RANDOM = Random().nextInt(2147483647)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            ivSlash1.toggleVisibility()
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash1)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash1)
            YoYo.with(Techniques.TakingOff).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.Wobble).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 1) {
            RANDOM = Random().nextInt(2147483647)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            ivSlash2.toggleVisibility()
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash2)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash2)
            YoYo.with(Techniques.Landing).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.Swing).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 2) {
            RANDOM = Random().nextInt(2147483647)
//            val rnd = Random().nextInt(1000000000)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            ivSlash3.toggleVisibility()
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash3)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash3)
            YoYo.with(Techniques.Tada).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.Wave).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 3) {
            RANDOM = Random().nextInt(2147483647)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            ivSlash4.toggleVisibility()
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash4)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash4)
            YoYo.with(Techniques.Shake).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.ZoomOutUp).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 4) {
            RANDOM = Random().nextInt(2147483647)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            ivSlash5.toggleVisibility()
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash5)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash5)
            YoYo.with(Techniques.Tada).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.Wave).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }

        if (ATTACK % 6 == 5) {
            RANDOM = Random().nextInt(2147483647)
            YoYo.with(Techniques.Shake).delay(50).playOn(cvMonsters)
            tvDamageText.text = "$RANDOM crit"
            ivSlash6.toggleVisibility()
            YoYo.with(Techniques.FadeIn).delay(50).playOn(ivSlash6)
            YoYo.with(Techniques.FadeOut).delay(100).playOn(ivSlash6)
            YoYo.with(Techniques.Shake).duration(1000).playOn(tvDamageText)
            YoYo.with(Techniques.ZoomOutUp).delay(200).duration(2000).playOn(tvDamageText)
            YoYo.with(Techniques.SlideOutUp).delay(250).duration(1000).playOn(tvDamageText)
            MediaPlayer.create(this, R.raw.slash).start()
            MediaPlayer.create(this, R.raw.slash).reset()
            MediaPlayer.create(this, R.raw.slash).release()
            healthBar.incrementProgressBy(-RANDOM)
            slashToggle(ATTACK)
            killCheck()
        }
    }

    private fun killCheck() {
        if (healthBar.progress == 0) {
            if (currentMonster % 3 == 0) {
                ivHandDemon.toggleVisibility()
                ivDeepSeaKing.toggleVisibility()

                tvHandDemon.toggleVisibility()
                tvDeepSeaKing.toggleVisibility()
            } else if (currentMonster % 3 == 1) {
                ivDeepSeaKing.toggleVisibility()
                ivRhino.toggleVisibility()

                tvDeepSeaKing.toggleVisibility()
                tvRhino.toggleVisibility()
            } else {
                ivRhino.toggleVisibility()
                ivHandDemon.toggleVisibility()

                tvRhino.toggleVisibility()
                tvHandDemon.toggleVisibility()
            }
            healthBar.progress = 1000000000
            currentMonster++
            tvStageNumber.text = "Stage " + (currentMonster + 1)
            RANDOM = Random().nextInt(100)

            var tempGOLD = 0
            tempGOLD = currentMonster * 10 + RANDOM
            GOLD = GOLD + tempGOLD
            tvCoins.text = "" + (GOLD)
            YoYo.with(Techniques.Landing).delay(50).playOn(ivCoins)
        }
    }
}
