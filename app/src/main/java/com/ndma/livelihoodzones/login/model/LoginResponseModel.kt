package com.ndma.livelihoodzones.login.model

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