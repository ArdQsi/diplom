package com.newdex.lti.security

import java.net.URLEncoder
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap

class OAuth1Signer(private val consumerKey: String, private val consumerSecret: String) {
    private val nonce: String = UUID.randomUUID().toString()
    private val timestamp: Long = System.currentTimeMillis() / 1000L

    fun sign(
        url: String,
        httpMethod: String = "POST",
        body: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): Map<String, String> {
        val parameters = hashMapOf(
            OAUTH_CONSUMER_KEY to consumerKey,
            OAUTH_NONCE to nonce,
            OAUTH_SIGNATURE_METHOD to OAUTH_SIGNATURE_METHOD_VALUE,
            OAUTH_TIMESTAMP to timestamp.toString(),
            OAUTH_VERSION to OAUTH_VERSION_VALUE
        )

        parameters.putAll(body)

        val uri = url.toUri()
        uri.query?.split('&')?.forEach { param ->
            param.split('=', limit = 2).takeIf { it.size == 2 }?.let { (key, value) ->
                parameters[key] = value
            }
        }

        val baseUrl = "${uri.scheme}://${uri.host}${uri.path}"
        val signingKey = "${consumerSecret.encodeRfc3986()}&"
        val paramsForSignature = parameters.encodeForSignature()
        val dataToSign = "${httpMethod.uppercase().encodeRfc3986()}&${baseUrl.encodeRfc3986()}&${paramsForSignature.encodeRfc3986()}"
        val signature = sign(signingKey, dataToSign)
        parameters[OAUTH_SIGNATURE] = signature

        return headers.toMutableMap().apply {
            this["Authorization"] = "OAuth ${parameters.toHeaderFormat()}"
        }
    }

    private fun sign(key: String, data: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1").apply {
            init(secretKey)
        }
        val signatureBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    private fun HashMap<String, String>.toHeaderFormat(): String {
        return filterKeys { it in baseKeys }
            .toList()
            .sortedBy { (key, _) -> key }
            .joinToString(", ") { (key, value) ->
                "${key.encodeRfc3986()}=\"${value.encodeRfc3986()}\""
            }
    }

    private fun HashMap<String, String>.encodeForSignature(): String {
        return toList()
            .sortedBy { (key, _) -> key }
            .joinToString("&") { (key, value) ->
                "${key.encodeRfc3986()}=${value.encodeRfc3986()}"
            }
    }

    private fun String.encodeRfc3986(): String {
        return URLEncoder.encode(this, "UTF-8")
            .replace("+", "%20")
            .replace("%7E", "~")
            .replace("*", "%2A")
    }

    private fun String.toUri(): java.net.URI {
        return try {
            java.net.URI(this)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL: $this", e)
        }
    }

    companion object {
        private const val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
        private const val OAUTH_NONCE = "oauth_nonce"
        private const val OAUTH_SIGNATURE = "oauth_signature"
        private const val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
        private const val OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1"
        private const val OAUTH_TIMESTAMP = "oauth_timestamp"
        private const val OAUTH_TOKEN = "oauth_token"
        private const val OAUTH_VERSION = "oauth_version"
        private const val OAUTH_VERSION_VALUE = "1.0"

        private val baseKeys = listOf(
            OAUTH_CONSUMER_KEY,
            OAUTH_NONCE,
            OAUTH_SIGNATURE,
            OAUTH_SIGNATURE_METHOD,
            OAUTH_TIMESTAMP,
            OAUTH_TOKEN,
            OAUTH_VERSION
        )
    }
}