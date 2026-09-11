package com.payx.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Base URL discovery mirrors the reference: explicit override first, then
// emulator loopback, then localhost. `adb reverse tcp:8787 tcp:8787` covers
// physical devices (see dev.sh).
class ApiClient(@PublishedApi internal val sessionToken: () -> String?) {
    private val json = Json { ignoreUnknownKeys = true }

    @PublishedApi
    internal val http = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(this@ApiClient.json) }
    }

    suspend fun baseUrl(): String {
        val override = BuildConfigShim.apiBaseUrl()
        val candidates = listOfNotNull(
            override,
            "http://10.0.2.2:8787",
            "http://127.0.0.1:8787"
        )
        for (candidate in candidates) {
            try {
                http.get("$candidate/health").body<Map<String, Boolean>>()
                return candidate
            } catch (_: Exception) {
                continue
            }
        }
        throw IllegalStateException("PayX backend unreachable. Start it with ./dev.sh.")
    }

    suspend inline fun <reified T> get(path: String, params: Map<String, String> = emptyMap()): T {
        val base = baseUrl()
        return http.get("$base$path") {
            sessionToken()?.let { header("Authorization", "Bearer $it") }
            params.forEach { (k, v) -> parameter(k, v) }
        }.body()
    }

    suspend inline fun <reified T> post(path: String, body: Any): T {
        val base = baseUrl()
        return http.post("$base$path") {
            contentType(ContentType.Application.Json)
            sessionToken()?.let { header("Authorization", "Bearer $it") }
            setBody(body)
        }.body()
    }
}

// Placeholder until BuildConfig fields are wired in Phase 3.
internal object BuildConfigShim {
    fun apiBaseUrl(): String? = null
}
