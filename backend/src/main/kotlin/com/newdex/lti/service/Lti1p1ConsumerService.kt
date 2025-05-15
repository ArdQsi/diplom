package com.newdex.lti.service

import com.newdex.lti.model.LtiContextData
import com.newdex.lti.model.LtiUserData
import com.newdex.lti.security.OAuthUtils
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.net.URLDecoder

@Service
class Lti1p1ConsumerService(
    private val templateEngine: SpringTemplateEngine
) {
    private var ltiUserData: LtiUserData? = null
    private var ltiContextData: LtiContextData? = null
    private var ltiLaunchPresentationLocale: String? = null
    private var ltiCustomParameters: Map<String, String>? = null
    private var extraClaims: Map<String, Any> = mutableMapOf()

    fun setUserData(userId: String,
                    roles: String,
                    resultSourcedId: String? = null,
                    personSourcedId: String? = null,
                    email: String? = null,
                    fullName: String? = null) {
        ltiUserData = LtiUserData(
            userId = userId,
            roles = roles,
            lisResultSourcedId = resultSourcedId,
            lisPersonSourcedId = personSourcedId,
            lisPersonContactEmailPrimary = email,
            lisPersonNameFull = fullName
        )
    }

    fun setContextData(contextData: LtiContextData) {
        this.ltiContextData = contextData
    }

    fun setLaunchPresentationLocale(locale: String) {
        this.ltiLaunchPresentationLocale = locale
    }

    fun setCustomParameters(customParameters: Map<String, String>) {
        this.ltiCustomParameters = customParameters
    }

    fun setExtraClaims(claims: Map<String, Any>) {
        this.extraClaims = claims
    }

    // Подписываем запрос на запуск LTI и возвращаем параметры подписи и OAuth
    fun generateLaunchRequest(launchUrl: String, consumerKey: String, sharedSecret: String, resourceLinkId: String): Map<String, String> {
        val ltiParameters = mutableMapOf(
            "oauth_callback" to "about:blank",
            "launch_presentation_return_url" to "",
            "lti_message_type" to "basic-lti-launch-request",
            "lti_version" to "LTI-1p0",
            "resource_link_id" to resourceLinkId,
            "tool_consumer_info_product_family_code" to "learn"
        )

        ltiUserData?.also  {
            ltiParameters["user_id"] = it.userId
            ltiParameters["roles"] = it.roles
            it.lisResultSourcedId?.let { id -> ltiParameters["lis_result_sourcedid"] = id }
            it.lisPersonSourcedId?.let { id -> ltiParameters["lis_person_sourcedid"] = id }
            it.lisPersonContactEmailPrimary?.let { email -> ltiParameters["lis_person_contact_email_primary"] = email }
            it.lisPersonNameFull?.let { name -> ltiParameters["lis_person_name_full"] = name }
        } ?: throw IllegalArgumentException("Required user data isn't set.")

        ltiContextData?.let {
            ltiParameters["context_id"] = it.contextId
            ltiParameters["context_title"] = it.contextTitle
            ltiParameters["context_label"] = it.contextLabel
        } ?: throw IllegalArgumentException("Required context data isn't set.")

        ltiCustomParameters?.let {
            ltiParameters.putAll(it)
        }

        extraClaims.let {
            ltiParameters.putAll(it.mapValues { it.value.toString() })
        }

        val headers = mapOf("Content-Type" to "application/x-www-form-urlencoded")
        val oauthSignature = OAuthUtils.getOAuthRequestSignature(consumerKey, sharedSecret, launchUrl, headers, ltiParameters)

        println(oauthSignature)
        val signatureParams = oauthSignature.split(",").associate {
            val parts = it.trim().removePrefix("OAuth ").split("=")
            val key = parts[0].trim()
            val value = parts[1].trim().removeSurrounding("\"")
            key to value
        }.toMutableMap()

        signatureParams["oauth_signature"] = URLDecoder.decode(
            signatureParams["oauth_signature"],
            Charsets.UTF_8.name()
        )
        println(signatureParams)

        ltiParameters.putAll(signatureParams)
        return ltiParameters
    }

    fun ltiLaunch(launchUrl: String, consumerKey: String, sharedSecret: String,
                  resourceLinkId: String): String  {

        val ltiParameters = generateLaunchRequest(launchUrl, consumerKey, sharedSecret, resourceLinkId)

        val context = Context().apply {
            setVariable("lti_parameters", ltiParameters)
            setVariable("launch_url", launchUrl)
            setVariable("element_id", "lti-${System.currentTimeMillis()}")
        }

        return templateEngine.process("lti/launch", context)
    }
}