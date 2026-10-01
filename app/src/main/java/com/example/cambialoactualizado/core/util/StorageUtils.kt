package com.example.cambialoactualizado.core.util

import android.content.SharedPreferences
import com.example.cambialoactualizado.data.model.P2PFavorite
import org.json.JSONArray
import org.json.JSONObject

fun loadFavorites(prefs: SharedPreferences): List<P2PFavorite> {
    val jsonStr = prefs.getString("p2p_favs", "[]") ?: "[]"
    val list = mutableListOf<P2PFavorite>()
    try {
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val banksArr = obj.getJSONArray("banks")
            val banks = mutableListOf<String>()
            for (j in 0 until banksArr.length()) banks.add(banksArr.getString(j))
            list.add(
                P2PFavorite(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    fiat = obj.getString("fiat"),
                    tradeType = obj.getString("tradeType"),
                    amount = obj.getString("amount"),
                    isVerified = obj.getBoolean("isVerified"),
                    banks = banks
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun saveFavorites(prefs: SharedPreferences, list: List<P2PFavorite>) {
    val arr = JSONArray()
    list.forEach { fav ->
        val obj = JSONObject()
        obj.put("id", fav.id)
        obj.put("name", fav.name)
        obj.put("fiat", fav.fiat)
        obj.put("tradeType", fav.tradeType)
        obj.put("amount", fav.amount)
        obj.put("isVerified", fav.isVerified)
        val banksArr = JSONArray()
        fav.banks.forEach { banksArr.put(it) }
        obj.put("banks", banksArr)
        arr.put(obj)
    }
    prefs.edit().putString("p2p_favs", arr.toString()).apply()
}
