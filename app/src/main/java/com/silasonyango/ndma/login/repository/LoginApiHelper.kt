package com.silasonyango.ndma.login.repository

import com.silasonyango.ndma.login.model.LoginRequestModel

class LoginApiHelper(private val loginService: LoginService) {
    suspend fun signin(loginRequestModel: LoginRequestModel) = loginService.signin(loginRequestModel)
}