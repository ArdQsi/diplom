import com.newdex.lti.security.OAuth1Signer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class OAuth1SignerTest {
    private val consumerKey = "test_consumer_key"
    private val consumerSecret = "test_consumer_secret"
    private val signer = OAuth1Signer(consumerKey, consumerSecret)

    @Test
    fun `sign should include required OAuth parameters in Authorization header`() {
        val url = "https://api.example.com/endpoint"
        val result = signer.sign(url)

        val authHeader = result["Authorization"] ?: fail("Authorization header is missing")

        assertTrue(authHeader.startsWith("OAuth "))
        assertTrue(authHeader.contains("oauth_consumer_key=\"test_consumer_key\""))
        assertTrue(authHeader.contains("oauth_nonce="))
        assertTrue(authHeader.contains("oauth_signature="))
        assertTrue(authHeader.contains("oauth_signature_method=\"HMAC-SHA1\""))
        assertTrue(authHeader.contains("oauth_timestamp="))
        assertTrue(authHeader.contains("oauth_version=\"1.0\""))
    }

    @Test
    fun `sign should include query parameters from URL in signature`() {
        val url = "https://api.example.com/endpoint?param1=value1&param2=value2"
        val result = signer.sign(url)

        val authHeader = result["Authorization"] ?: fail("Authorization header is missing")
        assertTrue(authHeader.contains("oauth_signature="))
    }

    @Test
    fun `sign should include body parameters in signature`() {
        val url = "https://api.example.com/endpoint"
        val body = mapOf("body_param1" to "body_value1", "body_param2" to "body_value2")
        val result = signer.sign(url, body = body)

        val authHeader = result["Authorization"] ?: fail("Authorization header is missing")
        assertTrue(authHeader.contains("oauth_signature="))
    }

    @Test
    fun `sign should preserve existing headers`() {
        val url = "https://api.example.com/endpoint"
        val headers = mapOf("X-Custom-Header" to "custom_value")
        val result = signer.sign(url, headers = headers)

        assertEquals("custom_value", result["X-Custom-Header"])
    }

    @Test
    fun `sign should work with different HTTP methods`() {
        val url = "https://api.example.com/endpoint"
        val resultGet = signer.sign(url, "GET")
        val resultPost = signer.sign(url, "POST")

        assertTrue(resultGet["Authorization"]?.contains("oauth_signature=") == true)
        assertTrue(resultPost["Authorization"]?.contains("oauth_signature=") == true)
    }

    @Test
    fun `sign should generate different nonce for each call`() {
        val url = "https://api.example.com/endpoint"
        val result1 = signer.sign(url)
        val result2 = OAuth1Signer(consumerKey, consumerSecret).sign(url)

        val nonce1 = extractParam(result1["Authorization"]!!, "oauth_nonce")
        val nonce2 = extractParam(result2["Authorization"]!!, "oauth_nonce")

        assertTrue(nonce1 != nonce2)
    }

    @Test
    fun `sign should include current timestamp`() {
        val url = "https://api.example.com/endpoint"
        val before = System.currentTimeMillis() / 1000L
        val result = signer.sign(url)
        val after = System.currentTimeMillis() / 1000L

        val timestampStr = extractParam(result["Authorization"]!!, "oauth_timestamp")
        val timestamp = timestampStr.toLong()

        assertTrue(timestamp in before..after)
    }

    private fun extractParam(authHeader: String, paramName: String): String {
        val pattern = "$paramName=\"([^\"]+)\"".toRegex()
        return pattern.find(authHeader)?.groupValues?.get(1) ?: fail("$paramName not found in header")
    }
}