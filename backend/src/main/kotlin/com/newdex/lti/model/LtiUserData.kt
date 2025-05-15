package com.newdex.lti.model

data class LtiUserData(
    val userId: String,
    val roles: String,
    val lisResultSourcedId: String? = null,
    val lisPersonSourcedId: String? = null,
    val lisPersonContactEmailPrimary: String? = null,
    val lisPersonNameFull: String? = null
)
