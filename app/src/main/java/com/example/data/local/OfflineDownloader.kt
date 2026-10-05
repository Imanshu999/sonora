package com.example.data.local

import android.content.Context
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Progress(val trackId: String, val progressFraction: Float) : DownloadStatus()
    data class Completed(val trackId: String, val fileUri: String) : DownloadStatus()
    data class Failed(val trackId: String, val errorMessage: String) : DownloadStatus()
}

class OfflineDownloader(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val dao: SonoraDao
) {
    private val _downloadStatus = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStatus: StateFlow<Map<String, DownloadStatus>> = _downloadStatus.asStateFlow()

    private val downloadsDir = File(context.filesDir, "sonora_downloads").apply {
        if (!exists()) mkdirs()
    }

    suspend fun downloadTrack(track: Track): Result<String> = withContext(Dispatchers.IO) {
        if (!track.isDownloadable) {
            return@withContext Result.failure(IllegalStateException("Track license does not permit offline downloading"))
        }

        updateStatus(track.id, DownloadStatus.Progress(track.id, 0.05f))

        try {
            val fileName = "sonora_${track.id.replace("[^a-zA-Z0-9_-]".toRegex(), "_")}.mp3"
            val targetFile = File(downloadsDir, fileName)

            val request = Request.Builder()
                .url(track.audioUrl)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    updateStatus(track.id, DownloadStatus.Failed(track.id, "HTTP error: ${response.code}"))
                    return@withContext Result.failure(Exception("HTTP error ${response.code}"))
                }

                val body = response.body ?: throw Exception("Empty response body")
                val totalBytes = body.contentLength()
                var downloadedBytes = 0L

                body.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                val fraction = (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                                updateStatus(track.id, DownloadStatus.Progress(track.id, fraction))
                            }
                        }
                        output.flush()
                    }
                }
            }

            val fileUri = targetFile.absolutePath
            val now = System.currentTimeMillis()

            // Save in database
            val existing = dao.getTrackById(track.id)
            if (existing != null) {
                dao.updateDownloadStatus(track.id, fileUri, now)
            } else {
                dao.upsertTrack(TrackEntity.fromTrack(track.copy(localUri = fileUri, downloadedAt = now)))
            }

            updateStatus(track.id, DownloadStatus.Completed(track.id, fileUri))
            Result.success(fileUri)
        } catch (e: Exception) {
            updateStatus(track.id, DownloadStatus.Failed(track.id, e.message ?: "Download failed"))
            Result.failure(e)
        }
    }

    suspend fun removeDownload(track: Track): Boolean = withContext(Dispatchers.IO) {
        try {
            track.localUri?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            dao.updateDownloadStatus(track.id, null, null)
            _downloadStatus.value = _downloadStatus.value.toMutableMap().apply { remove(track.id) }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getTotalCacheSizeBytes(): Long {
        return downloadsDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    suspend fun clearAllDownloads(): Boolean = withContext(Dispatchers.IO) {
        try {
            downloadsDir.listFiles()?.forEach { it.delete() }
            dao.clearAllDownloadStatuses()
            _downloadStatus.value = emptyMap()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun updateStatus(trackId: String, status: DownloadStatus) {
        val updated = _downloadStatus.value.toMutableMap()
        updated[trackId] = status
        _downloadStatus.value = updated
    }
}
