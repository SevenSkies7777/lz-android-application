package com.silasonyango.ndma.login.viewmodel

import com.silasonyango.ndma.login.repository.LoginRepository

class LoginViewModelProvider(private val loginRepository: LoginRepository) {
    companion object {
        var loginViewModel: LoginViewModel? = null
    }

    fun provideLoginViewModel(): LoginViewModel {
        val existing = loginViewModel
        return if (existing != null) {
            existing
        } else {
            val new =
                    LoginViewModel(
                            loginRepository
                    )
            loginViewModel = new
            new
        }
    }
}