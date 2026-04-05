package com.blockick.app.domain.engine

import com.blockick.app.data.preferences.AppPreferences
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import android.util.Log

@Singleton
class UpstreamResolver @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val appPreferences: AppPreferences
) {
    private val dnsMediaType = "application/dns-message".toMediaType()

    private fun getDnsUrl(dns: String): String {
        return when (dns) {
            "1.1.1.1" -> "https://1.1.1.1/dns-query"
            "8.8.8.8" -> "https://8.8.8.8/dns-query"
            "9.9.9.9" -> "https://9.9.9.9/dns-query"
            else -> if (dns.startsWith("http")) dns else "https://1.1.1.1/dns-query"
        }
    }

    /**
     * Forwards a DNS query (raw bytes) to a DoH provider and returns the response.
     */
    suspend fun resolve(queryBytes: ByteArray): ByteArray? = withContext(Dispatchers.IO) {
        val dns = appPreferences.upstreamDns.first()
        val url = getDnsUrl(dns)
        Log.d("UpstreamResolver", "Resolving via $url (payload: ${queryBytes.size} bytes)")
        
        val request = Request.Builder()
            .url(url)
            .post(queryBytes.toRequestBody(dnsMediaType))
            .addHeader("Accept", "application/dns-message")
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    Log.d("UpstreamResolver", "Resolution successful: ${bytes?.size ?: 0} bytes")
                    bytes
                } else {
                    Log.e("UpstreamResolver", "Resolution failed: HTTP ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("UpstreamResolver", "Resolution error: ${e.message}", e)
            null
        }
    }
}

