package com.hitkey.common.data

sealed class HitResponse(val success: Boolean) {
    data class OK<T>(
        val data: T?
    ): HitResponse(true) {
        constructor() : this(data = null)
    }

    data class Error(
        val error: String
    ): HitResponse(false)
}
