package xyz.memothelemo.edenmc.exceptions

import xyz.memothelemo.edenmc.gateway.GatewayError
import xyz.memothelemo.edenmc.gateway.GatewayErrorCode

class GatewayException : Exception {
    val code: GatewayErrorCode
    val errorMessage: String

    constructor(error: GatewayError) : super(formatMessage(error.code, error.message)) {
        this.code = error.code
        this.errorMessage = error.message
    }

    constructor(code: GatewayErrorCode, message: String) : super(formatMessage(code, message)) {
        this.code = code
        this.errorMessage = message
    }

    constructor(code: GatewayErrorCode, message: String, cause: Exception) : super(formatMessage(code, message), cause) {
        this.code = code
        this.errorMessage = message
    }
}

private fun formatMessage(code: GatewayErrorCode, message: String) =
    "Gateway error (${code.value}): $message"
