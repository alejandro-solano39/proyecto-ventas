package com.roomie.salesapp.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(): File {
        val file = File(context.cacheDir, "visit_audio_${System.currentTimeMillis()}.mp4")
        outputFile = file

        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder = rec
        try {
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setAudioSamplingRate(16000)
            rec.setAudioEncodingBitRate(128000)
            rec.setOutputFile(file.absolutePath)
            rec.prepare()
            rec.start()
        } catch (e: Exception) {
            // Liberar recursos y propagar el error para que la UI lo maneje sin crash
            try { rec.release() } catch (_: Exception) {}
            recorder = null
            outputFile = null
            throw e
        }

        return file
    }

    /**
     * Detiene y finaliza el archivo. Si stop() falla (grabación demasiado corta / sin frames),
     * el .mp4 queda corrupto: devolvemos null para que la UI avise en vez de subir basura.
     */
    fun stop(): File? {
        val rec = recorder ?: return null
        return try {
            rec.stop()
            rec.release()
            recorder = null
            val file = outputFile
            // Un .mp4 válido con audio pesa varios KB; si es minúsculo, la grabación no sirvió.
            if (file != null && file.exists() && file.length() >= 2000) file else {
                file?.delete()
                null
            }
        } catch (e: Exception) {
            // stop() lanza si la grabación fue demasiado corta: el archivo es inservible
            try { rec.release() } catch (_: Exception) {}
            recorder = null
            outputFile?.delete()
            null
        }
    }

    fun cancel() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}
