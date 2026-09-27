package com.example.colorterritory.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

enum class Sfx { SELECT, EXPAND, PASS, COMPLETE, WIN }

/**
 * Restrained, synthesized sound effects. Short soft sine tones are rendered
 * once to WAV files in the cache directory and played through SoundPool, so
 * the app ships no audio assets.
 */
class SoundEngine(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = HashMap<Sfx, Int>()
    private val loaded = HashSet<Int>()

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) synchronized(loaded) { loaded += sampleId }
        }
        val dir = File(context.cacheDir, "sfx").apply { mkdirs() }
        for (sfx in Sfx.entries) {
            val file = File(dir, "${sfx.name.lowercase()}_v1.wav")
            if (!file.exists()) writeWav(file, render(sfx))
            ids[sfx] = soundPool.load(file.absolutePath, 1)
        }
    }

    fun play(sfx: Sfx) {
        val id = ids[sfx] ?: return
        val ready = synchronized(loaded) { id in loaded }
        if (ready) soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() = soundPool.release()

    private data class Note(val startMs: Int, val durationMs: Int, val freq: Double, val endFreq: Double = freq, val gain: Double)

    private fun render(sfx: Sfx): FloatArray {
        val notes = when (sfx) {
            Sfx.SELECT -> listOf(Note(0, 45, 1320.0, 1250.0, 0.16))
            Sfx.EXPAND -> listOf(Note(0, 150, 523.25, 587.33, 0.20), Note(70, 190, 783.99, gain = 0.16))
            Sfx.PASS -> listOf(Note(0, 260, 392.0, 311.13, 0.18))
            Sfx.COMPLETE -> listOf(
                Note(0, 420, 523.25, gain = 0.14),
                Note(90, 420, 659.25, gain = 0.12),
                Note(180, 520, 783.99, gain = 0.12)
            )
            Sfx.WIN -> listOf(
                Note(0, 300, 523.25, gain = 0.14),
                Note(120, 300, 659.25, gain = 0.13),
                Note(240, 300, 783.99, gain = 0.13),
                Note(360, 700, 1046.5, gain = 0.12)
            )
        }
        val totalMs = notes.maxOf { it.startMs + it.durationMs } + 20
        val out = FloatArray(SAMPLE_RATE * totalMs / 1000)
        for (note in notes) {
            val start = SAMPLE_RATE * note.startMs / 1000
            val length = SAMPLE_RATE * note.durationMs / 1000
            var phase = 0.0
            for (i in 0 until length) {
                val t = i.toDouble() / length
                val freq = note.freq + (note.endFreq - note.freq) * t
                phase += 2 * PI * freq / SAMPLE_RATE
                val attack = min(1.0, i / (SAMPLE_RATE * 0.006))
                val envelope = attack * exp(-4.5 * t) * (1 - t)
                val sample = (sin(phase) + 0.18 * sin(2 * phase)) * envelope * note.gain
                val index = start + i
                if (index < out.size) out[index] += sample.toFloat()
            }
        }
        return out
    }

    private fun writeWav(file: File, samples: FloatArray) {
        val dataSize = samples.size * 2
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray()).putInt(36 + dataSize).put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1)
            .putInt(SAMPLE_RATE).putInt(SAMPLE_RATE * 2).putShort(2).putShort(16)
        buffer.put("data".toByteArray()).putInt(dataSize)
        for (s in samples) {
            buffer.putShort((s.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort())
        }
        FileOutputStream(file).use { it.write(buffer.array()) }
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
