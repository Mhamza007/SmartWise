package com.stackbuffers.groceryclient.model

data class User(
    var Ban: String,
    var City: String,
    var Email: String,
    var Mobile_Number: String,
    var Name: String,
    var Reason: String,
    var User_ID: String,
    var profileImageUrl: String
) {
    constructor() : this("", "", "", "", "", "", "", "")
}