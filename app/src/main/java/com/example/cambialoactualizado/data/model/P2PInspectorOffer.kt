package com.example.cambialoactualizado.data.model

data class P2PInspectorOffer(
    val merchantName: String,
    val advertiserNo: String,
    val price: Double,
    val minAmount: Double,
    val maxAmount: Double,
    val surplusAmount: Double,
    val payMethods: List<String>,
    val monthOrderCount: Int,
    val monthFinishRate: Double,
    val positiveRate: Double,
    val userType: String,
    val terms: String,
    val requiresVerification: Boolean = false
)
