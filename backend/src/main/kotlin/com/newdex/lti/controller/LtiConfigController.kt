package com.newdex.lti.controller

import com.newdex.lti.model.Lti11Configuration
import com.newdex.lti.model.Lti13Configuration
import com.newdex.lti.service.LtiConfigService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/lti-config")
class LtiConfigController(
    private val configService: LtiConfigService
) {
    @GetMapping("/{courseId}/v1.1")
    fun getLti11Config(@PathVariable courseId: String): ResponseEntity<Lti11Configuration> {
        return try {
            ResponseEntity.ok(configService.getLti11Config(courseId))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{courseId}/v1.3")
    fun getLti13Config(@PathVariable courseId: String): ResponseEntity<Lti13Configuration> {
        return try {
            ResponseEntity.ok(configService.getLti13Config(courseId))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{courseId}/v1.1")
    fun updateLti11Config(
        @PathVariable courseId: String,
        @RequestBody newConfig: Lti11Configuration
    ): ResponseEntity<Void> {
        configService.updateConfig(courseId, newConfig)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{courseId}/v1.3")
    fun updateLti13Config(
        @PathVariable courseId: String,
        @RequestBody newConfig: Lti13Configuration
    ): ResponseEntity<Void> {
        configService.updateConfig(courseId, newConfig)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/v1.1")
    fun addLti11Config(
        @RequestParam courseId: String,
        @RequestBody config: Lti11Configuration
    ): ResponseEntity<Void> {
        return try {
            configService.addConfig(courseId, config)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/v1.3")
    fun addLti13Config(
        @RequestParam courseId: String,
        @RequestBody config: Lti13Configuration
    ): ResponseEntity<Void> {
        return try {
            configService.addConfig(courseId, config)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping
    fun getAllConfigs(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(configService.getAllConfigs())
    }
}