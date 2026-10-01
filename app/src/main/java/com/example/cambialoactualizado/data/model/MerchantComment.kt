package com.example.cambialoactualizado.data.model

data class MerchantComment(
    val userName: String,
    val date: String,
    val content: String,
    val isPositive: Boolean,
    val payMethod: String = "",
    val tag: String = ""
)
