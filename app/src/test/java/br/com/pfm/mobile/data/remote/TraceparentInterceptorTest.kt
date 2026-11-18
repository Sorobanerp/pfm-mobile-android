package br.com.pfm.mobile.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TraceparentInterceptorTest {

    private val w3cRegex = Regex("^00-[0-9a-f]{32}-[0-9a-f]{16}-01$")

    @Test
    fun generatesValidW3cTraceparentAndPreservesExistingValidHeader() {
        val generated = TraceparentInterceptor.ensureTraceparent(null)
        assertTrue(w3cRegex.matches(generated))

        val fromInvalid = TraceparentInterceptor.ensureTraceparent("invalid-header")
        assertTrue(w3cRegex.matches(fromInvalid))

        val existing = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
        assertEquals(existing, TraceparentInterceptor.ensureTraceparent(existing))
    }

    @Test
    fun injectsTraceparentAndClientPlatformHeadersOnBffRequests() {
        var capturedRequest: Request? = null
        val terminalInterceptor = Interceptor { chain ->
            capturedRequest = chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .body("""{"reconciled":true}""".toResponseBody("application/json".toMediaType()))
                .build()
        }

        val client = TraceparentInterceptor.buildTracedHttpClient()
            .newBuilder()
            .addInterceptor(terminalInterceptor)
            .build()

        val req = Request.Builder()
            .url(ApiEndpoints.DEBUG_BASE_URL + "api/v1/receipts/nfce")
            .build()
        val resp = client.newCall(req).execute()
        assertEquals(200, resp.code)

        val intercepted = requireNotNull(capturedRequest)
        val tp = requireNotNull(intercepted.header(TraceparentInterceptor.HEADER_TRACEPARENT))
        assertTrue(w3cRegex.matches(tp))
        assertEquals(
            TraceparentInterceptor.CLIENT_PLATFORM_VALUE,
            intercepted.header(TraceparentInterceptor.HEADER_CLIENT_PLATFORM)
        )
    }

    @Test
    fun sanitizesZeroPiiMaskingCpfPanAndTokens() {
        assertEquals("", TraceparentInterceptor.sanitizeZeroPii(""))
        val raw = "cpf=123.456.789-01 card=4532 1122 3344 5566 token=BearerSecret123"
        val sanitized = TraceparentInterceptor.sanitizeZeroPii(raw)
        assertFalse(sanitized.contains("123.456.789-01"))
        assertFalse(sanitized.contains("4532 1122 3344 5566"))
        assertFalse(sanitized.contains("BearerSecret123"))
        assertTrue(sanitized.contains("[CPF_MASKED]"))
        assertTrue(sanitized.contains("[PAN_MASKED]"))
        assertTrue(sanitized.contains("[REDACTED]"))
    }
}
