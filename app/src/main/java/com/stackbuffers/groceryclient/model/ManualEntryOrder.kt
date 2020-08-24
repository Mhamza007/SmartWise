package com.stackbuffers.groceryclient.model

data class ManualEntryOrder(var productName: String, var quantity: String) {
    constructor() : this("", "")
}