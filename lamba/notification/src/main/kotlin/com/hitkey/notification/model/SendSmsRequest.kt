package com.hitkey.notification.model

data class SendSmsRequest(
    val phoneNumber: String,
    val text: String
)
