package com.ndma.livelihoodzones.login.repository

import com.ndma.livelihoodzones.login.model.LoginRequestModel

class LoginApiHelper(private val loginService: LoginService) {
    suspend fun signin(loginRequestModel: LoginRequestModel) = loginService.signin(loginRequestModel)
}