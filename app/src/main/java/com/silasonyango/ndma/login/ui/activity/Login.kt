package com.silasonyango.ndma.login.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.silasonyango.ndma.MainActivity
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.config.EndPoints
import com.silasonyango.ndma.databinding.ActivityLoginBinding
import com.silasonyango.ndma.login.model.LoginRequestModel
import com.silasonyango.ndma.login.repository.LoginApiHelper
import com.silasonyango.ndma.login.repository.LoginRepository
import com.silasonyango.ndma.login.repository.LoginService
import com.silasonyango.ndma.login.viewmodel.LoginViewModel
import com.silasonyango.ndma.login.viewmodel.LoginViewModelProvider
import com.silasonyango.ndma.services.model.Status
import com.silasonyango.ndma.services.retrofit.RetrofitClientInstance

class Login : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        loginViewModel =
            LoginViewModelProvider(LoginRepository((LoginService()))).provideLoginViewModel()
        defineViews()
        setupObservers()
    }


    private fun defineViews() {
        binding.apply {
            loginButton.setOnClickListener {
                loginText.visibility = View.GONE
                loginProgressBar.visibility = View.VISIBLE

                binding.apply {
                    loginViewModel.signin(
                        LoginRequestModel(
                            etEmail.text.toString(),
                            etPassword.text.toString()
                        )
                    )
                }

            }
        }
    }

    private fun setupObservers() {
        loginViewModel.loginResponse.observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        binding.loginProgressBar.visibility = View.GONE
                        AppStore.getInstance().sessionDetails = resource.data
                        val i = Intent(this@Login, MainActivity::class.java)
                        startActivity(i)
                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }
                    Status.UNAUTHORISED -> {

                    }
                    Status.UNPROCESSABLE_ENTITY -> {

                    }
                }
            }
        })
    }


}