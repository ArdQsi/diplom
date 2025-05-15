package com.newdex.lti.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Lti11Configuration(
    @JsonProperty("version")
    val version: String,
    @JsonProperty("toolName")
    val toolName: String,
    @JsonProperty("toolDescription")
    val toolDescription: String,
    @JsonProperty("toolUrl")
    val toolUrl: String,
    @JsonProperty("consumerKey")
    val consumerKey: String,
    @JsonProperty("sharedSecret")
    val sharedSecret: String,
    @JsonProperty("resourceLinkId")
    val resourceLinkId: String,
    @JsonProperty("customParams")
    val customParams: String?,
    @JsonProperty("gradingRule")
    val gradingRule: String?
)