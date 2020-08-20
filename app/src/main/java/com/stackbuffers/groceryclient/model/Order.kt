package com.stackbuffers.groceryclient.model

data class Order(
    val Date: String,
    val Order_ID: String,
    val Product_ID: String,
    val Product_Name: String,
    val Product_Price: String,
    val Quantity: String,
    val Status: String,
    val User_ID: String
) {
    constructor() : this("", "", "", "", "", "", "", "")
}