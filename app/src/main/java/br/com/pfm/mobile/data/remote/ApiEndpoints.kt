package br.com.pfm.mobile.data.remote

/**
 * Base URLs per build type. Debug targets the host machine's Envoy Gateway
 * through the Android emulator loopback alias (10.0.2.2 -> host :18080).
 * Release URL is injected via BuildConfig (12-Factor) and never hardcoded here.
 */
object ApiEndpoints {
    const val DEBUG_BASE_URL = "http://10.0.2.2:18080/"
}
