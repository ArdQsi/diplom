package com.newdex.lti.errors

class PreflightRequestValidationFailure(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)