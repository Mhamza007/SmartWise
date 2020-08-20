package com.stackbuffers.groceryclient.model

data class Cart(val productId: String, val dateAdded: String) {
    constructor() : this("", "")
}