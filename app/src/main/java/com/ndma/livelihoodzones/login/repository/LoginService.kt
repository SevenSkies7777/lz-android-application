package com.ndma.livelihoodzones.login.repository

import com.google.gson.Gson
import com.ndma.livelihoodzones.config.EndPoints
import com.ndma.livelihoodzones.login.model.LoginRequestModel
import com.ndma.livelihoodzones.login.model.LoginResponseModel
import com.ndma.livelihoodzones.services.model.Resource
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume


class LoginService {
    suspend fun signin(loginRequestModel: LoginRequestModel): Resource<LoginResponseModel?> =
        suspendCancellableCoroutine { continuation ->
            val baseUrl = EndPoints.BASE_URL
            val path = "/users/signin"
            val json = Gson().toJson(loginRequestModel)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("$baseUrl$path")
                .header("Accepts", "application/json")
                .post(body)
                .build()

            val responseCallback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(
                        Resource.error(
                            "An error occurred while signing in user",
                            null
                        )
                    )
                }

                override fun onResponse(call: Call, resp: Response) {
                    if (resp.code == 200) {
                        val responseData =
                            Gson().fromJson(
                                resp.body?.string(),
                                LoginResponseModel::class.java
                            )
                        continuation.resume(Resource.success(responseData))
                    } else if (resp.code == 401) {
                        continuation.resume(
                            Resource.unauthorised(
                                "Invalid credentials",
                                null
                            )
                        )

                    } else if (resp.code == 422) {
                        continuation.resume(
                            Resource.unprocessableEntity(
                                "User does not exist",
                                null
                            )
                        )
                    }

                    else {
                        continuation.resume(
                            Resource.error(
                                "An error occurred while fetching the client notifications",
                                null
                            )
                        )
                    }
                }
            }

            val call = OkHttpClient().newCall(request)
            call.enqueue(responseCallback)
            continuation.invokeOnCancellation { call.cancel() }
        }
}