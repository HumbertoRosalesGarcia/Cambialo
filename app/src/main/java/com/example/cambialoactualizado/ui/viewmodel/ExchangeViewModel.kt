package com.example.cambialoactualizado.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambialoactualizado.core.constants.LOCAL_API_URL
import com.example.cambialoactualizado.data.model.MarketRates
import com.example.cambialoactualizado.data.model.OfertaP2P
import com.example.cambialoactualizado.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI

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
            while (true) {
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
