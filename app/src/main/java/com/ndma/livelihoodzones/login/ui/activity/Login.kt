package com.ndma.livelihoodzones.login.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.ndma.livelihoodzones.MainActivity
import com.ndma.livelihoodzones.appStore.AppStore
import com.ndma.livelihoodzones.config.Constants
import com.ndma.livelihoodzones.databinding.ActivityLoginBinding
import com.ndma.livelihoodzones.login.model.LoginRequestModel
import com.ndma.livelihoodzones.login.model.LoginResponseModel
import com.ndma.livelihoodzones.login.repository.LoginRepository
import com.ndma.livelihoodzones.login.repository.LoginService
import com.ndma.livelihoodzones.login.viewmodel.LoginViewModel
import com.ndma.livelihoodzones.login.viewmodel.LoginViewModelProvider
import com.ndma.livelihoodzones.services.model.Status

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

        if (isAlreadyLoggedIntoThisDevice()) {
            val i = Intent(this@Login, MainActivity::class.java)
            startActivity(i)
        }
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
                        editor?.putString(Constants.EXISTING_ACCOUNT, "Existing account")
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

    fun isAlreadyLoggedIntoThisDevice(): Boolean {
        val sharedPreferences: SharedPreferences? = baseContext?.applicationContext?.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor? =  sharedPreferences?.edit()
        val gson = Gson()

        if (sharedPreferences?.getString(Constants.EXISTING_ACCOUNT, null).isNullOrEmpty()) {
            return false
        }
        return true
    }


}