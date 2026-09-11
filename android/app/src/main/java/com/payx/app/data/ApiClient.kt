package com.payx.app.data

import com.payx.app.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiException(message: String, val statusCode: Int? = null) : Exception(message)

class ApiClient(@PublishedApi internal val sessionToken: () -> String?) {
    private val json = Json { ignoreUnknownKeys = true }

    @PublishedApi
    internal val http = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(this@ApiClient.json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 12_000
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 12_000
        }
    }

    suspend fun baseUrl(): String {
        val override = BuildConfig.API_BASE_URL.trim().ifEmpty { null }
        val candidates = listOfNotNull(
            override,
            "http://10.0.2.2:8787",
            "http://127.0.0.1:8787"
        )
        for (candidate in candidates) {
            try {
                val health = http.get("$candidate/health").body<HealthResponse>()
                if (health.ok) return candidate
            } catch (_: Exception) {
                continue
            }
        }
        throw ApiException("PayX backend unreachable. Start it with ./dev.sh.")
    }

    suspend inline fun <reified T> get(path: String, params: Map<String, String> = emptyMap()): T {
        val base = baseUrl()
        val response = http.get("$base$path") {
            sessionToken()?.let { header("Authorization", "Bearer $it") }
            params.forEach { (k, v) -> parameter(k, v) }
        }
        return response.decodeOrThrow()
    }

    suspend inline fun <reified T, reified B> post(path: String, body: B): T {
        val base = baseUrl()
        val response = http.post("$base$path") {
            contentType(ContentType.Application.Json)
            sessionToken()?.let { header("Authorization", "Bearer $it") }
            setBody(body)
        }
        return response.decodeOrThrow()
    }

    @PublishedApi
    internal suspend inline fun <reified T> HttpResponse.decodeOrThrow(): T {
        if (!status.isSuccess()) {
            val message = runCatching { body<ApiErrorBody>().error }.getOrNull()
                ?: "Request failed with status ${status.value}"
            throw ApiException(message, status.value)
        }
        return body()
    }
}
