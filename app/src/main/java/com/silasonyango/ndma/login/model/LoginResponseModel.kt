package com.silasonyango.ndma.login.model

import com.silasonyango.ndma.ui.county.model.CountyModel
import com.silasonyango.ndma.ui.county.model.LivelihoodZoneModel

data class LoginResponseModel(
    var accessToken: String, var authenticationStatus: String,
    var authenticationSuccessful: Boolean,
    var firstName: String,
    var middleName: String,
    var organizationName: String,
    var surname: String,
    var userEmail: String,
    var roles: List<Roles>,
    var geography: GeographyObject
)