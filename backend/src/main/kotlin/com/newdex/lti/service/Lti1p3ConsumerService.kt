package com.newdex.lti.service

import com.newdex.lti.errors.PreflightRequestValidationFailure
import com.newdex.lti.model.Lti13Configuration
import com.newdex.lti.security.PlatformKeyHandler
import com.newdex.lti.utility.LtiConstants
import com.newdex.lti.utility.LtiConstants.LTI_1P3_ROLE_MAP
import com.newdex.lti.utility.LtiConstants.LTI_BASE_MESSAGE
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.servlet.ModelAndView
import java.net.URLEncoder

@Service
class Lti1p3ConsumerService() {
    private lateinit var config: Lti13Configuration
    private lateinit var keyHandler: PlatformKeyHandler
    private lateinit var oidcUrl: String
    private lateinit var launchUrl: String
    private lateinit var redirectUris: String
    private lateinit var rsaKey: String
    private lateinit var rsaKeyId: String
    private lateinit var iss: String
    private lateinit var clientId: String
    private lateinit var deploymentId: String

    fun init(config: Lti13Configuration, iss: String, clientId: String, deploymentId: String) {
        this.config = config
        this.oidcUrl = config.oidcUrl
        this.launchUrl = config.toolUrl
        this.redirectUris = config.oidcUrl
        this.rsaKey = config.rsaPrivateKey
        this.rsaKeyId = config.rsaKeyId
        this.keyHandler = PlatformKeyHandler(rsaKey, rsaKeyId)
        this.iss = iss
        this.clientId = clientId
        this.deploymentId = deploymentId
    }

    // LTI Claim data
    private var ltiClaimUserData: Map<String, Any>? = null
    private var ltiClaimResourceLink: Map<String, Any>? = null
    private var ltiClaimLaunchPresentation: Map<String, Any>? = null
    private var ltiClaimContext: Map<String, Any>? = null
    private var ltiClaimCustomParameters: Map<String, Any>? = null
    val extraClaims: MutableMap<String, Any> = mutableMapOf()

    fun getLtiUserRoles(role: String?): List<String> {
        val ltiUserRoles = mutableSetOf<String>()

        role?.let {
            if (!LTI_1P3_ROLE_MAP.containsKey(it)) {
                throw IllegalArgumentException("Invalid role list provided.")
            }

            ltiUserRoles.addAll(LTI_1P3_ROLE_MAP[it] ?: emptyList())
        }

        return ltiUserRoles.toList()
    }

    fun setUserData(
        userId: String,
        role: String,
        givenName: String? = null,
        familyName: String? = null,
        name: String? = null,
        email: String? = null,
        locale: String? = null
    ) {
        ltiClaimUserData = mutableMapOf<String, Any>().apply {
            put("sub", userId)
            put("https://purl.imsglobal.org/spec/lti/claim/roles", getLtiUserRoles(role))
            givenName?.let { put("given_name", it) }
            familyName?.let { put("family_name", it) }
            name?.let { put("name", it) }
            email?.let { put("email", it) }
            locale?.let { put("locale", it) }
        }
    }

    fun setResourceLinkClaim(
        resourceLinkId: String,
        description: String? = null,
        title: String? = null
    ) {
        ltiClaimResourceLink = mapOf(
            "https://purl.imsglobal.org/spec/lti/claim/resource_link" to mutableMapOf<String, Any>().apply {
                put("id", resourceLinkId)
                description?.let { put("description", it) }
                title?.let { put("title", it) }
            }
        )
    }


    // дополнительные claim
    fun setLaunchPresentationClaim(
        documentTarget: String? = null,
        returnUrl: String? = null
    ) {
        documentTarget?.let {
            if (it !in setOf("iframe", "frame", "window")) {
                throw IllegalArgumentException("Invalid launch presentation format.")
            }
        }

        val presentationData = mutableMapOf<String, String>()

        documentTarget?.let { presentationData["document_target"] = it }
        returnUrl?.let { presentationData["return_url"] = it }

        ltiClaimLaunchPresentation = mapOf(
            "https://purl.imsglobal.org/spec/lti/claim/launch_presentation" to presentationData
        )
    }


    fun setContextClaim(
        contextId: String,
        contextTypes: List<LtiConstants.LTI_1P3_CONTEXT_TYPE> = emptyList(),
        contextTitle: String? = null,
        contextLabel: String? = null
    ) {
        val contextClaimData = mutableMapOf<String, Any>(
            "id" to contextId
        )

        val validContextTypes = contextTypes.map { it.uri }
        if (validContextTypes.isNotEmpty()) {
            contextClaimData["type"] = validContextTypes
        }

        contextTitle?.let { contextClaimData["title"] = it }
        contextLabel?.let { contextClaimData["label"] = it }

        ltiClaimContext = mapOf(
            "https://purl.imsglobal.org/spec/lti/claim/context" to contextClaimData
        )
    }


    fun setCustomParameters(customParameters: Map<String, Any>) {
        require(customParameters is Map<*, *>) {
            "Custom parameters must be a key/value dictionary."
        }

        ltiClaimCustomParameters = mapOf(
            "https://purl.imsglobal.org/spec/lti/claim/custom" to customParameters
        )
    }


    // создание LTI сообщения(создает все параметры для запуска инструмента)
    fun getLtiLaunchMessage(includeExtraClaims: Boolean = true): Map<String, Any> {
        val ltiMessage = mutableMapOf<String, Any>().apply {
            putAll(LTI_BASE_MESSAGE)
        }

        ltiMessage.putAll(mapOf<String, Any>(
            "iss" to iss,
            "aud" to clientId,
            "azp" to clientId,
            "https://purl.imsglobal.org/spec/lti/claim/deployment_id" to deploymentId,
            "https://purl.imsglobal.org/spec/lti/claim/target_link_uri" to launchUrl
        ))

        ltiClaimResourceLink?.let {
            ltiMessage.putAll(it)
        } ?: throw IllegalStateException("Required resource_link data isn't set.")

        ltiClaimUserData?.let {
            ltiMessage.putAll(it)
        } ?: throw IllegalStateException("Required user data isn't set.")


        if (includeExtraClaims) {
            ltiClaimContext?.let { ltiMessage.putAll(it) }
            ltiClaimLaunchPresentation?.let { ltiMessage.putAll(it) }
            ltiClaimCustomParameters?.let { ltiMessage.putAll(it) }
            extraClaims?.let { ltiMessage.putAll(it) }
        }

        return ltiMessage
    }

    // формирование OIDC запроса(первоначальный запрос)
    fun preparePreflightUrl(): String {
        val parameters = mapOf(
            "iss" to iss,
            "target_link_uri" to launchUrl,
            "login_hint" to clientId
        )

        return "$oidcUrl?${parameters.toFormUrlEncoded()}"
    }

    private fun Map<String, String>.toFormUrlEncoded(): String {
        return this.entries.joinToString("&") { (key, value) ->
            "${key.encodeURL()}=${value.encodeURL()}"
        }
    }

    private fun String.encodeURL(): String {
        return URLEncoder.encode(this, "UTF-8")
    }

    // метод запуска инструмента
    fun generateLaunchRequest(preflightResponse: Map<String, String>): Map<String, String> {
        validatePreflightResponse(preflightResponse)
        setUserData("user1","student")
        setResourceLinkClaim("100d101f-2c14-434a-a0f3-57c2a42369fd")

        val ltiLaunchMessage = getLtiLaunchMessage().toMutableMap()

        preflightResponse["nonce"]?.let { nonce ->
            ltiLaunchMessage["nonce"] = nonce
        }

        return mapOf(
            "state" to preflightResponse["state"].orEmpty(),
            "id_token" to keyHandler.encodeAndSign(
                message = ltiLaunchMessage,
                expiration = 3600
            )
        )
    }

    // Валидация ответа от инструмента, чтобы потом сделать запуск инструмента
    private fun validatePreflightResponse(response: Map<String, String>) {
        try {
            val redirectUri = response["redirect_uri"]
            requireNotNull(response["nonce"]) { "Nonce is required" }
            requireNotNull(response["state"]) { "State is required" }
            requireNotNull(redirectUri) { "Redirect URI is required" }
            require(redirectUri in redirectUris) {
                "Redirect URI must be in allowed URIs"
            }
            require(response["client_id"] == clientId) {
                "Client ID must match"
            }
        } catch (ex: IllegalArgumentException) {
            throw PreflightRequestValidationFailure(ex.message ?: "Validation failed", ex)
        }
    }

     fun buildLtiLaunchForm(params: MultiValueMap<String, String>): ModelAndView {
        val preflightResponse = params.toSingleValueMap()
        val redirectUri = preflightResponse["redirect_uri"]
            ?: throw IllegalArgumentException("Missing redirect_uri")
        val response = generateLaunchRequest(preflightResponse)

        return ModelAndView("lti/launch").apply {
            addObject("element_id", "ltiLaunchForm")
            addObject("launch_url", redirectUri)
            addObject("lti_parameters", response)
        }
    }
}