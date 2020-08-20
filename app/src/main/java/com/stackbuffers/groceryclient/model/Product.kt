package com.stackbuffers.groceryclient.model

data class Product(
    val Category_ID: String,
    val Description: String,
    val Discount_Price: String,
    val Product_ID: String,
    val Product_Name: String,
    val Product_Price: String,
    val Product_image: String,
    val SubCategory_ID: String,
    val Unit: String
) {
    constructor() : this("", "", "", "", "", "", "", "", "")
}