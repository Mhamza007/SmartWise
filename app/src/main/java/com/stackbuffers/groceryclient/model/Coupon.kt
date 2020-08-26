package com.stackbuffers.groceryclient.model

data class Coupon(val Coupon: String, val Coupon_ID: String, val Discount_in_Perc: String, val ExpiryDate: String) {
    constructor(): this("", "", "", "")
}