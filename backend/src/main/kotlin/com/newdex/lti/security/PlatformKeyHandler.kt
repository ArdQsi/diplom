package com.newdex.lti.security

import com.newdex.lti.errors.InvalidRsaKeyException
import com.newdex.lti.errors.RsaKeyNotSetException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

class PlatformKeyHandler(private val keyPem: String?, private val kid: String? = null) {
    private var key: PrivateKey? = null

    init {
        if (!keyPem.isNullOrEmpty()) {
            try {
                val privateKeyBytes = keyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\n", "")
                    .trim()
                    .toByteArray()

                val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBytes))
                val keyFactory = KeyFactory.getInstance("RSA")
                key = keyFactory.generatePrivate(keySpec)
            } catch (e: Exception) {
                throw InvalidRsaKeyException(e)
            }
        }
    }

    fun encodeAndSign(message: Map<String, Any>, expiration: Long? = null): String {
        if (key == null) {
            throw RsaKeyNotSetException()
        }

        val claims = HashMap(message)
        val now = System.currentTimeMillis()
        if (expiration != null) {
            claims["iat"] = Date(now)
            claims["exp"] = Date(now + expiration * 1000)
        }

        return Jwts.builder()
            .setClaims(claims)
            .setHeaderParam("kid", kid)
            .signWith(key, SignatureAlgorithm.RS256)
            .compact()
    }

    fun getPublicJwk(kid: String): String? {
        return key?.let { privateKey ->
            val keyFactory = KeyFactory.getInstance("RSA")

            val rsaPrivate = keyFactory.getKeySpec(
                privateKey,
                java.security.spec.RSAPrivateCrtKeySpec::class.java
            ) as java.security.spec.RSAPrivateCrtKeySpec

            val nBytes = rsaPrivate.modulus.toByteArray().let {
                if (it[0].toInt() == 0) it.copyOfRange(1, it.size) else it
            }
            val eBytes = rsaPrivate.publicExponent.toByteArray().let {
                if (it[0].toInt() == 0) it.copyOfRange(1, it.size) else it
            }

            val encoder = Base64.getUrlEncoder().withoutPadding()
            val nBase64 = encoder.encodeToString(nBytes)
            val eBase64 = encoder.encodeToString(eBytes)

            """
        {
          "kty": "RSA",
          "kid": "$kid",
          "use": "sig",
          "alg": "RS256",
          "n": "$nBase64",
          "e": "$eBase64"
        }
        """.trimIndent()
        }
    }
}
