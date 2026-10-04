package com.example

import android.app.Application
import com.example.data.local.DataStoreManager
import com.example.data.local.OfflineDownloader
import com.example.data.local.SonoraDatabase
import com.example.data.remote.AudiusApi
import com.example.data.remote.JamendoApi
import com.example.data.remote.LrclibApi
import com.example.data.repository.MusicRepository
import com.example.playback.SonoraPlayer
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class SonoraApplication : Application() {

    lateinit var database: SonoraDatabase
        private set

    lateinit var dataStoreManager: DataStoreManager
        private set

    lateinit var repository: MusicRepository
        private set

    lateinit var player: SonoraPlayer
        private set

    lateinit var downloader: OfflineDownloader
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        instance = this

        val cacheDir = File(cacheDir, "http_cache")
        val cache = Cache(cacheDir, 50L * 1024L * 1024L) // 50 MB cache

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val jamendoRetrofit = Retrofit.Builder()
            .baseUrl("https://api.jamendo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val audiusRetrofit = Retrofit.Builder()
            .baseUrl("https://discoveryprovider.audius.co/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val lrclibRetrofit = Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val jamendoApi = jamendoRetrofit.create(JamendoApi::class.java)
        val audiusApi = audiusRetrofit.create(AudiusApi::class.java)
        val lrclibApi = lrclibRetrofit.create(LrclibApi::class.java)

        database = SonoraDatabase.getInstance(this)
        dataStoreManager = DataStoreManager(this)

        downloader = OfflineDownloader(this, okHttpClient, database.sonoraDao())
        repository = MusicRepository(jamendoApi, audiusApi, lrclibApi, database.sonoraDao(), downloader)

        player = SonoraPlayer(this) { finishedTrack ->
            applicationScope.launch {
                repository.recordPlay(finishedTrack)
            }
        }

        // Apply saved audio preferences to player
        applicationScope.launch {
            val crossfade = dataStoreManager.crossfadeSecondsFlow.first()
            player.setCrossfade(crossfade)

            val preset = dataStoreManager.equalizerPresetFlow.first()
            player.setEqualizerPreset(preset)

            val loudness = dataStoreManager.loudnessNormalizationFlow.first()
            player.setLoudnessNormalization(loudness)
        }
    }

    override fun onTerminate() {
        player.release()
        super.onTerminate()
    }

    companion object {
        lateinit var instance: SonoraApplication
            private set
    }
}
