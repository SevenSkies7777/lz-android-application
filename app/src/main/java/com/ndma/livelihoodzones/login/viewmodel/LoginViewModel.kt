package com.ndma.livelihoodzones.login.viewmodel

import androidx.lifecycle.*
import com.ndma.livelihoodzones.login.model.LoginRequestModel
import com.ndma.livelihoodzones.login.model.LoginResponseModel
import com.ndma.livelihoodzones.login.repository.LoginRepository
import com.ndma.livelihoodzones.services.model.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    val loginResponse: LiveData<Resource<LoginResponseModel?>> = MutableLiveData(null)

    fun signin(loginRequestModel: LoginRequestModel) {
        viewModelScope.launch {
            loginRepository.signin(loginRequestModel)
                .collect { commitLoginResponse(it) }
        }
    }

    fun commitLoginResponse(loginResponse: Resource<LoginResponseModel?>) {
        (this.loginResponse as MutableLiveData).value = loginResponse
    }
}