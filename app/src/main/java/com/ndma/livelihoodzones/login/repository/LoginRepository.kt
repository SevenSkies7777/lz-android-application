package com.ndma.livelihoodzones.login.repository

import com.ndma.livelihoodzones.login.model.LoginRequestModel
import com.ndma.livelihoodzones.services.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LoginRepository(private val loginService: LoginService) {
    fun signin(loginRequestModel: LoginRequestModel) = flow {
        emit(Resource.loading(null))
        emit(loginService.signin(loginRequestModel))
    }.flowOn(Dispatchers.IO)
}