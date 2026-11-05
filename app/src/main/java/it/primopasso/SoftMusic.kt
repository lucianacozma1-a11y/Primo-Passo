package it.primopasso

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Musica dolce generata dall'app: accordi lenti e morbidi con qualche nota di campanella lontana.
 * Nessun file audio, nessun suono forte. Il brano dura 32 secondi e si ripete senza stacchi.
 */
object SoftMusic {
    private const val RATE = 22050
    private const val SECONDS = 32
    private var track: AudioTrack? = null
    private var pcm: ShortArray? = null
    @Volatile private var wanted = false

    private fun generate(): ShortArray {
        val n = RATE * SECONDS
        val buf = FloatArray(n)
        // Quattro accordi tenuti, 8 secondi ciascuno, che sfumano l'uno nell'altro.
        val chords = listOf(
            floatArrayOf(130.81f, 196.00f, 261.63f, 329.63f),  // Do
            floatArrayOf(110.00f, 164.81f, 220.00f, 261.63f),  // La minore
            floatArrayOf(87.31f, 174.61f, 220.00f, 261.63f),   // Fa
            floatArrayOf(98.00f, 146.83f, 196.00f, 246.94f)    // Sol
        )
        val seg = n / chords.size
        val twoPi = (2 * PI).toFloat()
        for (c in chords.indices) {
            val center = c * seg + seg / 2
            for (k in -seg..seg) {
                // finestra morbida lunga due segmenti: accordi sovrapposti, nessun attacco netto
                val w = 0.5f * (1f + kotlin.math.cos(PI.toFloat() * k / seg))
                val idx = ((center + k) % n + n) % n
                val t = (center + k).toFloat() / RATE
                var v = 0f
                chords[c].forEachIndexed { i, f ->
                    val amp = if (i == 0) 0.32f else 0.22f
                    v += amp * sin(twoPi * f * t) + amp * 0.35f * sin(twoPi * (f * 1.003f) * t)
                }
                buf[idx] += v * w
            }
        }
        // Campanelle lontane su una scala pentatonica, rare e lievi.
        val bells = floatArrayOf(523.25f, 587.33f, 659.25f, 783.99f, 880.00f)
        val rnd = Random(7)
        var pos = RATE * 2
        while (pos < n) {
            val f = bells[rnd.nextInt(bells.size)]
            val len = RATE * 4
            for (k in 0 until len) {
                val t = k.toFloat() / RATE
                val env = exp(-1.4f * t) * (1f - exp(-60f * t))
                buf[(pos + k) % n] += 0.10f * env * sin(twoPi * f * t)
            }
            pos += RATE * (3 + rnd.nextInt(4))
        }
        var max = 0f
        for (x in buf) if (kotlin.math.abs(x) > max) max = kotlin.math.abs(x)
        val gain = if (max > 0f) 0.22f / max else 0f
        return ShortArray(n) { (buf[it] * gain * Short.MAX_VALUE).toInt().toShort() }
    }

    private var level = 0.8f

    fun start(volume: Float = 0.8f) {
        level = volume
        wanted = true
        if (track != null) return
        Thread {
            try {
                val data = pcm ?: generate().also { pcm = it }
                if (!wanted || track != null) return@Thread
                val t = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(data.size * 2)
                    .build()
                t.write(data, 0, data.size)
                t.setLoopPoints(0, data.size, -1)
                t.setVolume(0f)
                track = t
                t.play()
                // dissolvenza in entrata, dolce
                for (i in 1..30) {
                    if (!wanted) break
                    t.setVolume(i / 30f * level)
                    Thread.sleep(60)
                }
                if (!wanted) stop()
            } catch (_: Exception) {
                track = null
            }
        }.start()
    }

    fun stop() {
        wanted = false
        val t = track ?: return
        track = null
        try {
            t.pause()
            t.flush()
            t.stop()
        } catch (_: Exception) {
        }
        try {
            t.release()
        } catch (_: Exception) {
        }
    }
}
