package br.com.pfm.mobile.data.remote

import org.junit.Assert.assertTrue
import org.junit.Test

class ApiEndpointsTest {
    @Test
    fun debugBaseUrlTargetsEmulatorLoopback() {
        val url = ApiEndpoints.DEBUG_BASE_URL
        assertTrue(url.endsWith(":18080/"))
    }
}
