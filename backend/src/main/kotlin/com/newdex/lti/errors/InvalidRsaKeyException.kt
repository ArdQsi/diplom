package com.newdex.lti.errors

class InvalidRsaKeyException(cause: Throwable) : Exception("Invalid RSA key", cause)