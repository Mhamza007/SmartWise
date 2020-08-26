package com.stackbuffers.groceryclient.model

data class Banner(
    val Banner_ID: String,
    val Banner_Image: String,
    val Banner_Type: String,
    val Category_ID: String,
    val Product_ID: String,
    val Sub_Categories_ID: String
) {
    constructor() : this("", "", "", "", "", "")
}