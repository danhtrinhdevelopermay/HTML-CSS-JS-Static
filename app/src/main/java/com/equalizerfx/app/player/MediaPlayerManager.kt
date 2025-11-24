package com.equalizerfx.app.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException

class MediaPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    
    private val _currentFile = MutableStateFlow<String?>(null)
    val currentFile: StateFlow<String?> = _currentFile
    
    val audioSessionId: Int
        get() = mediaPlayer?.audioSessionId ?: 0
    
    init {
        initializePlayer()
    }
    
    private fun initializePlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                _isPlaying.value = false
            }
            setOnPreparedListener {
                _duration.value = duration.toLong()
            }
        }
    }
    
    fun loadFile(uri: Uri) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(context, uri)
            mediaPlayer?.prepare()
            _currentFile.value = uri.toString()
            _duration.value = mediaPlayer?.duration?.toLong() ?: 0L
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    fun play() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
            }
        }
    }
    
    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }
    
    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.prepare()
            }
            _isPlaying.value = false
            _currentPosition.value = 0L
        }
    }
    
    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position
    }
    
    fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: 0L
    }
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
