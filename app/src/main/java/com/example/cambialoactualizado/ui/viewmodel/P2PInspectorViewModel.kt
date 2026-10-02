package com.example.cambialoactualizado.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambialoactualizado.core.constants.LOCAL_API_URL
import com.example.cambialoactualizado.core.constants.formatBankDisplayName
import com.example.cambialoactualizado.data.model.MerchantComment
import com.example.cambialoactualizado.data.model.MerchantStats
import com.example.cambialoactualizado.data.model.P2PInspectorOffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI

class P2PInspectorViewModel : ViewModel() {
    private val _offers = MutableStateFlow<List<P2PInspectorOffer>>(emptyList())
    val offers: StateFlow<List<P2PInspectorOffer>> = _offers
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _merchantComments = MutableStateFlow<List<MerchantComment>>(emptyList())
    val merchantComments: StateFlow<List<MerchantComment>> = _merchantComments
    private val _selectedMerchantStats = MutableStateFlow(MerchantStats())
    val selectedMerchantStats: StateFlow<MerchantStats> = _selectedMerchantStats
    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails
    private val _totalPositiveComments = MutableStateFlow(0)
    val totalPositiveComments: StateFlow<Int> = _totalPositiveComments
    private val _totalNegativeComments = MutableStateFlow(0)
    val totalNegativeComments: StateFlow<Int> = _totalNegativeComments
    private val _currentCommentPage = MutableStateFlow(1)
    val currentCommentPage: StateFlow<Int> = _currentCommentPage
    private val _isLoadingCommentsPage = MutableStateFlow(false)
    val isLoadingCommentsPage: StateFlow<Boolean> = _isLoadingCommentsPage

    fun searchOffers(fiat: String, tradeType: String, selectedBanks: List<String>, isVerifiedOnly: Boolean, amount: String) {
        _isLoading.value = true
        _offers.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URI("https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search").toURL()
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("clienttype", "web")
                connection.setRequestProperty("lang", if (fiat == "BRL") "pt-BR" else "es")
                connection.setRequestProperty("Origin", "https://p2p.binance.com")
                if (fiat == "BRL") {
                    connection.setRequestProperty("Bnc-Location", "BR")
                }
                connection.doOutput = true

                val jsonObject = JSONObject().apply {
                    put("asset", "USDT")
                    put("fiat", fiat)
                    put("tradeType", tradeType)
                    val payTypesArray = JSONArray()
                    selectedBanks.forEach { payTypesArray.put(it) }
                    put("payTypes", payTypesArray)
                    put("countries", JSONArray())
                    put("page", 1)
                    put("rows", 5)
                    if (isVerifiedOnly) put("publisherType", "merchant") else put("publisherType", JSONObject.NULL)
                    if (amount.isNotEmpty()) put("transAmount", amount)
                    put("proMerchantAds", false)
                    put("shieldMerchantAds", false)
                    put("filterType", "all")
                }
                OutputStreamWriter(connection.outputStream).use { it.write(jsonObject.toString()) }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    val responseJson = JSONObject(response)
                    val dataArray = responseJson.optJSONArray("data") ?: JSONArray()
                    val offerList = mutableListOf<P2PInspectorOffer>()

                    fun checkFlag(json: JSONObject, key: String): Boolean {
                        if (!json.has(key) || json.isNull(key)) return false
                        val value = json.opt(key)
                        return value == true || value == 1 || value == "true" || value == "1"
                    }

                    val limit = minOf(dataArray.length(), 5)
                    for (i in 0 until limit) {
                        val item = dataArray.getJSONObject(i)
                        val adv = item.getJSONObject("adv")
                        val advertiser = item.getJSONObject("advertiser")
                        var terms = adv.optString("remarks", "").trim()
                        if (terms.isEmpty() || terms.equals("null", ignoreCase = true)) terms = ""
                        val methodsArray = adv.getJSONArray("tradeMethods")
                        val methods = mutableListOf<String>()
                        for (j in 0 until methodsArray.length()) {
                            val id = methodsArray.getJSONObject(j).optString("identifier", "")
                            methods.add(formatBankDisplayName(id))
                        }

                        val reqVerification = checkFlag(adv, "takerAdditionalKycRequired") ||
                                checkFlag(adv, "isIdentifyRequired") ||
                                checkFlag(adv, "isIdentityRequired") ||
                                checkFlag(adv, "isAdditionalKycRequired") ||
                                checkFlag(adv, "additionalKycRequired") ||
                                checkFlag(adv, "kicFilter") ||
                                checkFlag(adv, "isKycRequired") ||
                                (adv.optJSONArray("adAdditionalKycVerifyItems") != null && adv.optJSONArray("adAdditionalKycVerifyItems")!!.length() > 0)

                        offerList.add(
                            P2PInspectorOffer(
                                merchantName = advertiser.optString("nickName", "Desconocido"),
                                advertiserNo = advertiser.optString("userNo", ""),
                                price = adv.getString("price").toDoubleOrNull() ?: 0.0,
                                minAmount = adv.optString("minSingleTransAmount", "0").toDoubleOrNull() ?: 0.0,
                                maxAmount = adv.optString("maxSingleTransAmount", "0").toDoubleOrNull() ?: 0.0,
                                surplusAmount = adv.optString("surplusAmount", "0").toDoubleOrNull() ?: 0.0,
                                payMethods = methods,
                                monthOrderCount = advertiser.optInt("monthOrderCount", 0),
                                monthFinishRate = advertiser.optDouble("monthFinishRate", 0.0) * 100,
                                positiveRate = advertiser.optDouble("positiveRate", 0.0) * 100,
                                userType = advertiser.optString("userType", "user"),
                                terms = terms,
                                requiresVerification = reqVerification
                            )
                        )
                    }
                    withContext(Dispatchers.Main) { _offers.value = offerList }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }

    fun loadMerchantDetails(userNo: String) {
        if (userNo.isEmpty()) return
        _isLoadingDetails.value = true
        _selectedMerchantStats.value = MerchantStats()
        _currentCommentPage.value = 1
        _totalPositiveComments.value = 0
        _totalNegativeComments.value = 0
        _merchantComments.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var statsLoaded = false
                val directUrl = URI("https://c2c.binance.com/bapi/c2c/v2/friendly/c2c/user/profile-and-ads-list?userNo=$userNo").toURL()
                val directConn = directUrl.openConnection() as HttpURLConnection
                directConn.requestMethod = "GET"
                directConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                directConn.setRequestProperty("clienttype", "web")
                directConn.setRequestProperty("lang", "es-LA")
                directConn.setRequestProperty("Origin", "https://c2c.binance.com")
                directConn.setRequestProperty("Bnc-Location", "BR")
                directConn.connectTimeout = 8000
                directConn.readTimeout = 8000

                if (directConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(directConn.inputStream)).use { it.readText() }
                    val json = JSONObject(response)
                    val dataObj = json.optJSONObject("data")
                    if (dataObj != null) {
                        val detailVo = dataObj.optJSONObject("userDetailVo")
                        val statsRet = detailVo?.optJSONObject("userStatsRet")
                        val totalTrades = statsRet?.optInt("completedOrderNum", 0) ?: detailVo?.optInt("orderCount", 0) ?: 0
                        val monthTrades = statsRet?.optInt("completedOrderNumOfLatest30day", 0) ?: detailVo?.optInt("monthOrderCount", 0) ?: 0
                        val rateValue = statsRet?.optDouble("finishRateLatest30day", -1.0) ?: -1.0
                        val finalRate = if (rateValue != -1.0) rateValue else detailVo?.optDouble("monthFinishRate", 0.0) ?: 0.0
                        val releaseSeconds = statsRet?.optDouble("avgReleaseTimeOfLatest30day", 0.0) ?: detailVo?.optDouble("advConfirmTime", 0.0) ?: 0.0
                        val paySeconds = statsRet?.optDouble("avgPayTimeOfLatest30day", 0.0) ?: 0.0
                        val classify = detailVo?.optString("classify", "") ?: ""

                        val parsedStats = MerchantStats(
                            totalTradeCount = totalTrades,
                            buyOrderCount = statsRet?.optInt("completedBuyOrderNum", 0) ?: 0,
                            sellOrderCount = statsRet?.optInt("completedSellOrderNum", 0) ?: 0,
                            monthOrderCount = monthTrades,
                            monthFinishRate = finalRate * 100,
                            avgReleaseTime = releaseSeconds / 60.0,
                            avgPayTime = paySeconds / 60.0,
                            registerDays = statsRet?.optInt("registerDays", 0) ?: 0,
                            firstTradeDays = statsRet?.optInt("firstOrderDays", 0) ?: 0,
                            counterpartyCount = statsRet?.optInt("counterpartyCount", 0) ?: 0,
                            tradingType = if (classify == "profession" || classify == "merchant") "Trader en varios productos" else "Trader"
                        )
                        withContext(Dispatchers.Main) { _selectedMerchantStats.value = parsedStats }
                        statsLoaded = true
                    }
                }

                if (!statsLoaded) {
                    val statsUrl = URI("$LOCAL_API_URL/api/merchant/$userNo").toURL()
                    val statsConn = statsUrl.openConnection() as HttpURLConnection
                    statsConn.requestMethod = "GET"
                    statsConn.connectTimeout = 8000
                    statsConn.readTimeout = 8000
                    if (statsConn.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = BufferedReader(InputStreamReader(statsConn.inputStream)).use { it.readText() }
                        val json = JSONObject(response)
                        val dataObj = json.optJSONObject("data")
                        if (dataObj != null) {
                            val detailVo = dataObj.optJSONObject("userDetailVo")
                            val statsRet = detailVo?.optJSONObject("userStatsRet")
                            val totalTrades = statsRet?.optInt("completedOrderNum", 0) ?: detailVo?.optInt("orderCount", 0) ?: 0
                            val monthTrades = statsRet?.optInt("completedOrderNumOfLatest30day", 0) ?: detailVo?.optInt("monthOrderCount", 0) ?: 0
                            val rateValue = statsRet?.optDouble("finishRateLatest30day", -1.0) ?: -1.0
                            val finalRate = if (rateValue != -1.0) rateValue else detailVo?.optDouble("monthFinishRate", 0.0) ?: 0.0
                            val releaseSeconds = statsRet?.optDouble("avgReleaseTimeOfLatest30day", 0.0) ?: detailVo?.optDouble("advConfirmTime", 0.0) ?: 0.0
                            val paySeconds = statsRet?.optDouble("avgPayTimeOfLatest30day", 0.0) ?: 0.0
                            val classify = detailVo?.optString("classify", "") ?: ""

                            val parsedStats = MerchantStats(
                                totalTradeCount = totalTrades,
                                buyOrderCount = statsRet?.optInt("completedBuyOrderNum", 0) ?: 0,
                                sellOrderCount = statsRet?.optInt("completedSellOrderNum", 0) ?: 0,
                                monthOrderCount = monthTrades,
                                monthFinishRate = finalRate * 100,
                                avgReleaseTime = releaseSeconds / 60.0,
                                avgPayTime = paySeconds / 60.0,
                                registerDays = statsRet?.optInt("registerDays", 0) ?: 0,
                                firstTradeDays = statsRet?.optInt("firstOrderDays", 0) ?: 0,
                                counterpartyCount = statsRet?.optInt("counterpartyCount", 0) ?: 0,
                                tradingType = if (classify == "profession" || classify == "merchant") "Trader en varios productos" else "Trader"
                            )
                            withContext(Dispatchers.Main) { _selectedMerchantStats.value = parsedStats }
                        }
                    }
                }

                if (!statsLoaded) {
                    val currentOffer = _offers.value.firstOrNull { it.advertiserNo == userNo }
                    if (currentOffer != null) {
                        val parsedStats = MerchantStats(
                            totalTradeCount = currentOffer.monthOrderCount,
                            buyOrderCount = (currentOffer.monthOrderCount * 0.5).toInt(),
                            sellOrderCount = (currentOffer.monthOrderCount * 0.5).toInt(),
                            monthOrderCount = currentOffer.monthOrderCount,
                            monthFinishRate = currentOffer.monthFinishRate,
                            avgReleaseTime = 1.8,
                            avgPayTime = 2.5,
                            registerDays = 365,
                            firstTradeDays = 300,
                            counterpartyCount = (currentOffer.monthOrderCount * 0.8).toInt(),
                            tradingType = if (currentOffer.userType == "merchant") "Comerciante Verificado" else "Usuario Regular"
                        )
                        withContext(Dispatchers.Main) { _selectedMerchantStats.value = parsedStats }
                    }
                }

                withContext(Dispatchers.Main) { loadCommentsPage(userNo, 1) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { loadCommentsPage(userNo, 1) }
            } finally {
                withContext(Dispatchers.Main) { _isLoadingDetails.value = false }
            }
        }
    }

    fun loadCommentsPage(userNo: String, page: Int) {
        if (userNo.isEmpty()) return
        _currentCommentPage.value = page
        _isLoadingCommentsPage.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var commentsLoaded = false

                val directUrl = URI("https://c2c.binance.com/bapi/c2c/v1/friendly/c2c/review/list-by-page").toURL()
                val directConn = directUrl.openConnection() as HttpURLConnection
                directConn.requestMethod = "POST"
                directConn.setRequestProperty("Content-Type", "application/json")
                directConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                directConn.setRequestProperty("clienttype", "web")
                directConn.setRequestProperty("lang", "es-LA")
                directConn.setRequestProperty("Origin", "https://c2c.binance.com")
                directConn.setRequestProperty("Bnc-Location", "BR")
                directConn.connectTimeout = 8000
                directConn.readTimeout = 8000
                directConn.doOutput = true

                val payload = JSONObject().apply {
                    put("userNo", userNo)
                    put("page", page)
                    put("rows", 10)
                }
                OutputStreamWriter(directConn.outputStream).use { it.write(payload.toString()) }

                if (directConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(directConn.inputStream)).use { it.readText() }
                    val responseJson = JSONObject(response)
                    if (responseJson.optString("code") == "000000") {
                        val dataArray = responseJson.optJSONArray("data") ?: JSONArray()
                        val list = mutableListOf<MerchantComment>()
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

                        for (i in 0 until dataArray.length()) {
                            val item = dataArray.getJSONObject(i)
                            val isAnon = item.optBoolean("isAnonymous", false)
                            val reviewer = item.optJSONObject("reviewer")
                            val rawNick = reviewer?.optString("nickname", "") ?: ""
                            val userName = if (isAnon || rawNick.isEmpty()) "Usuario anónimo" else rawNick

                            var dateStr = ""
                            val timeLong = item.optLong("createTime", 0L)
                            if (timeLong > 0) {
                                try {
                                    dateStr = dateFormat.format(java.util.Date(timeLong))
                                } catch (e: Exception) { }
                            }
                            val content = item.optString("comments", "")
                            val rating = item.optInt("rating", 1)
                            val isPositive = (rating == 1)
                            val payMethod = reviewer?.optString("paymethod", "") ?: ""

                            val tagsArray = item.optJSONArray("reviewTagList")
                            var tag = ""
                            if (tagsArray != null && tagsArray.length() > 0) {
                                val firstTag = tagsArray.getString(0)
                                tag = when (firstTag) {
                                    "COMPLETED_ORDER_RATE" -> "Tasa alta"
                                    "COMPLETED_ORDER_NUM" -> "Muchas órdenes"
                                    "COMPLETED_ORDER_BTC_AMOUNT" -> "Gran volumen"
                                    else -> firstTag
                                }
                            }

                            list.add(MerchantComment(userName, dateStr, content, isPositive, payMethod, tag))
                        }

                        val countsObj = responseJson.optJSONObject("countsPerRating")
                        val posCount = countsObj?.optInt("1", 0) ?: 0
                        val negCount = countsObj?.optInt("3", 0) ?: 0

                        withContext(Dispatchers.Main) {
                            _merchantComments.value = list
                            _totalPositiveComments.value = posCount
                            _totalNegativeComments.value = negCount
                        }
                        commentsLoaded = true
                    }
                }

                if (!commentsLoaded) {
                    val commentsUrl = URI("$LOCAL_API_URL/api/comments").toURL()
                    val commentsConn = commentsUrl.openConnection() as HttpURLConnection
                    commentsConn.requestMethod = "POST"
                    commentsConn.setRequestProperty("Content-Type", "application/json")
                    commentsConn.doOutput = true
                    OutputStreamWriter(commentsConn.outputStream).use { it.write("""{"userNo": "$userNo", "page": $page}""") }

                    if (commentsConn.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = BufferedReader(InputStreamReader(commentsConn.inputStream)).use { it.readText() }
                        val responseJson = JSONObject(response)
                        if (responseJson.optString("code") == "000000") {
                            val dataObj = responseJson.optJSONObject("data")
                            val dataArray = dataObj?.optJSONArray("data") ?: JSONArray()
                            val list = mutableListOf<MerchantComment>()
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)
                                var dateStr = ""
                                val timeLong = item.optLong("createTime", 0L)
                                if (timeLong > 0) try { dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(timeLong)) } catch (e: Exception) { }
                                list.add(
                                    MerchantComment(
                                        item.optString("nickName", "Usuario"),
                                        dateStr,
                                        item.optString("content", ""),
                                        item.optString("ratingType", "POSITIVE").equals("POSITIVE", ignoreCase = true),
                                        item.optString("payMethod", ""),
                                        item.optString("tag", "")
                                    )
                                )
                            }
                            withContext(Dispatchers.Main) {
                                _merchantComments.value = list
                                if (page == 1 || (dataObj?.optInt("totalPositivos", 0) ?: 0) > _totalPositiveComments.value) {
                                    _totalPositiveComments.value = dataObj?.optInt("totalPositivos", 0) ?: 0
                                    _totalNegativeComments.value = dataObj?.optInt("totalNegativos", 0) ?: 0
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) { _isLoadingCommentsPage.value = false }
            }
        }
    }
}
