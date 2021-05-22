package com.ndma.livelihoodzones.ui.wealthgroup.repository

import com.google.gson.Gson
import com.ndma.livelihoodzones.appStore.AppStore
import com.ndma.livelihoodzones.appStore.model.WealthGroupQuestionnaire
import com.ndma.livelihoodzones.config.EndPoints
import com.ndma.livelihoodzones.services.model.Resource
import com.ndma.livelihoodzones.ui.model.QuestionnaireApiResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume

class WealthGroupService {
    suspend fun submitWealthGroupQuestionnaire(wealthGroupQuestionnaire: WealthGroupQuestionnaire): Resource<QuestionnaireApiResponse?> =
        suspendCancellableCoroutine { continuation ->
            val baseUrl = EndPoints.BASE_URL
            val path = "/responses/wealthgroup"
            val json = Gson().toJson(wealthGroupQuestionnaire)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("$baseUrl$path")
                .header("Accepts", "application/json")
                .header("Authorization", "Bearer "+ AppStore.getInstance().sessionDetails?.accessToken)
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
                                QuestionnaireApiResponse::class.java
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
                                "Duplicate questionnaire",
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