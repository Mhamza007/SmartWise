package com.stackbuffers.groceryclient.model

data class WishList(val productId: String, val dateAdded: String) {
    constructor() : this("", "")
}