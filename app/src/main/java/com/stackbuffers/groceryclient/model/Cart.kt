package com.stackbuffers.groceryclient.model

data class Cart(
    val productId: String,
    val dateAdded: String,
    val productName: String,
    val productImage: String,
    val productPrice: String,
    val quantity: Int
) {
    constructor() : this("", "", "", "", "", 0)
}