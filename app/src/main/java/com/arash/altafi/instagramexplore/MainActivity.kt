package com.arash.altafi.instagramexplore

import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import com.arash.altafi.instagramexplore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var volumeListener: ((Int) -> Unit)? = null
    private lateinit var audioManager: AudioManager
    private var currentVolume = 0
    private var maxVolume = 0
    private var progressSound = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            progressSound = currentVolume * 100 / maxVolume

            if (progressSound < 100)
                progressSound += 10
            volumeListener?.invoke(progressSound)

            currentVolume += 1
            if (currentVolume > maxVolume) currentVolume = maxVolume

            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume,
                0
            )

            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            progressSound = currentVolume * 100 / maxVolume

            if (progressSound > 0)
                progressSound -= 10
            volumeListener?.invoke(progressSound)

            currentVolume -= 1
            if (currentVolume < 0) currentVolume = 0

            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume,
                0
            )

            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}