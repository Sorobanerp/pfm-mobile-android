package br.com.pfm.mobile.data.remote

import java.security.SecureRandom
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * OkHttp [Interceptor] propagating W3C Trace Context (`traceparent: 00-<32-hex>-<16-hex>-01`)
 * and `X-Client-Platform: android-offline-first` to the Go BFF for end-to-end Datadog APM correlation.
 * Strictly zero-PII: never inspects or logs payloads, tokens, or sensitive headers.
 */
class TraceparentInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val traceparent = ensureTraceparent(original.header(HEADER_TRACEPARENT))
        val tracedRequest = original.newBuilder()
            .header(HEADER_TRACEPARENT, traceparent)
            .header(HEADER_CLIENT_PLATFORM, CLIENT_PLATFORM_VALUE)
            .build()
        return chain.proceed(tracedRequest)
    }

    companion object {
        const val HEADER_TRACEPARENT = "traceparent"
        const val HEADER_CLIENT_PLATFORM = "X-Client-Platform"
        const val CLIENT_PLATFORM_VALUE = "android-offline-first"

        private val W3C_TRACEPARENT_REGEX =
            Regex("^00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$")
        private val CPF_REGEX =
            Regex("\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-\\d{2}\\b|\\b\\d{11}\\b")
        private val PAN_REGEX =
            Regex("\\b(?:\\d[ -]*?){13,16}\\b")
        private val TOKEN_REGEX =
            Regex("(?i)(\"?(?:authorization|token|password|pdf_?password)\"?\\s*[:=]\\s*\"?)([^\"\\s,}]+)(\"?)")

        private val secureRandom = SecureRandom()

        fun ensureTraceparent(incoming: String? = null): String {
            val trimmed = incoming?.trim()
            if (trimmed != null && W3C_TRACEPARENT_REGEX.matches(trimmed)) {
                return trimmed
            }
            val traceBytes = ByteArray(16)
            val spanBytes = ByteArray(8)
            secureRandom.nextBytes(traceBytes)
            secureRandom.nextBytes(spanBytes)
            val traceHex = traceBytes.joinToString("") { "%02x".format(it) }
            val spanHex = spanBytes.joinToString("") { "%02x".format(it) }
            return "00-$traceHex-$spanHex-01"
        }

        fun sanitizeZeroPii(raw: String): String {
            if (raw.isEmpty()) return ""
            return raw
                .replace(TOKEN_REGEX, "$1[REDACTED]$3")
                .replace(PAN_REGEX, "[PAN_MASKED]")
                .replace(CPF_REGEX, "[CPF_MASKED]")
        }

        fun buildTracedHttpClient(
            baseBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        ): OkHttpClient = baseBuilder
            .addInterceptor(TraceparentInterceptor())
            .build()
    }
}
