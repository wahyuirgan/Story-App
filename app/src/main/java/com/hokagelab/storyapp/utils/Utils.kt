package com.hokagelab.storyapp.utils

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.data.LoginRepository
import com.hokagelab.storyapp.data.PreferenceRepository
import com.hokagelab.storyapp.data.RegisterRepository
import com.hokagelab.storyapp.data.StoryRepository
import com.hokagelab.storyapp.data.source.local.UserPreference
import com.hokagelab.storyapp.data.source.local.room.StoryDatabase
import com.hokagelab.storyapp.data.source.remote.network.ApiConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
object Utils {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preference")

    val timeStamp: String = SimpleDateFormat(
        "dd-MMM-yyyy",
        Locale.US
    ).format(System.currentTimeMillis())

    fun checkEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun providerRegisterRepository(): RegisterRepository {
        val apiService = ApiConfig.getApiService()
        return RegisterRepository.getInstance(apiService)
    }
    fun providerLoginRepository(): LoginRepository {
        val apiService = ApiConfig.getApiService()
        return LoginRepository.getInstance(apiService)
    }
    fun providerPreferenceRepository(context: Context): PreferenceRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        return PreferenceRepository.getInstance(userPreference)
    }
    fun providerStoryRepository(context: Context): StoryRepository {
        val apiService = ApiConfig.getApiService()
        val storyDatabase = StoryDatabase.getDatabase(context)
        return StoryRepository.getInstance(apiService, storyDatabase)
    }

    private const val LIGHT_MODE = "light"
    private const val DARK_MODE = "dark"
    const val DEFAULT_MODE = "default"

    fun applyTheme(theme: String) {
        val mode = when (theme) {
            LIGHT_MODE -> AppCompatDelegate.MODE_NIGHT_NO
            DARK_MODE -> AppCompatDelegate.MODE_NIGHT_YES
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            }
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    fun createFile(application: Application): File {
        val mediaDirectory = application.externalMediaDirs.firstOrNull()?.let {
            File(it, "camerapicture").apply { mkdirs() }
        }
        val outputDirectory = if (
            mediaDirectory != null && mediaDirectory.exists()
        ) mediaDirectory else application.filesDir

        return File(outputDirectory, "$timeStamp.jpg")
    }

    fun withDateFormat(dateTime: String): String {
        val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                Locale.US)
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'",
                Locale.US)
        }
        val parsedDate = dateFormat.parse(dateTime) as Date
        val dateTimeData: ZonedDateTime? = ZonedDateTime.parse(changeToRightZoneId(dateTime))
        val timeReformatted: DateTimeFormatter? =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale(Locale.getDefault().language, Locale.getDefault().country))
        return DateFormat.getDateInstance(DateFormat.LONG).format(parsedDate)  + " \u2022 " + dateTimeData?.format(timeReformatted) + " [" + TimeZone.getDefault().toZoneId() + "]"
    }

    private fun changeToRightZoneId(dateTime: String): String {
        val datetimeWithoutZone: LocalDateTime = LocalDateTime.parse(dateTime,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        val utcZdt: ZonedDateTime = datetimeWithoutZone.atZone(ZoneId.of("UTC"))
        return utcZdt.withZoneSameInstant(ZoneId.of(TimeZone.getDefault().toZoneId().toString())).toString()
    }

    private fun getHour(): String {
        val simpleDateFormat = SimpleDateFormat("HH", Locale.getDefault())
        return simpleDateFormat.format(Date())
    }

    fun setGreeting(): Int {
        return when (getHour().toInt()) {
            in 11..14 -> (
                R.string.text_greeting_afternoon
            )
            in 15..18 -> (
                R.string.text_greeting_evening
            )
            in 19..23 -> (
                R.string.text_greeting_night
            )
            else -> (
                R.string.text_greeting_morning
            )
        }
    }
}