package com.stackbuffers.groceryclient.model

data class Category(
    val Category_ID: String,
    val Category_Name: String,
    val Category_image: String
) {
    constructor() : this("", "", "")
}