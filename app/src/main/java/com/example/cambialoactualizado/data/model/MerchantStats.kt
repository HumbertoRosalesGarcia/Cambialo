package com.example.cambialoactualizado.data.model

data class MerchantStats(
    val totalTradeCount: Int = 0,
    val buyOrderCount: Int = 0,
    val sellOrderCount: Int = 0,
    val monthOrderCount: Int = 0,
    val monthFinishRate: Double = 0.0,
    val avgReleaseTime: Double = 0.0,
    val avgPayTime: Double = 0.0,
    val registerDays: Int = 0,
    val firstTradeDays: Int = 0,
    val counterpartyCount: Int = 0,
    val tradingType: String = "Trader"
)
