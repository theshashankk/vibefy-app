package com.vibefy.musicwtf.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibefy.musicwtf.data.model.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    private val _config = MutableStateFlow(AppConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    init {
        fetchRemoteConfig()
    }

    private fun fetchRemoteConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${com.vibefy.musicwtf.BuildConfig.BASE_URL}/config.json")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = json.decodeFromString<AppConfig>(body)
                        _config.value = parsed
                    }
                }
            } catch (e: Exception) {
                // Silently keep default config when offline
            }
        }
    }
}
