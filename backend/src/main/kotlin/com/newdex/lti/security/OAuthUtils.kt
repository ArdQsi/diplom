package com.newdex.lti.security

import com.newdex.lti.errors.Lti1p1Error

object OAuthUtils {
    @Throws(Lti1p1Error::class)
    fun getOAuthRequestSignature(
        key: String,
        secret: String,
        url: String,
        headers: Map<String, String>,
        body: Map<String, String>
    ): String {
        try {
            val signer = OAuth1Signer(key, secret)
            val signedHeaders = signer.sign(
                url = url.trim(),
                httpMethod = "POST",
                body = body,
                headers = headers
            )
            return signedHeaders["Authorization"] ?: throw Lti1p1Error("No Authorization header in signed request")
        } catch (err: Exception) {
            throw Lti1p1Error("Failed to sign OAuth request", err)
        }
    }
}

