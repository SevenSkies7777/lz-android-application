package com.ndma.livelihoodzones.ui.model

class QuestionnaireApiResponse(
    val questionnaireResponseStatus: String,
    val responseMessage: String,
    val questionnaireUniqueId: String,
    val questionnaireType: Int
)