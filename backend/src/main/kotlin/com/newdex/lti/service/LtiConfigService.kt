package com.newdex.lti.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.newdex.lti.model.Lti11Configuration
import com.newdex.lti.model.Lti13Configuration
import com.newdex.lti.model.LtiVersion
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@Service
class LtiConfigService {
    private val mapper = ObjectMapper(YAMLFactory())
        .registerKotlinModule()
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE) // Для поддержки kebab-case

    private val configFile = File("lti-config.yaml")
    private val configCache: MutableMap<String, Any> = ConcurrentHashMap()

    @PostConstruct
    fun init() {
        reloadConfig()
    }

    fun getLtiVersion(courseId: String): LtiVersion {
        return when {
            configCache[courseId] is Lti11Configuration -> LtiVersion.V1_1
            configCache[courseId] is Lti13Configuration -> LtiVersion.V1_3
            else -> throw IllegalArgumentException("Unknown LTI version for course $courseId")
        }
    }

    fun getLti11Config(courseId: String): Lti11Configuration {
        return configCache[courseId] as? Lti11Configuration
            ?: throw IllegalArgumentException("LTI 1.1 config not found for $courseId")
    }

    fun getLti13Config(courseId: String): Lti13Configuration {
        return configCache[courseId] as? Lti13Configuration
            ?: throw IllegalArgumentException("LTI 1.3 config not found for $courseId")
    }

    fun getAllConfigs(): Map<String, Any> {
        return configCache.toMap()
    }

    fun updateConfig(courseId: String, newConfig: Any) {
        println(newConfig)
        when (newConfig) {
            is Lti11Configuration, is Lti13Configuration -> {
                configCache[courseId] = newConfig
                println(configCache)
                saveConfigToFile()
            }
            else -> throw IllegalArgumentException("Unsupported configuration type")
        }
    }

    fun addConfig(courseId: String, config: Any) {
        if (configCache.containsKey(courseId)) {
            throw IllegalStateException("Configuration already exists for $courseId")
        }
        updateConfig(courseId, config)
    }

    @Synchronized
    private fun reloadConfig() {
        try {
            if (configFile.exists()) {
                configFile.inputStream().use { input ->
                    val rawMap = mapper.readValue(input, object : TypeReference<Map<String, Any>>() {})
                    configCache.clear()

                    rawMap.forEach { (courseId, configData) ->
                        val config = when {
                            // Проверяем наличие поля, характерного для LTI 1.1
                            (configData as Map<*, *>).containsKey("consumerKey") ->
                                mapper.convertValue(configData, Lti11Configuration::class.java)
                            // Проверяем наличие поля, характерного для LTI 1.3
                            configData.containsKey("oidcUrl") ->
                                mapper.convertValue(configData, Lti13Configuration::class.java)
                            else -> throw IllegalStateException("Unknown config type for course $courseId")
                        }
                        configCache[courseId] = config
                    }

                    println("Loaded configs: $configCache")
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to reload LTI configurations", e)
        }
    }

    @Synchronized
    private fun saveConfigToFile() {
        try {
            mapper.writeValue(configFile, configCache)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to save LTI configurations", e)
        }
    }
}