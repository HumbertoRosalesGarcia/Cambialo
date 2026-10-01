package com.example.cambialoactualizado.ui.screens.usdt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cambialoactualizado.data.model.MarketRates
import com.example.cambialoactualizado.ui.components.AnimatedResult
import com.example.cambialoactualizado.ui.components.CalcRow
import com.example.cambialoactualizado.ui.components.MercadoBcvInfo
import com.example.cambialoactualizado.ui.theme.FondoOscuro
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.floor

@Composable
fun PesosToUsdtScreen(rates: MarketRates, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputAmount by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MercadoBcvInfo(rates = rates, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
        HorizontalDivider(color = Color(0xFF2B3139), modifier = Modifier.padding(bottom = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        CalcRow("🇨🇴 ¿Cuántos Pesos tienes? 🪙", inputAmount, { inputAmount = it; resultText = ""; noteText = "" }) {
            val amount = inputAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0 && rates.compraPesos > 0) {
                val rawTotal = amount / rates.compraPesos
                val rawUsdt = floor(rawTotal * 100) / 100.0
                val fee = 0.07
                var finalUsdt = rawUsdt - fee
                if (finalUsdt < 0) finalUsdt = 0.0
                val dfUsdt = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
                resultText = "💸 Recibirá Netos:\n${dfUsdt.format(finalUsdt)} USDT"
                noteText = "💡 Desglose de Binance:\nTotal Bruto: ${dfUsdt.format(rawUsdt)} USDT\nComisión: -${dfUsdt.format(fee)} USDT"
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        AnimatedResult(resultText, noteText)
    }
}
