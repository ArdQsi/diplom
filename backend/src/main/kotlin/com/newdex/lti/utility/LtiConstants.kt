package com.newdex.lti.utility

object  LtiConstants {
    val LTI_BASE_MESSAGE = mapOf(
        "https://purl.imsglobal.org/spec/lti/claim/message_type" to "LtiResourceLinkRequest",
        "https://purl.imsglobal.org/spec/lti/claim/version" to "1.3.0"
    )

    val LTI_1P3_ROLE_MAP = mapOf(
        "staff" to listOf(
            "http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator",
            "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor"
        ),
        "instructor" to listOf(
            "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor"
        ),
        "student" to listOf(
            "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Student"
        ),
        "guest" to listOf(
            "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Student"
        )
    )

    val LTI_1P3_CONTEXT_ROLE_MAP = mapOf(
        "staff" to listOf(
            "http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator"
        ),
        "instructor" to listOf(
            "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor"
        ),
        "student" to listOf(
            "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner"
        )
    )

    val LTI_1P3_ACCESS_TOKEN_REQUIRED_CLAIMS = setOf(
        "grant_type",
        "client_assertion_type",
        "client_assertion",
        "scope"
    )

    val LTI_1P3_ACCESS_TOKEN_SCOPES = listOf(
        // LTI-AGS Scopes
        "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly",
        "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem",
        "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly",
        "https://purl.imsglobal.org/spec/lti-ags/scope/score",

        "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"
    )

    const val LTI_1P3_ACS_SCOPE = "https://purl.imsglobal.org/spec/lti-ap/scope/control.all"

    val LTI_DEEP_LINKING_ACCEPTED_TYPES = listOf(
        "ltiResourceLink",
        "link",
        "html",
        "image"
        // TODO: implement "file" support,
    )

    enum class LTI_1P3_CONTEXT_TYPE(val uri: String) {
        GROUP("http://purl.imsglobal.org/vocab/lis/v2/course#CourseGroup"),
        COURSE_OFFERING("http://purl.imsglobal.org/vocab/lis/v2/course#CourseOffering"),
        COURSE_SECTION("http://purl.imsglobal.org/vocab/lis/v2/course#CourseSection"),
        COURSE_TEMPLATE("http://purl.imsglobal.org/vocab/lis/v2/course#CourseTemplate")
    }

    val LTI_PROCTORING_DATA_KEYS = listOf(
        "attempt_number",
        "resource_link_id",
        "session_data",
        "start_assessment_url",
        "assessment_control_url",
        "assessment_control_actions"
    )

    val LTI_PROCTORING_ASSESSMENT_CONTROL_ACTIONS = listOf(
        "pause",
        "resume",
        "terminate",
        "update",
        "flag"
    )
}