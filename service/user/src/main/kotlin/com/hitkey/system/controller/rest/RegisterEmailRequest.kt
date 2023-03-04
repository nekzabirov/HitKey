package com.hitkey.system.controller.rest

data class RegisterEmailRequest(val email: String)

data class ConfirmEmailRequest(val token: String)