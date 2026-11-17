package br.com.pfm.mobile.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiEndpointsTest {
    @Test
    fun debugBaseUrlTargetsEmulatorLoopback() {
        assertEquals("http://10.0.2.2:18080/", ApiEndpoints.DEBUG_BASE_URL)
    }
}
