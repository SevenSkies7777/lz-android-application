package com.silasonyango.ndma.login.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.gson.Gson
import com.silasonyango.ndma.MainActivity
import com.silasonyango.ndma.appStore.AppStore
import com.silasonyango.ndma.appStore.model.CountyLevelQuestionnaireListObject
import com.silasonyango.ndma.config.Constants
import com.silasonyango.ndma.config.EndPoints
import com.silasonyango.ndma.databinding.ActivityLoginBinding
import com.silasonyango.ndma.login.model.LoginRequestModel
import com.silasonyango.ndma.login.model.LoginResponseModel
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

                        val sharedPreferences: SharedPreferences? = baseContext?.applicationContext?.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
                        val editor: SharedPreferences.Editor? =  sharedPreferences?.edit()
                        val gson = Gson()

                        if (!sharedPreferences?.getString(Constants.GEOGRAPHY_OBJECT, null).isNullOrEmpty()) {
                            editor?.remove(Constants.GEOGRAPHY_OBJECT)
                        }
                        val loginResponseModel: LoginResponseModel? = resource.data
                        val responsesJson: String = gson.toJson(loginResponseModel?.geography)
                        editor?.putString(Constants.GEOGRAPHY_OBJECT, responsesJson)
                        editor?.commit()

                        val i = Intent(this@Login, MainActivity::class.java)
                        startActivity(i)
                    }
                    Status.ERROR -> {
                        binding.apply {
                            errorText.text = "An unexpected error has occured"
                            errorText.visibility = View.VISIBLE
                            loginProgressBar.visibility = View.GONE
                            loginText.visibility = View.VISIBLE
                        }
                    }
                    Status.LOADING -> {
                        binding.apply {
                            errorText.text = ""
                            errorText.visibility = View.GONE
                        }
                    }
                    Status.UNAUTHORISED -> {
                        binding.apply {
                            errorText.text = "Invalid credentials"
                            errorText.visibility = View.VISIBLE
                            loginProgressBar.visibility = View.GONE
                            loginText.visibility = View.VISIBLE
                        }
                    }
                    Status.UNPROCESSABLE_ENTITY -> {
                        binding.apply {
                            errorText.text = "No user exists by this email"
                            errorText.visibility = View.VISIBLE
                            loginProgressBar.visibility = View.GONE
                            loginText.visibility = View.VISIBLE
                        }
                    }
                }
            }
        })
    }


}