package com.stackbuffers.groceryclient.model

class SubCategory(
    val Category_ID: String,
    val Image: String,
    val SubCategory_ID: String,
    val SubCategory_Name: String
) {
    constructor() : this("", "", "", "")
}