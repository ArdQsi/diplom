package com.newdex.lti.controller

import com.newdex.lti.model.*
import com.newdex.lti.service.Lti1p1ConsumerService
import com.newdex.lti.service.Lti1p3ConsumerService
import com.newdex.lti.service.LtiConfigService
import com.newdex.lti.model.LtiContextData
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Controller
class LtiController(
    private val configService: LtiConfigService,
    private val lti1p1ConsumerService: Lti1p1ConsumerService,
    private val lti1p3ConsumerService: Lti1p3ConsumerService,
    private val templateEngine: SpringTemplateEngine
) {
    @GetMapping("/api/courses/{courseId}/launch")
    fun launchLti(@PathVariable courseId: String): ResponseEntity<String> {
        return when (configService.getLtiVersion(courseId)) {
            LtiVersion.V1_1 -> return handleLti1p1Launch(configService.getLti11Config(courseId))
            LtiVersion.V1_3 -> return handleLti1p3Launch(configService.getLti13Config(courseId))
        }
    }

    private fun handleLti1p1Launch(config: Lti11Configuration): ResponseEntity<String> {
        // эти параметры должны приходить со фронта
        lti1p1ConsumerService.setUserData(
            "1",
            "Student"
        )

        // эти перменные берутся из переменных окружения, где развернут
        lti1p1ConsumerService.setContextData(
            LtiContextData(
                contextId = "your_context_id",
                contextTitle = "your_context_title",
                contextLabel = "your_context_label"
            )
        )

        val htmlContent = lti1p1ConsumerService.ltiLaunch(
            config.toolUrl,
            config.consumerKey,
            config.sharedSecret,
            config.resourceLinkId
        )

        return ResponseEntity.ok()
            .header("Content-Type", "text/html; charset=UTF-8")
            .body(htmlContent)
    }

    private fun handleLti1p3Launch(config: Lti13Configuration): ResponseEntity<String> {
        lti1p3ConsumerService.init(config, "http://localhost:8080", "client1", "1")
        val oidcURL = lti1p3ConsumerService.preparePreflightUrl()
        val context = Context()
        context.setVariable("oidcURL", oidcURL)

        val html = templateEngine.process("lti/redirect", context)

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html)
    }

    @PostMapping("/auth")
    fun postAuthLti(@RequestParam params: MultiValueMap<String, String>): ModelAndView {
        return lti1p3ConsumerService.buildLtiLaunchForm(params)
    }

    @GetMapping("/auth")
    fun getAuthLti(@RequestParam params: MultiValueMap<String, String>): ModelAndView {
        return lti1p3ConsumerService.buildLtiLaunchForm(params)
    }
}