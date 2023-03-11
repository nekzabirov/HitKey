package com.hitkey.common.config

class NotFoundException: RuntimeException()

class UnAuthorizedException: RuntimeException()

class NotPermitted: RuntimeException()

class ParamIsRequired(override val message: String): RuntimeException(message)