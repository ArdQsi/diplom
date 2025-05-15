package com.newdex.lti.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Lti13Configuration(
    @JsonProperty("version")
    val version: String,
    @JsonProperty("toolName")
    val toolName: String,
    @JsonProperty("toolDescription")
    val toolDescription: String,
    @JsonProperty("oidcUrl")
    val oidcUrl: String,
    @JsonProperty("toolUrl")
    val toolUrl: String,
    @JsonProperty("rsaPrivateKey")
    val rsaPrivateKey: String,
    @JsonProperty("rsaKeyId")
    val rsaKeyId: String,
    @JsonProperty("customParams")
    val customParams: String?,
    @JsonProperty("gradingRule")
    val gradingRule: String?
)