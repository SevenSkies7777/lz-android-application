package com.silasonyango.ndma.login.viewmodel

import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.silasonyango.ndma.R
import com.silasonyango.ndma.config.EndPoints
import com.silasonyango.ndma.login.model.LoginRequestModel
import com.silasonyango.ndma.login.model.LoginResponseModel
import com.silasonyango.ndma.login.repository.LoginRepository
import com.silasonyango.ndma.login.repository.LoginService
import com.silasonyango.ndma.services.model.Resource
import com.silasonyango.ndma.services.retrofit.RetrofitClientInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import okhttp3.ResponseBody
import retrofit2.Response

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