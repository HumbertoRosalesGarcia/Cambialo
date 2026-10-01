package com.example.cambialoactualizado.ui.screens.bolivarpeso

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cambialoactualizado.data.model.MarketRates
import com.example.cambialoactualizado.ui.components.AnimatedResult
import com.example.cambialoactualizado.ui.components.CalcRow
import com.example.cambialoactualizado.ui.components.MercadoBcvInfo
import com.example.cambialoactualizado.ui.components.PaymentSection
import com.example.cambialoactualizado.ui.components.SingleRateInfoCard
import com.example.cambialoactualizado.ui.theme.FondoOscuro
import com.example.cambialoactualizado.ui.theme.TextoBlanco
import com.example.cambialoactualizado.ui.theme.TextoGris
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

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

    val tasaNeta = if (rates.ventaPesos > 0) rates.compraBolivar / rates.ventaPesos else 0.0
    val tasaDelDia = tasaNeta * (1 + (ganancia / 100.0))
    val dfCop = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMAN))
    val dfBs = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.GERMAN))

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

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text("⚙️ Ajustar Margen", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(40.dp)
                            .background(Color(0xFF0B0E11), RoundedCornerShape(8.dp))
                            .border(1.dp, TextoGris, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = inputMargin,
                            onValueChange = { newVal ->
                                val filtered = newVal.filter { it.isDigit() }
                                if (filtered.length <= 3) {
                                    inputMargin = filtered
                                    filtered.toDoubleOrNull()?.let { v -> onUpdateGanancia(v) }
                                }
                            },
                            textStyle = TextStyle(color = TextoBlanco, fontSize = 16.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
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
                enviarMensaje = "Bs ${"%.2f".format(bolivares)}"
                recibirMensaje = "${amount.roundToInt()} COP"
                val usdBolivares = if (rates.bcv > 0) bolivares / rates.bcv else 0.0
                noteText = "💡 Equivalen a $${dfBs.format(usdBolivares)} al BCV ⚖️"

                profitCop = amount * (ganancia / 100.0)
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedResult(resultText, noteText)

        AnimatedVisibility(visible = profitCop > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
            ) {
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
