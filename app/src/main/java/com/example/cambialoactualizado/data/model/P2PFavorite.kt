package com.example.cambialoactualizado.data.model

data class P2PFavorite(
    val id: String,
    val name: String,
    val fiat: String,
    val tradeType: String,
    val amount: String,
    val isVerified: Boolean,
    val banks: List<String>
)
