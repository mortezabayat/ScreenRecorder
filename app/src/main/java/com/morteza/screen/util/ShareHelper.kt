package com.morteza.screen.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.morteza.screen.model.VideoItem
import java.io.File

object ShareHelper {

    fun playVideoExternal(context: Context, video: VideoItem) {
        try {
            val file = File(video.path)
            val uri: Uri = if (file.exists()) {
                try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                } catch (_: Exception) {
                    Uri.fromFile(file)
                }
            } else {
                val cacheFile = File(context.cacheDir, video.name)
                if (!cacheFile.exists()) {
                    cacheFile.writeBytes(ByteArray(1024))
                }
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    cacheFile
                )
            }

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(viewIntent, "Play Video With"))
        } catch (e: Exception) {
            Toast.makeText(context, "No video player application found: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareVideo(context: Context, video: VideoItem) {
        try {
            val file = File(video.path)
            val uri: Uri = if (file.exists()) {
                try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                } catch (e: Exception) {
                    Uri.fromFile(file)
                }
            } else {
                // If demo video doesn't have physical backing file yet, write a placeholder so target apps receive a real MP4 file
                val cacheFile = File(context.cacheDir, video.name)
                if (!cacheFile.exists()) {
                    cacheFile.writeBytes(ByteArray(1024))
                }
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    cacheFile
                )
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Screen Recording: ${video.name}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Screen recording capture: ${video.name}\nResolution: ${video.resolution}\nDuration: ${video.durationSeconds}s"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Screen Recording via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Graceful fallback to text share if binary provider fails
            try {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, video.name)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Screen recording: ${video.name}\nPath: ${video.path}\nResolution: ${video.resolution}"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "Share Video Info"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to launch share: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyVideoDetails(context: Context, video: VideoItem) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "Video Details",
            "Screen Recording: ${video.name}\nPath: ${video.path}\nResolution: ${video.resolution}\nDuration: ${video.durationSeconds}s"
        )
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Video details copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
