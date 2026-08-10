package com.example.cambialoactualizado

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

// --- COLORES PERSONALIZADOS Y CONSTANTES ---
val FondoOscuro = Color(0xFF161A1E)
val BotonLila = Color(0xFFD0BCFF)
val TextoBlanco = Color.White
val TextoGris = Color(0xFF848E9C)
val BinanceYellow = Color(0xFFF0B90B)
val BinanceGreen = Color(0xFF0ECB81)
val BinanceRed = Color(0xFFF6465D)

const val NUMERO_WHATSAPP = "573337337769"
const val LOCAL_API_URL = "http://158.247.123.136:3000"

// --- MODELOS DE DATOS ---
data class OfertaP2P(val precio: Double, val minimo: Double)
data class MarketRates(
    val compraPesos: Double = 0.0, val ventaPesos: Double = 0.0,
    val compraBolivar: Double = 0.0, val ventaBolivar: Double = 0.0,
    val bcv: Double = 0.0
)

data class P2PInspectorOffer(
    val merchantName: String, val advertiserNo: String, val price: Double,
    val minAmount: Double, val maxAmount: Double, val surplusAmount: Double,
    val payMethods: List<String>, val monthOrderCount: Int, val monthFinishRate: Double,
    val positiveRate: Double, val userType: String, val terms: String,
    val requiresVerification: Boolean = false
)

data class MerchantStats(
    val totalTradeCount: Int = 0, val buyOrderCount: Int = 0, val sellOrderCount: Int = 0,
    val monthOrderCount: Int = 0, val monthFinishRate: Double = 0.0, val avgReleaseTime: Double = 0.0,
    val avgPayTime: Double = 0.0, val registerDays: Int = 0, val firstTradeDays: Int = 0,
    val counterpartyCount: Int = 0, val tradingType: String = "Trader"
)

data class MerchantComment(
    val userName: String, val date: String, val content: String,
    val isPositive: Boolean, val payMethod: String = "", val tag: String = ""
)

data class P2PFavorite(
    val id: String, val name: String, val fiat: String, val tradeType: String,
    val amount: String, val isVerified: Boolean, val banks: List<String>
)

val fiatDetails = mapOf(
    "COP" to Pair("🇨🇴", "Colombia"), "VES" to Pair("🇻🇪", "Venezuela"), "BRL" to Pair("🇧🇷", "Brasil"),
    "ARS" to Pair("🇦🇷", "Argentina"), "USD" to Pair("🇺🇸", "Estados Unidos"), "EUR" to Pair("🇪🇺", "Eurozona"),
    "MXN" to Pair("🇲🇽", "México"), "PEN" to Pair("🇵🇪", "Perú"), "CLP" to Pair("🇨🇱", "Chile")
)
val allFiatCurrencies = fiatDetails.keys.toList().sorted()

val bankOptionsByFiat = mapOf(
    "COP" to listOf("BancolombiaSA", "Nequi", "Daviplata", "DaviviendaSA", "Llaves_Bre_B", "BBVA", "Banco_Caja_Social", "Banco_Falabella", "Banco_Itau_Colombia", "Banco_Popular", "Banco_de_Bogota", "DAVIBank", "Efecty", "Global66", "Movii", "Powwi", "Trans_Bank", "Uala_Colombia"),
    "VES" to listOf("PagoMovil", "Banesco", "Mercantil", "Provincial", "Bancamiga", "BBVA_Provincial", "Banco_de_Venezuela", "BNC", "BOD")
)

// --- FUNCIONES PARA FAVORITOS ---
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
            list.add(P2PFavorite(obj.getString("id"), obj.getString("name"), obj.getString("fiat"), obj.getString("tradeType"), obj.getString("amount"), obj.getBoolean("isVerified"), banks))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun saveFavorites(prefs: SharedPreferences, list: List<P2PFavorite>) {
    val arr = JSONArray()
    list.forEach { fav ->
        val obj = JSONObject()
        obj.put("id", fav.id); obj.put("name", fav.name); obj.put("fiat", fav.fiat); obj.put("tradeType", fav.tradeType); obj.put("amount", fav.amount); obj.put("isVerified", fav.isVerified)
        val banksArr = JSONArray()
        fav.banks.forEach { banksArr.put(it) }
        obj.put("banks", banksArr)
        arr.put(obj)
    }
    prefs.edit().putString("p2p_favs", arr.toString()).apply()
}

// --- FORMATO CON PUNTOS ---
class ThousandSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val parts = originalText.split('.')
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1] else null
        val formattedInt = intPart.reversed().chunked(3).joinToString(".").reversed()
        val formattedText = if (decPart != null) "$formattedInt,$decPart" else if (originalText.endsWith(".")) "$formattedInt," else formattedInt

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = 0; var originalOffset = 0
                for (i in formattedText.indices) {
                    if (originalOffset == offset) break
                    if (formattedText[i] != '.') originalOffset++
                    transformedOffset++
                }
                return minOf(transformedOffset, formattedText.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = 0
                for (i in 0 until minOf(offset, formattedText.length)) {
                    if (formattedText[i] != '.') originalOffset++
                }
                return minOf(originalOffset, originalText.length)
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

// --- ESTADOS DE NAVEGACIÓN ---
sealed class Screen {
    object Loading : Screen()
    object Error : Screen()
    object Menu : Screen()
    object P2PInspector : Screen()

    object PesosBolivaresMenu : Screen()
    object PesosABolivares : Screen()
    object BolivaresAPesos : Screen()

    object BolivaresPesosMenu : Screen()
    object VenColBolivaresAPesos : Screen()
    object VenColPesosABolivares : Screen()

    object CombinedMenu : Screen()
    object PesosAUsdt : Screen()
    object BolivaresAUsdt : Screen()
    object UsdtAPesos : Screen()
    object UsdtABolivares : Screen()
}

// --- VIEWMODELS ---
class ExchangeViewModel : ViewModel() {
    private val _rates = MutableStateFlow(MarketRates())
    val rates: StateFlow<MarketRates> = _rates

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Loading)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _gananciaPB = MutableStateFlow(9.0)
    val gananciaPB: StateFlow<Double> = _gananciaPB

    private val _gananciaBP = MutableStateFlow(12.0)
    val gananciaBP: StateFlow<Double> = _gananciaBP

    private val _gananciaVenCol = MutableStateFlow(25.0)
    val gananciaVenCol: StateFlow<Double> = _gananciaVenCol

    init {
        viewModelScope.launch(Dispatchers.IO) {
            actualizarTasasSilenciosamente()
            withContext(Dispatchers.Main) {
                if (_rates.value.compraPesos > 0.0) _currentScreen.value = Screen.Menu else _currentScreen.value = Screen.Error
            }
            while(true) {
                delay(5000)
                actualizarTasasSilenciosamente()
            }
        }
    }

    fun updateGananciaPB(nuevoValor: Double) { _gananciaPB.value = nuevoValor }
    fun updateGananciaBP(nuevoValor: Double) { _gananciaBP.value = nuevoValor }
    fun updateGananciaVenCol(nuevoValor: Double) { _gananciaVenCol.value = nuevoValor }

    private suspend fun actualizarTasasSilenciosamente() {
        val bancosColombia = listOf("BancolombiaSA", "Nequi")
        val bancosVenezuela = listOf("Bancamiga", "PagoMovil")

        val compraPesos = obtenerMejorPrecioP2P("COP", "BUY", bancosColombia, "8400")?.precio ?: 0.0
        val ventaPesos = obtenerMejorPrecioP2P("COP", "SELL", bancosColombia, "8400")?.precio ?: 0.0
        val compraBolivar = obtenerMejorPrecioP2P("VES", "BUY", bancosVenezuela, "220")?.precio ?: 0.0
        val ventaBolivar = obtenerMejorPrecioP2P("VES", "SELL", bancosVenezuela, "220")?.precio ?: 0.0
        val tasaBcv = obtenerTasaBCV()

        withContext(Dispatchers.Main) {
            if (compraPesos > 0.0 && ventaPesos > 0.0 && compraBolivar > 0.0 && ventaBolivar > 0.0) {
                _rates.value = MarketRates(
                    compraPesos = compraPesos, ventaPesos = ventaPesos,
                    compraBolivar = compraBolivar, ventaBolivar = ventaBolivar,
                    bcv = if (tasaBcv > 0.0) tasaBcv else _rates.value.bcv
                )
            }
        }
    }

    fun fetchRatesAndNavigate(targetScreen: Screen) {
        _currentScreen.value = Screen.Loading
        viewModelScope.launch(Dispatchers.IO) {
            actualizarTasasSilenciosamente()
            withContext(Dispatchers.Main) {
                if (_rates.value.compraPesos == 0.0) _currentScreen.value = Screen.Error else _currentScreen.value = targetScreen
            }
        }
    }

    fun navigateTo(screen: Screen) { _currentScreen.value = screen }

    private fun obtenerMejorPrecioP2P(fiat: String, tradeType: String, bancos: List<String>, montoBuscado: String): OfertaP2P? {
        return try {
            val url = URI("https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 6000
            connection.readTimeout = 6000
            connection.doOutput = true

            val filtroPago = if (bancos.isNotEmpty()) bancos.joinToString(prefix = "[", postfix = "]", separator = ", ") { "\"$it\"" } else "[]"
            val filtroMonto = if (montoBuscado.isNotEmpty()) """"transAmount": "$montoBuscado",""" else ""

            val jsonInputString = """{"asset": "USDT", "fiat": "$fiat", "tradeType": "$tradeType", "payTypes": $filtroPago, $filtroMonto "countries": [], "page": 1, "rows": 1, "publisherType": "merchant", "proMerchantAds": false, "shieldMerchantAds": false, "filterType": "all"}"""
            OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val precio = """"price":"([\d.]+)"""".toRegex().find(response)?.groups?.get(1)?.value?.toDoubleOrNull()
                val minimo = """"minSingleTransAmount":"([\d.]+)"""".toRegex().find(response)?.groups?.get(1)?.value?.toDoubleOrNull()
                if (precio != null && minimo != null) OfertaP2P(precio, minimo) else null
            } else null
        } catch (e: Exception) { null }
    }

    private fun obtenerTasaBCV(): Double {
        var tasa = 0.0
        try {
            val url = URI("$LOCAL_API_URL/api/bcv").toURL()
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(response)
                if (json.optString("code") == "000000") tasa = json.optDouble("tasa", 0.0)
            }
        } catch (e: Exception) { e.printStackTrace() }
        return tasa
    }
}

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
        _isLoading.value = true; _offers.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URI("https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search").toURL()
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("clienttype", "web")
                connection.setRequestProperty("lang", "es")
                connection.setRequestProperty("Origin", "https://p2p.binance.com")
                connection.doOutput = true

                val jsonObject = JSONObject().apply {
                    put("asset", "USDT"); put("fiat", fiat); put("tradeType", tradeType)
                    val payTypesArray = JSONArray()
                    selectedBanks.forEach { payTypesArray.put(it) }
                    put("payTypes", payTypesArray); put("countries", JSONArray())
                    put("page", 1); put("rows", 5)
                    if (isVerifiedOnly) put("publisherType", "merchant") else put("publisherType", JSONObject.NULL)
                    if (amount.isNotEmpty()) put("transAmount", amount)
                    put("proMerchantAds", false); put("shieldMerchantAds", false); put("filterType", "all")
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
                        for (j in 0 until methodsArray.length()) methods.add(methodsArray.getJSONObject(j).optString("identifier", ""))

                        val reqVerification = checkFlag(adv, "isIdentifyRequired") || checkFlag(adv, "isIdentityRequired") || checkFlag(adv, "isAdditionalKycRequired") || checkFlag(adv, "additionalKycRequired") || checkFlag(adv, "kicFilter") || checkFlag(adv, "isKycRequired")

                        offerList.add(
                            P2PInspectorOffer(
                                merchantName = advertiser.optString("nickName", "Desconocido"), advertiserNo = advertiser.optString("userNo", ""),
                                price = adv.getString("price").toDoubleOrNull() ?: 0.0, minAmount = adv.optString("minSingleTransAmount", "0").toDoubleOrNull() ?: 0.0,
                                maxAmount = adv.optString("maxSingleTransAmount", "0").toDoubleOrNull() ?: 0.0, surplusAmount = adv.optString("surplusAmount", "0").toDoubleOrNull() ?: 0.0,
                                payMethods = methods, monthOrderCount = advertiser.optInt("monthOrderCount", 0), monthFinishRate = advertiser.optDouble("monthFinishRate", 0.0) * 100,
                                positiveRate = advertiser.optDouble("positiveRate", 0.0) * 100, userType = advertiser.optString("userType", "user"), terms = terms,
                                requiresVerification = reqVerification
                            )
                        )
                    }
                    withContext(Dispatchers.Main) { _offers.value = offerList }
                }
            } catch (e: Exception) { e.printStackTrace() } finally { withContext(Dispatchers.Main) { _isLoading.value = false } }
        }
    }

    fun loadMerchantDetails(userNo: String) {
        if (userNo.isEmpty()) return
        _isLoadingDetails.value = true; _selectedMerchantStats.value = MerchantStats(); _currentCommentPage.value = 1; _totalPositiveComments.value = 0; _totalNegativeComments.value = 0; _merchantComments.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val statsUrl = URI("$LOCAL_API_URL/api/merchant/$userNo").toURL()
                val statsConn = statsUrl.openConnection() as HttpURLConnection
                statsConn.requestMethod = "GET"; statsConn.connectTimeout = 8000; statsConn.readTimeout = 8000
                if (statsConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(statsConn.inputStream)).use { it.readText() }
                    val json = JSONObject(response)
                    val dataObj = json.optJSONObject("data")
                    if (dataObj != null) {
                        val detailVo = dataObj.optJSONObject("userDetailVo")
                        val statsRet = detailVo?.optJSONObject("userStatsRet")
                        val totalTrades = statsRet?.optInt("completedOrderNum", 0) ?: detailVo?.optInt("orderCount", 0) ?: 0
                        val monthTrades = statsRet?.optInt("completedOrderNumOfLatest30day", 0) ?: detailVo?.optInt("monthOrderCount", 0) ?: 0
                        val rateValue = statsRet?.optString("finishRateLatest30day")?.toDoubleOrNull()
                        val finalRate = if (rateValue != null && rateValue != -1.0) rateValue else detailVo?.optString("monthFinishRate")?.toDoubleOrNull() ?: 0.0
                        var releaseSeconds = statsRet?.optString("avgReleaseTimeOfLatest30day")?.toDoubleOrNull() ?: detailVo?.optString("advConfirmTime")?.toDoubleOrNull() ?: 0.0
                        var paySeconds = statsRet?.optString("avgPayTimeOfLatest30day")?.toDoubleOrNull() ?: detailVo?.optString("advPayTime")?.toDoubleOrNull() ?: 0.0
                        val classify = detailVo?.optString("classify", "") ?: ""

                        val parsedStats = MerchantStats(
                            totalTradeCount = totalTrades, buyOrderCount = statsRet?.optInt("completedBuyOrderNum", 0) ?: 0, sellOrderCount = statsRet?.optInt("completedSellOrderNum", 0) ?: 0,
                            monthOrderCount = monthTrades, monthFinishRate = finalRate * 100, avgReleaseTime = releaseSeconds / 60.0, avgPayTime = paySeconds / 60.0,
                            registerDays = statsRet?.optInt("registerDays", 0) ?: 0, firstTradeDays = statsRet?.optInt("firstOrderDays", 0) ?: 0,
                            counterpartyCount = statsRet?.optInt("counterpartyCount", 0) ?: 0, tradingType = if (classify == "profession" || classify == "merchant") "Trader en varios productos" else "Trader"
                        )
                        withContext(Dispatchers.Main) { _selectedMerchantStats.value = parsedStats }
                    }
                }
                withContext(Dispatchers.Main) { loadCommentsPage(userNo, 1) }
            } catch (e: Exception) { e.printStackTrace() } finally { withContext(Dispatchers.Main) { _isLoadingDetails.value = false } }
        }
    }

    fun loadCommentsPage(userNo: String, page: Int) {
        if (userNo.isEmpty()) return
        _currentCommentPage.value = page; _isLoadingCommentsPage.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val commentsUrl = URI("$LOCAL_API_URL/api/comments").toURL()
                val commentsConn = commentsUrl.openConnection() as HttpURLConnection
                commentsConn.requestMethod = "POST"; commentsConn.setRequestProperty("Content-Type", "application/json"); commentsConn.doOutput = true
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
                            list.add(MerchantComment(item.optString("nickName", "Usuario"), dateStr, item.optString("content", ""), item.optString("ratingType", "POSITIVE").equals("POSITIVE", ignoreCase = true), item.optString("payMethod", ""), item.optString("tag", "")))
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
            } catch (e: Exception) { e.printStackTrace() } finally { withContext(Dispatchers.Main) { _isLoadingCommentsPage.value = false } }
        }
    }
}

// --- ACTIVITY PRINCIPAL ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#161A1E")))
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#161A1E"))
        super.onCreate(savedInstanceState)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = FondoOscuro) { AppNavigation() }
            }
        }
    }
}

// --- NAVEGADOR ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(viewModel: ExchangeViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val rates by viewModel.rates.collectAsState()
    val gananciaPB by viewModel.gananciaPB.collectAsState()
    val gananciaBP by viewModel.gananciaBP.collectAsState()
    val gananciaVenCol by viewModel.gananciaVenCol.collectAsState()
    val context = LocalContext.current

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == Screen.Loading) fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            else slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn() togetherWith slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut()
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.Loading -> LoadingScreen()
            Screen.Error -> ErrorScreen(onRetry = { viewModel.fetchRatesAndNavigate(Screen.Menu) })
            Screen.Menu -> MainMenu(rates = rates, onNavigate = { viewModel.fetchRatesAndNavigate(it) }, onExit = { (context as? Activity)?.finish() })
            Screen.P2PInspector -> P2PInspectorScreen { viewModel.navigateTo(Screen.Menu) }

            // Menú 2: Pesos / Bolívares
            Screen.PesosBolivaresMenu -> PesosBolivaresMenuScreen { viewModel.navigateTo(it) }
            Screen.PesosABolivares -> PesosToBolivaresScreen(rates, gananciaPB, { viewModel.updateGananciaPB(it) }) { viewModel.navigateTo(Screen.PesosBolivaresMenu) }
            Screen.BolivaresAPesos -> BolivaresToPesosScreen(rates, gananciaBP, { viewModel.updateGananciaBP(it) }) { viewModel.navigateTo(Screen.PesosBolivaresMenu) }

            // Menú 3: Bolívares / Pesos
            Screen.BolivaresPesosMenu -> BolivaresPesosMenuScreen { viewModel.navigateTo(it) }
            Screen.VenColBolivaresAPesos -> VenColBolivaresAPesosScreen(rates, gananciaVenCol, { viewModel.updateGananciaVenCol(it) }) { viewModel.navigateTo(Screen.BolivaresPesosMenu) }
            Screen.VenColPesosABolivares -> VenColPesosABolivaresScreen(rates, gananciaVenCol, { viewModel.updateGananciaVenCol(it) }) { viewModel.navigateTo(Screen.BolivaresPesosMenu) }

            // Menú 4: USDT
            Screen.CombinedMenu -> CombinedMenuScreen { viewModel.navigateTo(it) }
            Screen.PesosAUsdt -> PesosToUsdtScreen(rates) { viewModel.navigateTo(Screen.CombinedMenu) }
            Screen.BolivaresAUsdt -> BolivaresToUsdtScreen(rates) { viewModel.navigateTo(Screen.CombinedMenu) }
            Screen.UsdtAPesos -> UsdtToPesosScreen(rates) { viewModel.navigateTo(Screen.CombinedMenu) }
            Screen.UsdtABolivares -> UsdtToBolivaresScreen(rates) { viewModel.navigateTo(Screen.CombinedMenu) }
        }
    }
}

fun abrirWhatsAppConComprobante(context: Context, numero: String, mensaje: String, imageUri: Uri?) {
    if (imageUri != null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"; putExtra(Intent.EXTRA_STREAM, imageUri); putExtra(Intent.EXTRA_TEXT, mensaje)
            putExtra("jid", "$numero@s.whatsapp.net"); setPackage("com.whatsapp"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show() }
    } else {
        val url = "https://api.whatsapp.com/send?phone=$numero&text=${Uri.encode(mensaje)}"
        val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
        try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show() }
    }
}

// --- PANTALLA: MENÚ PRINCIPAL ---
@Composable
fun MainMenu(rates: MarketRates, onNavigate: (Screen) -> Unit, onExit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {
        if (rates.compraPesos > 0.0 && rates.compraBolivar > 0.0) {
            AnimatedVisibility(visible = true, enter = fadeIn(tween(1000)), modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 32.dp, start = 16.dp, end = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("📊 MERCADO Y BCV", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Compra COP: ${rates.compraPesos}", color = TextoGris, fontSize = 10.sp)
                        Text("Venta COP: ${rates.ventaPesos}", color = TextoGris, fontSize = 10.sp)
                        Text("Compra VES: ${rates.compraBolivar}", color = TextoGris, fontSize = 10.sp)
                        Text("Venta VES: ${rates.ventaBolivar}", color = TextoGris, fontSize = 10.sp)
                        Text("Tasa BCV: ${rates.bcv}", color = TextoGris, fontSize = 10.sp)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 100.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "title_animation")
            val rotation by infiniteTransition.animateFloat(initialValue = -15f, targetValue = 15f, animationSpec = infiniteRepeatable(animation = tween(800, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "rotation")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💱", fontSize = 28.sp, modifier = Modifier.rotate(rotation))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ELIGE EL TIPO DE\nOPERACIÓN", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextoBlanco, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(8.dp))
                Text("💱", fontSize = 28.sp, modifier = Modifier.rotate(-rotation))
            }
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedMenuButton("🔍 1. CONSULTAR TASAS P2P", 100) { onNavigate(Screen.P2PInspector) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("💱 2. PESOS / BOLÍVARES", 200) { onNavigate(Screen.PesosBolivaresMenu) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("💱 3. BOLÍVARES / PESOS", 300) { onNavigate(Screen.BolivaresPesosMenu) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🪙 4. USDT (Restantes)", 400) { onNavigate(Screen.CombinedMenu) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🚪 5. SALIR DEL PROGRAMA 👋", 500) { onExit() }
        }
    }
}

// --- SUBMENÚS ---
@Composable
fun PesosBolivaresMenuScreen(onNavigate: (Screen) -> Unit) {
    BackHandler { onNavigate(Screen.Menu) }
    Box(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("💱", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Pesos / Bolívares", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextoBlanco, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedMenuButton("🇨🇴 1. ¿CUÁNTOS PESOS TIENES? 💵", 100) { onNavigate(Screen.PesosABolivares) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🇻🇪 2. ¿CUÁNTOS BOLÍVARES QUIERES? 💰", 200) { onNavigate(Screen.BolivaresAPesos) }
            Spacer(modifier = Modifier.height(48.dp))

            Button(onClick = { onNavigate(Screen.Menu) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White), shape = CircleShape, modifier = Modifier.fillMaxWidth(0.5f).height(50.dp)) { Text("🔙 Volver al Menú") }
        }
    }
}

@Composable
fun BolivaresPesosMenuScreen(onNavigate: (Screen) -> Unit) {
    BackHandler { onNavigate(Screen.Menu) }
    Box(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("💱", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bolívares / Pesos", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextoBlanco, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedMenuButton("🇻🇪 1. ¿CUÁNTOS BOLÍVARES TIENES? 💰", 100) { onNavigate(Screen.VenColBolivaresAPesos) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🇨🇴 2. ¿CUÁNTOS PESOS QUIERES? 💵", 200) { onNavigate(Screen.VenColPesosABolivares) }
            Spacer(modifier = Modifier.height(48.dp))

            Button(onClick = { onNavigate(Screen.Menu) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White), shape = CircleShape, modifier = Modifier.fillMaxWidth(0.5f).height(50.dp)) { Text("🔙 Volver al Menú") }
        }
    }
}

@Composable
fun CombinedMenuScreen(onNavigate: (Screen) -> Unit) {
    BackHandler { onNavigate(Screen.Menu) }
    Box(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("🪙", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Operaciones\ncon USDT", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextoBlanco, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedMenuButton("🇨🇴 1. DE PESOS A USDT 🪙", 100) { onNavigate(Screen.PesosAUsdt) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🇻🇪 2. DE BOLÍVARES A USDT 🪙", 200) { onNavigate(Screen.BolivaresAUsdt) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🪙 3. DE USDT A PESOS 🇨🇴", 300) { onNavigate(Screen.UsdtAPesos) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🪙 4. DE USDT A BOLÍVARES 🇻🇪", 400) { onNavigate(Screen.UsdtABolivares) }
            Spacer(modifier = Modifier.height(48.dp))

            Button(onClick = { onNavigate(Screen.Menu) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White), shape = CircleShape, modifier = Modifier.fillMaxWidth(0.5f).height(50.dp)) { Text("🔙 Volver al Menú") }
        }
    }
}

// --- PANTALLAS: PESOS A BOLIVARES (Colombia -> Venezuela) ---
@Composable
fun PesosToBolivaresScreen(rates: MarketRates, ganancia: Double, onUpdateGanancia: (Double) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputMargin by remember { mutableStateOf(ganancia.roundToInt().toString()) }
    var pesosInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var enviarMensaje by remember { mutableStateOf("") }
    var recibirMensaje by remember { mutableStateOf("") }
    var profitCop by remember { mutableStateOf(0.0) }
    var profitUsdt by remember { mutableStateOf(0.0) }

    // FÓRMULA EXACTA DEL EXCEL (COL -> VEN): Venta Pesos / Venta Bolívares
    val tasaNeta = if (rates.ventaBolivar > 0) rates.ventaPesos / rates.ventaBolivar else 0.0
    val tasaDelDia = tasaNeta + (tasaNeta * (ganancia / 100.0))
    val dfCop = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMAN))
    val dfBs = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.GERMAN))

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.Start) {
                Text("⚙️ Ajustar Margen", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(55.dp).height(40.dp).background(Color(0xFF0B0E11), RoundedCornerShape(8.dp)).border(1.dp, TextoGris, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        BasicTextField(value = inputMargin, onValueChange = { newVal -> val filtered = newVal.filter { it.isDigit() }; if (filtered.length <= 3) { inputMargin = filtered; filtered.toDoubleOrNull()?.let { v -> onUpdateGanancia(v) } } }, textStyle = TextStyle(color = TextoBlanco, fontSize = 16.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%", color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            SingleRateInfoCard("🇨🇴 ➔ 🇻🇪", tasaNeta, tasaDelDia, Modifier.width(130.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        CalcRow("🇨🇴 Pesos a enviar", pesosInput, { pesosInput = it; resultText = ""; noteText = ""; profitCop = 0.0; profitUsdt = 0.0 }) {
            val amount = pesosInput.toDoubleOrNull() ?: 0.0
            if (amount > 0 && tasaDelDia > 0) {
                val bolivares = amount / tasaDelDia
                resultText = "✅ Recibe Bs ${dfBs.format(bolivares)}"
                enviarMensaje = "${amount.roundToInt()} COP"; recibirMensaje = "Bs ${"%.2f".format(bolivares)}"
                val usdBolivares = if (rates.bcv > 0) bolivares / rates.bcv else 0.0
                noteText = "💡 Equivalen a $${dfBs.format(usdBolivares)} al BCV ⚖️"

                profitCop = amount * (ganancia / 100.0)
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedResult(resultText, noteText)

        AnimatedVisibility(visible = profitCop > 0) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329).copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤑 Ganancia Neta de la Operación", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${dfCop.format(profitCop.roundToInt())} COP  ≈  $${dfBs.format(profitUsdt)} USDT", color = Color(0xFF81C995), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        PaymentSection("PESOS", resultText.isNotEmpty(), enviarMensaje, recibirMensaje)
    }
}

@Composable
fun BolivaresToPesosScreen(rates: MarketRates, ganancia: Double, onUpdateGanancia: (Double) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputMargin by remember { mutableStateOf(ganancia.roundToInt().toString()) }
    var bolivaresInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var enviarMensaje by remember { mutableStateOf("") }
    var recibirMensaje by remember { mutableStateOf("") }
    var profitCop by remember { mutableStateOf(0.0) }
    var profitUsdt by remember { mutableStateOf(0.0) }

    // FÓRMULA EXACTA DEL VIDEO (COL -> VEN): Venta Pesos / Venta Bolívares
    val tasaNeta = if (rates.ventaBolivar > 0) rates.ventaPesos / rates.ventaBolivar else 0.0
    val tasaDelDia = tasaNeta + (tasaNeta * (ganancia / 100.0))
    val dfCop = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMAN))
    val dfBs = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.GERMAN))

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.Start) {
                Text("⚙️ Ajustar Margen", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(55.dp).height(40.dp).background(Color(0xFF0B0E11), RoundedCornerShape(8.dp)).border(1.dp, TextoGris, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        BasicTextField(value = inputMargin, onValueChange = { newVal -> val filtered = newVal.filter { it.isDigit() }; if (filtered.length <= 3) { inputMargin = filtered; filtered.toDoubleOrNull()?.let { v -> onUpdateGanancia(v) } } }, textStyle = TextStyle(color = TextoBlanco, fontSize = 16.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%", color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            SingleRateInfoCard("🇨🇴 ➔ 🇻🇪", tasaNeta, tasaDelDia, Modifier.width(130.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        CalcRow("🇻🇪 Bolívares a recibir", bolivaresInput, { bolivaresInput = it; resultText = ""; noteText = ""; profitCop = 0.0; profitUsdt = 0.0 }) {
            val amount = bolivaresInput.toDoubleOrNull() ?: 0.0
            if (amount > 0 && tasaDelDia > 0) {
                val pesos = (amount * tasaDelDia).roundToInt()
                resultText = "✅ Debe enviar ${dfCop.format(pesos)} COP"
                enviarMensaje = "${pesos} COP"; recibirMensaje = "Bs ${amount}"
                val usdBolivares = if (rates.bcv > 0) amount / rates.bcv else 0.0
                noteText = "💡 Equivalen a $${dfBs.format(usdBolivares)} al BCV ⚖️"

                profitCop = pesos.toDouble() * (ganancia / 100.0)
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedResult(resultText, noteText)

        AnimatedVisibility(visible = profitCop > 0) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329).copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤑 Ganancia Neta de la Operación", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${dfCop.format(profitCop.roundToInt())} COP  ≈  $${dfBs.format(profitUsdt)} USDT", color = Color(0xFF81C995), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        PaymentSection("PESOS", resultText.isNotEmpty(), enviarMensaje, recibirMensaje)
    }
}

// --- PANTALLAS: BOLIVARES A PESOS (Venezuela -> Colombia) ---
@Composable
fun VenColBolivaresAPesosScreen(rates: MarketRates, ganancia: Double, onUpdateGanancia: (Double) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputMargin by remember { mutableStateOf(ganancia.roundToInt().toString()) }
    var bolivaresInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var enviarMensaje by remember { mutableStateOf("") }
    var recibirMensaje by remember { mutableStateOf("") }
    var profitCop by remember { mutableStateOf(0.0) }
    var profitUsdt by remember { mutableStateOf(0.0) }

    // FÓRMULA EXACTA DEL VIDEO (VEN -> COL): Compra Bolívares / Venta Pesos
    val tasaNeta = if (rates.ventaPesos > 0) rates.compraBolivar / rates.ventaPesos else 0.0
    val tasaDelDia = tasaNeta * (1 + (ganancia / 100.0))
    val dfCop = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMAN))
    val dfBs = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.GERMAN))

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.Start) {
                Text("⚙️ Ajustar Margen", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(55.dp).height(40.dp).background(Color(0xFF0B0E11), RoundedCornerShape(8.dp)).border(1.dp, TextoGris, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        BasicTextField(value = inputMargin, onValueChange = { newVal -> val filtered = newVal.filter { it.isDigit() }; if (filtered.length <= 3) { inputMargin = filtered; filtered.toDoubleOrNull()?.let { v -> onUpdateGanancia(v) } } }, textStyle = TextStyle(color = TextoBlanco, fontSize = 16.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%", color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            SingleRateInfoCard("🇻🇪 ➔ 🇨🇴", tasaNeta, tasaDelDia, Modifier.width(130.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        CalcRow("🇻🇪 Bolívares a enviar", bolivaresInput, { bolivaresInput = it; resultText = ""; noteText = ""; profitCop = 0.0; profitUsdt = 0.0 }) {
            val amount = bolivaresInput.toDoubleOrNull() ?: 0.0
            if (amount > 0 && tasaDelDia > 0) {
                val pesos = (amount / tasaDelDia).roundToInt()
                resultText = "✅ Recibe ${dfCop.format(pesos)} COP"
                enviarMensaje = "Bs ${amount}"; recibirMensaje = "${pesos} COP"
                val usdBolivares = if (rates.bcv > 0) amount / rates.bcv else 0.0
                noteText = "💡 Equivalen a $${dfBs.format(usdBolivares)} al BCV ⚖️"

                profitCop = pesos.toDouble() * (ganancia / 100.0)
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedResult(resultText, noteText)

        AnimatedVisibility(visible = profitCop > 0) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329).copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤑 Ganancia Neta de la Operación", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${dfCop.format(profitCop.roundToInt())} COP  ≈  $${dfBs.format(profitUsdt)} USDT", color = Color(0xFF81C995), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        PaymentSection("BOLIVARES", resultText.isNotEmpty(), enviarMensaje, recibirMensaje)
    }
}

@Composable
fun VenColPesosABolivaresScreen(rates: MarketRates, ganancia: Double, onUpdateGanancia: (Double) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputMargin by remember { mutableStateOf(ganancia.roundToInt().toString()) }
    var pesosInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var enviarMensaje by remember { mutableStateOf("") }
    var recibirMensaje by remember { mutableStateOf("") }
    var profitCop by remember { mutableStateOf(0.0) }
    var profitUsdt by remember { mutableStateOf(0.0) }

    // FÓRMULA EXACTA DEL VIDEO (VEN -> COL): Compra Bolívares / Venta Pesos
    val tasaNeta = if (rates.ventaPesos > 0) rates.compraBolivar / rates.ventaPesos else 0.0
    val tasaDelDia = tasaNeta * (1 + (ganancia / 100.0))
    val dfCop = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMAN))
    val dfBs = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.GERMAN))

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.Start) {
                Text("⚙️ Ajustar Margen", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(55.dp).height(40.dp).background(Color(0xFF0B0E11), RoundedCornerShape(8.dp)).border(1.dp, TextoGris, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        BasicTextField(value = inputMargin, onValueChange = { newVal -> val filtered = newVal.filter { it.isDigit() }; if (filtered.length <= 3) { inputMargin = filtered; filtered.toDoubleOrNull()?.let { v -> onUpdateGanancia(v) } } }, textStyle = TextStyle(color = TextoBlanco, fontSize = 16.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%", color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            SingleRateInfoCard("🇻🇪 ➔ 🇨🇴", tasaNeta, tasaDelDia, Modifier.width(130.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        CalcRow("🇨🇴 Pesos a recibir", pesosInput, { pesosInput = it; resultText = ""; noteText = ""; profitCop = 0.0; profitUsdt = 0.0 }) {
            val amount = pesosInput.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val bolivares = amount * tasaDelDia
                resultText = "✅ Debe enviar Bs ${dfBs.format(bolivares)}"
                enviarMensaje = "Bs ${"%.2f".format(bolivares)}"; recibirMensaje = "${amount.roundToInt()} COP"
                val usdBolivares = if (rates.bcv > 0) bolivares / rates.bcv else 0.0
                noteText = "💡 Equivalen a $${dfBs.format(usdBolivares)} al BCV ⚖️"

                profitCop = amount * (ganancia / 100.0)
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedResult(resultText, noteText)

        AnimatedVisibility(visible = profitCop > 0) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329).copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤑 Ganancia Neta de la Operación", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${dfCop.format(profitCop.roundToInt())} COP  ≈  $${dfBs.format(profitUsdt)} USDT", color = Color(0xFF81C995), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        PaymentSection("BOLIVARES", resultText.isNotEmpty(), enviarMensaje, recibirMensaje)
    }
}

// --- PANTALLAS MENORES USDT ---
@Composable
fun PesosToUsdtScreen(rates: MarketRates, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputAmount by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        CalcRow("🇨🇴 ¿Cuántos Pesos tienes? 🪙", inputAmount, { inputAmount = it; resultText = ""; noteText = "" }) {
            val amount = inputAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0 && rates.compraPesos > 0) {
                val rawTotal = amount / rates.compraPesos; val rawUsdt = floor(rawTotal * 100) / 100.0; val fee = 0.07; var finalUsdt = rawUsdt - fee; if(finalUsdt < 0) finalUsdt = 0.0
                val dfUsdt = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
                resultText = "💸 Recibirá Netos:\n${dfUsdt.format(finalUsdt)} USDT"
                noteText = "💡 Desglose de Binance:\nTotal Bruto: ${dfUsdt.format(rawUsdt)} USDT\nComisión: -${dfUsdt.format(fee)} USDT"
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        AnimatedResult(resultText, noteText)
    }
}

@Composable
fun BolivaresToUsdtScreen(rates: MarketRates, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputAmount by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        CalcRow("🇻🇪 ¿Cuántos Bolívares tienes? 🪙", inputAmount, { inputAmount = it; resultText = ""; noteText = "" }) {
            val amount = inputAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0 && rates.compraBolivar > 0) {
                val rawTotal = amount / rates.compraBolivar; val rawUsdt = floor(rawTotal * 100) / 100.0; val fee = 0.06; var finalUsdt = rawUsdt - fee; if (finalUsdt < 0) finalUsdt = 0.0
                val dfUsdt = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
                resultText = "💸 Recibirá Netos:\n${dfUsdt.format(finalUsdt)} USDT"
                noteText = "💡 Desglose de Binance:\nTotal Bruto: ${dfUsdt.format(rawUsdt)} USDT\nComisión: -${dfUsdt.format(fee)} USDT"
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        AnimatedResult(resultText, noteText)
    }
}

@Composable
fun UsdtToPesosScreen(rates: MarketRates, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputAmount by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        CalcRow("🪙 ¿Cuántos USDT venderás? 🇨🇴", inputAmount, { inputAmount = it; resultText = ""; noteText = "" }) {
            val amount = inputAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0 && rates.ventaPesos > 0) {
                val fee = 0.07; var netUsdt = amount - fee; if (netUsdt < 0) netUsdt = 0.0
                val dfFiat = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.GERMAN)); val dfUsdt = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
                val totalFiatRecibir = netUsdt * rates.ventaPesos
                resultText = "💸 Recibirá:\n${dfFiat.format(totalFiatRecibir)} COP"
                noteText = "💡 Desglose de Binance:\nUSDT Vendidos (Brutos): ${dfUsdt.format(amount)} USDT\nComisión: -${dfUsdt.format(fee)} USDT\nCantidad Liberada (Netos): ${dfUsdt.format(netUsdt)} USDT"
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        AnimatedResult(resultText, noteText)
    }
}

@Composable
fun UsdtToBolivaresScreen(rates: MarketRates, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputAmount by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        CalcRow("🪙 ¿Cuántos USDT venderás? 🇻🇪", inputAmount, { inputAmount = it; resultText = ""; noteText = "" }) {
            val amount = inputAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0 && rates.ventaBolivar > 0) {
                val fee = 0.06; var netUsdt = amount - fee; if (netUsdt < 0) netUsdt = 0.0
                val dfFiat = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.GERMAN)); val dfUsdt = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
                val totalFiatRecibir = netUsdt * rates.ventaBolivar
                resultText = "💸 Recibirá:\nBs ${dfFiat.format(totalFiatRecibir)}"
                noteText = "💡 Desglose de Binance:\nUSDT Vendidos (Brutos): ${dfUsdt.format(amount)} USDT\nComisión: -${dfUsdt.format(fee)} USDT\nCantidad Liberada (Netos): ${dfUsdt.format(netUsdt)} USDT"
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        AnimatedResult(resultText, noteText)
    }
}

// --- INSPECTOR P2P ---
@Composable
fun P2PInspectorScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val viewModel: P2PInspectorViewModel = viewModel()
    val offers by viewModel.offers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val prefs = remember { context.getSharedPreferences("p2p_app_data", Context.MODE_PRIVATE) }
    var favorites by remember { mutableStateOf(loadFavorites(prefs)) }

    var selectedFiat by remember { mutableStateOf("COP") }
    var tradeType by remember { mutableStateOf("BUY") }

    val currentBankOptions = bankOptionsByFiat[selectedFiat] ?: listOf("BancolombiaSA", "Nequi", "Daviplata", "Mercadopago")
    var selectedBanks by remember { mutableStateOf<List<String>>(emptyList()) }

    var isVerifiedOnly by remember { mutableStateOf(true) }
    var searchAmount by remember { mutableStateOf("") }

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBankModal by remember { mutableStateOf(false) }
    var showSaveFavDialog by remember { mutableStateOf(false) }
    var showFavListDialog by remember { mutableStateOf(false) }
    var favNameInput by remember { mutableStateOf("") }

    var showReplaceDialog by remember { mutableStateOf(false) }
    var pendingFavNameToSave by remember { mutableStateOf("") }

    var selectedMerchant by remember { mutableStateOf<P2PInspectorOffer?>(null) }
    var selectedMerchantForCalc by remember { mutableStateOf<P2PInspectorOffer?>(null) }

    val formatterSymbols = remember { DecimalFormatSymbols(Locale.US) }
    val limitFormat = remember { DecimalFormat("#,###.##", formatterSymbols) }
    val priceFormat = remember { DecimalFormat("#,##0.00", formatterSymbols) }

    LaunchedEffect(selectedFiat) { selectedBanks = emptyList() }
    LaunchedEffect(selectedFiat, tradeType, selectedBanks, isVerifiedOnly, searchAmount) {
        delay(600)
        viewModel.searchOffers(selectedFiat, tradeType, selectedBanks, isVerifiedOnly, searchAmount)
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(currentFiat = selectedFiat, onDismiss = { showCurrencyDialog = false }) { fiat -> selectedFiat = fiat; showCurrencyDialog = false }
    }

    if (showBankModal) {
        BankMultiSelectionDialog(bankList = currentBankOptions, initialSelected = selectedBanks, onDismiss = { showBankModal = false }) { newSelected -> selectedBanks = newSelected; showBankModal = false }
    }

    if (showReplaceDialog) {
        AlertDialog(
            onDismissRequest = { showReplaceDialog = false }, containerColor = Color(0xFF1E2329),
            title = { Text("Favorito existente", color = BinanceYellow, fontWeight = FontWeight.Bold) },
            text = { Text("Ya existe un favorito guardado como '$pendingFavNameToSave'. ¿Deseas reemplazarlo?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    val existingId = favorites.first { it.name == pendingFavNameToSave }.id
                    val updatedFav = P2PFavorite(existingId, pendingFavNameToSave, selectedFiat, tradeType, searchAmount, isVerifiedOnly, selectedBanks)
                    val newList = favorites.map { if (it.id == existingId) updatedFav else it }
                    saveFavorites(prefs, newList); favorites = newList
                    showReplaceDialog = false; showSaveFavDialog = false; favNameInput = ""
                    Toast.makeText(context, "Favorito reemplazado", Toast.LENGTH_SHORT).show()
                }) { Text("Sí, reemplazar", color = BinanceYellow, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showReplaceDialog = false }) { Text("No", color = TextoGris) } }
        )
    }

    if (showSaveFavDialog && !showReplaceDialog) {
        Dialog(onDismissRequest = { showSaveFavDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐ Guardar como Favorito", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = favNameInput, onValueChange = { favNameInput = it }, label = { Text("Nombre para esta búsqueda", color = TextoGris) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = TextoGris, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showSaveFavDialog = false }, modifier = Modifier.weight(1f)) { Text("Cancelar", color = Color.White) }
                        Button(
                            onClick = {
                                val finalName = favNameInput.trim().ifEmpty { "Favorito ${favorites.size + 1}" }
                                pendingFavNameToSave = finalName
                                if (favorites.any { it.name.equals(finalName, ignoreCase = true) }) { showReplaceDialog = true }
                                else {
                                    val newFav = P2PFavorite(java.util.UUID.randomUUID().toString(), finalName, selectedFiat, tradeType, searchAmount, isVerifiedOnly, selectedBanks)
                                    val newList = favorites + newFav
                                    saveFavorites(prefs, newList); favorites = newList
                                    showSaveFavDialog = false; favNameInput = ""
                                    Toast.makeText(context, "Favorito guardado", Toast.LENGTH_SHORT).show()
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black), modifier = Modifier.weight(1f)
                        ) { Text("Guardar", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    if (showFavListDialog) {
        Dialog(onDismissRequest = { showFavListDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⭐ Mis Búsquedas Favoritas", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (favorites.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No tienes favoritos guardados aún.", color = TextoGris) }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(favorites) { fav ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B3139)), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f).clickable {
                                            selectedFiat = fav.fiat; tradeType = fav.tradeType; selectedBanks = fav.banks; searchAmount = fav.amount; isVerifiedOnly = fav.isVerified
                                            showFavListDialog = false; haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }) {
                                            Text(fav.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            val operation = if (fav.tradeType == "BUY") "Comprar" else "Vender"
                                            val amountTxt = if (fav.amount.isNotEmpty()) " - Monto: ${fav.amount}" else ""
                                            val banksTxt = if (fav.banks.isNotEmpty()) "\nBancos: ${fav.banks.joinToString { it.replace("_", " ") }}" else "\nBancos: Todos"
                                            Text("$operation USDT con ${fav.fiat}$amountTxt$banksTxt", color = BinanceYellow, fontSize = 12.sp)
                                        }
                                        IconButton(onClick = {
                                            val newList = favorites.filter { it.id != fav.id }
                                            saveFavorites(prefs, newList); favorites = newList
                                        }) { Text("🗑️", fontSize = 18.sp) }
                                    }
                                }
                            }
                        }
                    }
                    Button(onClick = { showFavListDialog = false }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B0E11), contentColor = Color.White)) { Text("Cerrar") }
                }
            }
        }
    }

    if (selectedMerchant != null) MerchantProfileDialog(offer = selectedMerchant!!, viewModel = viewModel, onDismiss = { selectedMerchant = null })
    if (selectedMerchantForCalc != null) MerchantCalculatorDialog(offer = selectedMerchantForCalc!!, selectedFiat = selectedFiat, tradeType = tradeType, onDismiss = { selectedMerchantForCalc = null })

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Text("🔙", fontSize = 24.sp) }
                Text("Inspector P2P", color = BinanceYellow, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
            IconButton(onClick = { showFavListDialog = true }) { Text("⭐", fontSize = 24.sp) }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0B0E11), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Button(onClick = { tradeType = "BUY" }, colors = ButtonDefaults.buttonColors(containerColor = if (tradeType == "BUY") BinanceGreen else Color.Transparent, contentColor = Color.White), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) { Text("Comprar USDT", fontWeight = FontWeight.Bold) }
                    Button(onClick = { tradeType = "SELL" }, colors = ButtonDefaults.buttonColors(containerColor = if (tradeType == "SELL") BinanceRed else Color.Transparent, contentColor = Color.White), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) { Text("Vender USDT", fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { showCurrencyDialog = true }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceYellow), border = BorderStroke(1.dp, BinanceYellow), modifier = Modifier.weight(0.4f).height(50.dp)) {
                        val flagInfo = fiatDetails[selectedFiat] ?: Pair("🏳️", "")
                        Text("${flagInfo.first} $selectedFiat", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(onClick = { showBankModal = true }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = if (selectedBanks.isNotEmpty()) Color.Black else Color.White, containerColor = if (selectedBanks.isNotEmpty()) BinanceYellow else Color.Transparent), border = BorderStroke(1.dp, if (selectedBanks.isNotEmpty()) BinanceYellow else TextoGris), modifier = Modifier.weight(0.6f).height(50.dp)) {
                        val labelText = when { selectedBanks.isEmpty() -> "Pago: Todos"; selectedBanks.size == 1 -> selectedBanks.first().replace("_", " "); else -> "${selectedBanks.size} Bancos" }
                        Text(labelText, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchAmount,
                        onValueChange = { newVal -> val filtered = newVal.replace(',', '.').filter { it.isDigit() || it == '.' }; if (filtered.count { it == '.' } <= 1) searchAmount = filtered },
                        label = { Text("Monto a cambiar", color = TextoGris, fontSize = 11.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = ThousandSeparatorVisualTransformation(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = TextoGris, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.weight(0.55f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.weight(0.45f).padding(top = 4.dp)) {
                        Switch(checked = isVerifiedOnly, onCheckedChange = { isVerifiedOnly = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = BinanceYellow, uncheckedThumbColor = Color.LightGray, uncheckedTrackColor = Color.DarkGray), modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = { showSaveFavDialog = true }, modifier = Modifier.size(32.dp)) { Text("💾", fontSize = 18.sp) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BinanceYellow) }
        } else if (offers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("No hay ofertas para estos filtros.", color = BinanceRed, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(offers) { offer ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedMerchant = offer }.padding(vertical = 4.dp)) {
                                    Box(modifier = Modifier.size(26.dp).background(Color(0xFF2B3139), CircleShape), contentAlignment = Alignment.Center) { Text(offer.merchantName.take(1).uppercase(), color = BinanceYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(offer.merchantName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    if (offer.userType == "merchant") {
                                        Spacer(modifier = Modifier.width(4.dp));
                                        Icon(Icons.Filled.Star, "Comerciante", tint = BinanceYellow, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Button(onClick = { selectedMerchantForCalc = offer }, colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black), shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(30.dp)) { Text("🧮 Calcular", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Trade: ", color = TextoGris, fontSize = 11.sp)
                                Text("${offer.monthOrderCount} Órdenes (${String.format(Locale.US, "%.2f", offer.monthFinishRate)}%)", color = TextoGris, fontSize = 11.sp)
                                Text("  |  👍 ${String.format(Locale.US, "%.2f", offer.positiveRate)}%", color = TextoGris, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("$selectedFiat$ ", color = TextoGris, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                                    Text(priceFormat.format(offer.price), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Text(" /USDT", color = TextoGris, fontSize = 11.sp, modifier = Modifier.padding(bottom = 3.dp))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    offer.payMethods.take(3).forEach { method ->
                                        Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(3.dp).background(BinanceYellow, CircleShape).padding(end = 4.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(method, color = Color(0xFFC9CCD2), fontSize = 11.sp) }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Límite ${limitFormat.format(offer.minAmount)} - ${limitFormat.format(offer.maxAmount)} $selectedFiat", color = TextoGris, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Disponible ${limitFormat.format(offer.surplusAmount)} USDT", color = TextoGris, fontSize = 11.sp)

                                    if (offer.requiresVerification) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(color = Color(0xFF2B3139), shape = RoundedCornerShape(4.dp)) {
                                            Text(text = "🪪 Verificación requerida", color = TextoGris, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Button(onClick = { selectedMerchant = offer }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White), shape = CircleShape, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), modifier = Modifier.height(32.dp)) { Text("Ver Perfil", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- DIÁLOGOS Y COMPONENTES REUTILIZABLES ---
@Composable
fun MerchantCalculatorDialog(offer: P2PInspectorOffer, selectedFiat: String, tradeType: String, onDismiss: () -> Unit) {
    var fiatInput by remember { mutableStateOf("") }
    var usdtInput by remember { mutableStateOf("") }
    var isUpdatingFromFiat by remember { mutableStateOf(false) }
    var isUpdatingFromUsdt by remember { mutableStateOf(false) }
    var currentFiatAmount by remember { mutableStateOf(0.0) }
    var currentGrossUsdt by remember { mutableStateOf(0.0) }
    var currentFeeUsdt by remember { mutableStateOf(0.0) }
    var currentNetUsdt by remember { mutableStateOf(0.0) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val formatterSymbols = remember { DecimalFormatSymbols(Locale.US) }
    val priceFormat = remember { DecimalFormat("#,##0.00", formatterSymbols) }
    val dfResult = remember { DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.GERMAN)) }
    val dfUsdt = remember { DecimalFormat("#,##0.00", formatterSymbols) }
    val comision = if (selectedFiat == "VES") 0.06 else 0.07

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧮 Calculadora P2P", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Comerciante: ${offer.merchantName}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Tasa: $selectedFiat ${priceFormat.format(offer.price)} / USDT", color = TextoGris, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = fiatInput,
                    onValueChange = { newVal ->
                        val filtered = newVal.replace(',', '.').filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            fiatInput = filtered
                            if (!isUpdatingFromUsdt) {
                                isUpdatingFromFiat = true
                                val amount = filtered.toDoubleOrNull() ?: 0.0; currentFiatAmount = amount
                                if (amount > 0 && offer.price > 0) {
                                    if (tradeType == "SELL") {
                                        val netUsdt = amount / offer.price; val grossUsdt = netUsdt + comision
                                        currentNetUsdt = netUsdt; currentFeeUsdt = comision; currentGrossUsdt = grossUsdt; usdtInput = dfResult.format(grossUsdt)
                                    } else {
                                        val rawTotal = amount / offer.price; val cantidadTotal = floor(rawTotal * 100) / 100.0; var recibes = maxOf(0.0, cantidadTotal - comision)
                                        currentGrossUsdt = cantidadTotal; currentFeeUsdt = comision; currentNetUsdt = recibes; usdtInput = dfResult.format(recibes)
                                    }
                                } else { usdtInput = "" }
                                isUpdatingFromFiat = false
                            }
                        }
                    },
                    label = { Text("Monto en $selectedFiat", color = TextoGris) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = ThousandSeparatorVisualTransformation(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = TextoGris, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("⇅ Conversión Sincronizada", color = TextoGris, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = usdtInput,
                    onValueChange = { newVal ->
                        val filtered = newVal.replace(',', '.').filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            usdtInput = filtered
                            if (!isUpdatingFromFiat) {
                                isUpdatingFromUsdt = true
                                val usdtVal = filtered.toDoubleOrNull() ?: 0.0
                                if (usdtVal > 0 && offer.price > 0) {
                                    if (tradeType == "SELL") {
                                        val netUsdt = maxOf(0.0, usdtVal - comision); val fiatCalculado = netUsdt * offer.price
                                        currentGrossUsdt = usdtVal; currentFeeUsdt = comision; currentNetUsdt = netUsdt; currentFiatAmount = fiatCalculado; fiatInput = dfResult.format(fiatCalculado)
                                    } else {
                                        val cantidadTotal = usdtVal + comision; val fiatCalculado = cantidadTotal * offer.price
                                        currentNetUsdt = usdtVal; currentGrossUsdt = cantidadTotal; currentFeeUsdt = comision; currentFiatAmount = fiatCalculado; fiatInput = dfResult.format(fiatCalculado)
                                    }
                                } else { fiatInput = "" }
                                isUpdatingFromUsdt = false
                            }
                        }
                    },
                    label = { Text("Monto en USDT", color = TextoGris) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { if (usdtInput.isNotEmpty() && (currentNetUsdt > 0 || currentGrossUsdt > 0)) IconButton(onClick = { showDetailsDialog = true }) { Icon(Icons.Filled.Info, "Ver Detalles", tint = BinanceYellow) } },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = TextoGris, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) { Text("Cerrar", fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showDetailsDialog) {
        Dialog(onDismissRequest = { showDetailsDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) { Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color(0xFF2B3139), CircleShape)) }
                    Text("Detalles de la Orden", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))

                    if (tradeType == "SELL") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Recibes", color = TextoGris, fontSize = 14.sp); Text("${dfResult.format(currentFiatAmount)} $selectedFiat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Precio de USDT", color = TextoGris, fontSize = 14.sp); Text("${priceFormat.format(offer.price)} $selectedFiat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Vendes", color = TextoGris, fontSize = 14.sp); Text("${dfUsdt.format(currentGrossUsdt)} USDT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("├─  Cantidad liberada", color = TextoGris, fontSize = 13.sp); Text("${dfUsdt.format(currentNetUsdt)} USDT", color = Color.White, fontSize = 13.sp) }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("└─  Comisión", color = TextoGris, fontSize = 13.sp); Text("-${dfUsdt.format(currentFeeUsdt)} USDT", color = Color.White, fontSize = 13.sp) }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("You Pay", color = TextoGris, fontSize = 14.sp); Text("${dfResult.format(currentFiatAmount)} $selectedFiat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Precio de USDT", color = TextoGris, fontSize = 14.sp); Text("${priceFormat.format(offer.price)} $selectedFiat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Recibes", color = TextoGris, fontSize = 14.sp); Text("${dfUsdt.format(currentNetUsdt)} USDT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("├─  Cantidad total", color = TextoGris, fontSize = 13.sp); Text("${dfUsdt.format(currentGrossUsdt)} USDT", color = Color.White, fontSize = 13.sp) }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("└─  Comisión", color = TextoGris, fontSize = 13.sp); Text("-${dfUsdt.format(currentFeeUsdt)} USDT", color = Color.White, fontSize = 13.sp) }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(onClick = { showDetailsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black), modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(8.dp)) { Text("Ok", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                }
            }
        }
    }
}

@Composable
fun MerchantProfileDialog(offer: P2PInspectorOffer, viewModel: P2PInspectorViewModel, onDismiss: () -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Información", "Comentarios")

    LaunchedEffect(offer.advertiserNo) { if (offer.advertiserNo.isNotEmpty()) viewModel.loadMerchantDetails(offer.advertiserNo) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
            Column(modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(60.dp).background(Color(0xFF2B3139), CircleShape), contentAlignment = Alignment.Center) { Text(offer.merchantName.take(1).uppercase(), color = BinanceYellow, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(offer.merchantName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    if (offer.userType == "merchant") { Spacer(modifier = Modifier.width(6.dp)); Icon(Icons.Filled.Star, "Comerciante", tint = BinanceYellow, modifier = Modifier.size(20.dp)) }
                }
                Text(if (offer.userType == "merchant") "Comerciante Verificado" else "Usuario Regular", color = if (offer.userType == "merchant") BinanceYellow else TextoGris, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFF2B3139))
                Spacer(modifier = Modifier.height(16.dp))

                TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent, contentColor = BinanceYellow, indicator = { tabPositions -> if (selectedTabIndex < tabPositions.size) TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = BinanceYellow) }) {
                    tabs.forEachIndexed { index, title -> Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, color = if (selectedTabIndex == index) BinanceYellow else TextoGris, fontWeight = FontWeight.Bold) }) }
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 16.dp)) {
                    val isLoadingDetails by viewModel.isLoadingDetails.collectAsState()
                    val stats by viewModel.selectedMerchantStats.collectAsState()

                    if (selectedTabIndex == 0) {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            if (isLoadingDetails) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BinanceYellow) }
                            } else {
                                InfoRow("Transacciones 30d", "${stats.monthOrderCount}"); Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Promedio de finalización últ. 30d", "${String.format(Locale.US, "%.1f", stats.monthFinishRate)}%"); Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Tiempo promedio de liberación", "${String.format(Locale.US, "%.2f", stats.avgReleaseTime)} minutos"); Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Tiempo promedio de pago", "${String.format(Locale.US, "%.2f", stats.avgPayTime)} minutos"); Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFF2B3139)); Spacer(modifier = Modifier.height(16.dp))
                                InfoRow("Tipo de trading", stats.tradingType); Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Registrado", "${stats.registerDays} Día(s) atrás"); Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Primer comercio", "${stats.firstTradeDays} Día(s) atrás"); Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Contrapartes de comercio", "${stats.counterpartyCount}"); Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                    Text("Todos los comercios", color = TextoGris, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.5f)) {
                                        Text("${stats.totalTradeCount} Órdenes completadas", color = TextoBlanco, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Comprar ${stats.buyOrderCount} | Vender ${stats.sellOrderCount}", color = TextoGris, fontSize = 11.sp, textAlign = TextAlign.End)
                                    }
                                }
                            }
                            if (offer.terms.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(color = Color(0xFF2B3139)); Spacer(modifier = Modifier.height(16.dp))
                                Text("Términos y condiciones de la oferta:", color = TextoGris, fontSize = 12.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                                Text(offer.terms, color = Color.White, fontSize = 14.sp); Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        val comments by viewModel.merchantComments.collectAsState()
                        val posTotal by viewModel.totalPositiveComments.collectAsState()
                        val negTotal by viewModel.totalNegativeComments.collectAsState()
                        val currentPage by viewModel.currentCommentPage.collectAsState()
                        val isLoadingPage by viewModel.isLoadingCommentsPage.collectAsState()

                        if (isLoadingDetails && currentPage == 1) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BinanceYellow) }
                        } else {
                            var filterType by remember { mutableStateOf("Todos") }
                            val filteredComments = when (filterType) { "Positivos" -> comments.filter { it.isPositive }; "Negativos" -> comments.filter { !it.isPositive }; else -> comments }
                            val totalOpiniones = posTotal + negTotal
                            val pctPositivas = if (totalOpiniones > 0) String.format(Locale.US, "%.1f", (posTotal.toDouble() / totalOpiniones) * 100) else "100.0"

                            Column(modifier = Modifier.fillMaxSize()) {
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF181C20)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        Text("👍 $pctPositivas%", color = BinanceGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(" | $totalOpiniones Opinión(es)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChipCustom("Todo", filterType == "Todos") { filterType = "Todos" }
                                    FilterChipCustom("Positiva($posTotal)", filterType == "Positivos") { filterType = "Positivos" }
                                    FilterChipCustom("Negativa($negTotal)", filterType == "Negativos") { filterType = "Negativos" }
                                }
                                if (isLoadingPage && currentPage > 1) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BinanceYellow) }
                                } else if (filteredComments.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("No hay comentarios disponibles.", color = TextoGris, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp)) }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(filteredComments) { comment ->
                                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(28.dp).background(Color(0xFF2B3139), CircleShape), contentAlignment = Alignment.Center) { Text(comment.userName.take(1).uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                                    Spacer(modifier = Modifier.width(8.dp)); Text(comment.userName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(modifier = Modifier.padding(start = 36.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(comment.date, color = TextoGris, fontSize = 11.sp)
                                                    if (comment.payMethod.isNotEmpty()) Text(" | ${comment.payMethod}", color = TextoGris, fontSize = 11.sp)
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(modifier = Modifier.padding(start = 36.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(if (comment.isPositive) "👍" else "👎", fontSize = 14.sp)
                                                    if (comment.tag.isNotEmpty()) { Spacer(modifier = Modifier.width(6.dp)); Surface(color = Color(0xFF332B15), shape = RoundedCornerShape(4.dp)) { Text(comment.tag, color = BinanceYellow, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) } }
                                                }
                                                if (comment.content.isNotEmpty()) { Spacer(modifier = Modifier.height(6.dp)); Text(comment.content, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 36.dp)) }
                                                HorizontalDivider(color = Color(0xFF2B3139), modifier = Modifier.padding(top = 10.dp))
                                            }
                                        }
                                        item {
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                                Button(onClick = { viewModel.loadCommentsPage(offer.advertiserNo, currentPage - 1) }, enabled = currentPage > 1 && !isLoadingPage, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White, disabledContainerColor = Color.Transparent), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(40.dp)) { Text("<", fontWeight = FontWeight.Bold) }
                                                Text("Página $currentPage", color = TextoBlanco, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                                                Button(onClick = { viewModel.loadCommentsPage(offer.advertiserNo, currentPage + 1) }, enabled = !isLoadingPage && comments.size >= 10, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White, disabledContainerColor = Color.Transparent), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(40.dp)) { Text(">", fontWeight = FontWeight.Bold) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) { Text("Volver", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// --- UTILIDADES GRÁFICAS Y COMPONENTES VISUALES ---

@Composable
fun FilterChipCustom(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(color = if (isSelected) Color(0xFF2B3139) else Color.Transparent, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFF2B3139)), modifier = Modifier.clickable(onClick = onClick)) {
        Text(text = text, color = if (isSelected) Color.White else TextoGris, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = TextoGris, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(text = value, color = TextoBlanco, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SingleRateInfoCard(direction: String, tasaNeta: Double, tasaDia: Double, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329).copy(alpha = 0.8f)), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(direction, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextoBlanco)
            Text("Neta: ${"%.2f".format(tasaNeta)}", color = TextoGris, fontSize = 13.sp)
            Text("Día: ${"%.2f".format(tasaDia)}", color = Color(0xFF81C995), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CurrencySelectionDialog(currentFiat: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filteredList = remember(query) { allFiatCurrencies.filter { code -> val details = fiatDetails[code] ?: Pair("🏳️", "Desconocido"); code.contains(query, ignoreCase = true) || details.second.contains(query, ignoreCase = true) } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxHeight(0.8f).fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Seleccionar Divisa", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("Ej: COP o Colombia...", color = TextoGris) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = TextoGris, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()); Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredList) { fiat ->
                        val details = fiatDetails[fiat] ?: Pair("🏳️", "Desconocido")
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(fiat) }.padding(vertical = 12.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("$fiat ${details.first} (${details.second})", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            if (fiat == currentFiat) Text("✓", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = Color(0xFF2B3139))
                    }
                }
            }
        }
    }
}

@Composable
fun BankMultiSelectionDialog(bankList: List<String>, initialSelected: List<String>, onDismiss: () -> Unit, onConfirm: (List<String>) -> Unit) {
    var tempSelected by remember { mutableStateOf(initialSelected.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredBanks = remember(searchQuery, bankList) { bankList.filter { it.replace("_", " ").contains(searchQuery, ignoreCase = true) } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxHeight(0.85f).fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pagar con (Selecciona Varios)", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Buscar banco...", color = TextoGris) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BinanceYellow, unfocusedBorderColor = TextoGris, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()); Spacer(modifier = Modifier.height(12.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filteredBanks) { bank ->
                        val isSelected = tempSelected.contains(bank); val displayName = bank.replace("_", " ")
                        Surface(onClick = { tempSelected = if (isSelected) tempSelected - bank else tempSelected + bank }, shape = RoundedCornerShape(8.dp), color = if (isSelected) BinanceYellow else Color(0xFF2B3139), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) { Text(displayName, color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { tempSelected = emptySet() }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = BorderStroke(1.dp, Color.Gray), modifier = Modifier.weight(1f)) { Text("Restablecer") }
                    Button(onClick = { onConfirm(tempSelected.toList()) }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black), modifier = Modifier.weight(1f)) { Text("Confirmar (${tempSelected.size})", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(FondoOscuro),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = BotonLila,
            strokeWidth = 6.dp,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Buscando la mejor tasa del mercado...",
            color = TextoBlanco,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("📶 Error de red. Revisa tu conexión 🔌", color = MaterialTheme.colorScheme.error, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🔙 Volver al Menú") }
    }
}

@Composable
fun AnimatedMenuButton(text: String, delayMillis: Int, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMillis.toLong()); isVisible = true }

    AnimatedVisibility(visible = isVisible, enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn() + scaleIn()) {
        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black), modifier = Modifier.fillMaxWidth(0.85f).height(60.dp).padding(vertical = 4.dp)) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun CalcRow(label: String, value: String, onValueChange: (String) -> Unit, onCalculate: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = { newVal ->
                val standardized = newVal.replace(',', '.')
                val filtered = standardized.filter { char -> char.isDigit() || char == '.' }
                if (filtered.count { char -> char == '.' } <= 1) onValueChange(filtered)
            },
            label = { Text(label, color = TextoGris) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ThousandSeparatorVisualTransformation(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = TextoBlanco, focusedIndicatorColor = BotonLila,
                unfocusedTextColor = TextoBlanco, focusedTextColor = TextoBlanco, cursorColor = BotonLila
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onCalculate() }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🧮 Calcular") }
    }
}

@Composable
fun AnimatedResult(resultText: String, noteText: String = "") {
    AnimatedVisibility(visible = resultText.isNotEmpty(), enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) {
        SelectionContainer {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color(0xFF1E2329), RoundedCornerShape(16.dp)).padding(16.dp)) {
                Text(resultText, color = TextoBlanco, fontSize = 18.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                if (noteText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(noteText, color = Color(0xFF81C995), fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PaymentDialog(currency: String, selectedOption: Int, cantidadAEnviar: String, cantidadARecibir: String, onDismiss: () -> Unit) {
    var qrExpanded by remember { mutableStateOf<Int?>(null) }
    var qrBankName by remember { mutableStateOf("") }
    var animateContent by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var comprobanteUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            comprobanteUri = uri
            if (uri != null) {
                val msn = "Hola, aquí te envío mi comprobante de pago"
                abrirWhatsAppConComprobante(context, NUMERO_WHATSAPP, msn, uri)
            }
        }
    )

    val dialogTitle = when (selectedOption) {
        1 -> "🏦 Datos de Cuenta"
        2 -> if (currency == "BOLIVARES") "📱 Pago Móvil" else "🔑 Pago por Llaves"
        3 -> "📲 Códigos QR"
        else -> "Detalles de Pago"
    }

    LaunchedEffect(Unit) { delay(100); animateContent = true }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = FondoOscuro) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(dialogTitle, color = TextoBlanco, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Text("❌", fontSize = 18.sp) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedOption == 3 && (currency == "PESOS" || currency == "BOLIVARES")) {
                    Text("🏦 Selecciona un Banco para ver el QR", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))

                    val bancosQR = if (currency == "PESOS") {
                        listOf("Bancolombia" to 0, "Nequi" to 0, "Davivienda" to 0, "Daviplata" to 0, "Banco de Bogotá" to 0, "Nu Bank" to 0)
                    } else {
                        listOf("Bancamiga" to 0, "Pago Móvil" to 0)
                    }

                    AnimatedVisibility(visible = animateContent, enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))) {
                        Column {
                            bancosQR.chunked(2).forEach { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowItems.forEach { (nombre, icono) ->
                                        Button(onClick = { qrExpanded = icono; qrBankName = nombre }, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (qrBankName == nombre) BotonLila else Color(0xFF2B3139), contentColor = if (qrBankName == nombre) Color.Black else TextoBlanco), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                                            Text(nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(targetState = Pair(qrExpanded, qrBankName), transitionSpec = { scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith fadeOut(tween(150)) }, label = "qr_animation") { (icono, nombre) ->
                        if (icono != null) {
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                    Text("QR de $nombre", color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                                    if (icono != 0) {
                                        Image(painter = painterResource(id = icono), contentDescription = "Código QR $nombre", modifier = Modifier.size(280.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp))
                                    } else {
                                        Text("⚠️ Imagen QR no cargada aún", color = Color.Gray, modifier = Modifier.padding(32.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    AnimatedVisibility(visible = animateContent, enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                if (currency == "PESOS") {
                                    if (selectedOption == 1) {
                                        Text("🏦 BANCOLOMBIA", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Nombre", "Humberto Rosales García")
                                        CopyableItem("Tipo de Cuenta", "Ahorro")
                                        CopyableItem("Número de Cuenta", "91280165986")
                                        Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(color = Color.Gray, thickness = 1.dp); Spacer(modifier = Modifier.height(16.dp))
                                        Text("📱 NEQUI", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Nombre", "Humberto Rosales García")
                                        CopyableItem("Número", "3337337769")
                                    } else if (selectedOption == 2) {
                                        Text("🔑 LLAVES DISPONIBLES", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Bancolombia", "@humberto502")
                                        CopyableItem("Banco de Bogotá", "BB502")
                                        CopyableItem("Davivienda", "@DAVI3337337769")
                                    }
                                } else if (currency == "BOLIVARES") {
                                    if (selectedOption == 1) {
                                        Text("🏦 CUENTA BANCAMIGA", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Nombre", "Liliana Isabel Martinez")
                                        CopyableItem("Cédula", "20195627")
                                        CopyableItem("Cuenta", "01720608786084779187")
                                    } else if (selectedOption == 2) {
                                        Text("📱 PAGO MÓVIL", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Banco", "0172 (Bancamiga)")
                                        CopyableItem("Teléfono", "04120216596")
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("✅ ¡QUEDAMOS ATENTOS AL COMPROBANTE!", color = Color(0xFF81C995), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                if (selectedOption != 3 || (selectedOption == 3 && qrExpanded != null)) {
                    AnimatedVisibility(visible = animateContent, enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(color = Color(0xFF2B3139), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Paso 2: Adjunta tu comprobante", color = Color(0xFF81C995), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (comprobanteUri == null) {
                                Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = TextoBlanco), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(55.dp)) { Text("📸 Seleccionar Comprobante", fontWeight = FontWeight.Bold) }
                            } else {
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A28)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("✅ Imagen lista para enviar", color = Color(0xFF81C995), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); val msn = "Hola, aquí te envío mi comprobante de pago"; abrirWhatsAppConComprobante(context, NUMERO_WHATSAPP, msn, comprobanteUri) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().height(55.dp)) { Text("📲 Volver a enviar", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Cambiar imagen", color = TextoGris) }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PaymentSection(currency: String, isVisible: Boolean, cantidadAEnviar: String, cantidadARecibir: String) {
    var step by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf(0) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) { if (!isVisible) { step = 0; selectedOption = 0; showPaymentDialog = false } }

    if (showPaymentDialog) PaymentDialog(currency, selectedOption, cantidadAEnviar, cantidadARecibir) { showPaymentDialog = false }

    AnimatedVisibility(visible = isVisible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
            if (step == 0) Button(onClick = { step = 1 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C995), contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("💳 Proceder con el Pago", fontWeight = FontWeight.Bold) }
            AnimatedVisibility(visible = step >= 1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("¿Cómo deseas pagar? 👇", color = TextoBlanco, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (currency == "PESOS") {
                            Button(onClick = { selectedOption = 1; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🏦 Cuenta", fontSize = 13.sp) }
                            Button(onClick = { selectedOption = 2; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🔑 Llaves", fontSize = 13.sp) }
                            Button(onClick = { selectedOption = 3; step = 2; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("📱 QR", fontSize = 13.sp) }
                        } else if (currency == "BOLIVARES") {
                            Button(onClick = { selectedOption = 1; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🏦 Cuenta", fontSize = 13.sp) }
                            Button(onClick = { selectedOption = 2; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("📱 P. Móvil", fontSize = 13.sp) }
                            Button(onClick = { selectedOption = 3; step = 2; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("📱 QR", fontSize = 13.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CopyableItem(label: String, value: String) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val haptic = LocalHapticFeedback.current

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = TextoGris, fontSize = 12.sp)
            Text(text = value, color = TextoBlanco, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(
            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); val clip = ClipData.newPlainText(label, value); clipboardManager.setPrimaryClip(clip); Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.background(Color(0xFF2B3139), CircleShape).size(40.dp)
        ) { Text("📋", fontSize = 18.sp) }
    }
}