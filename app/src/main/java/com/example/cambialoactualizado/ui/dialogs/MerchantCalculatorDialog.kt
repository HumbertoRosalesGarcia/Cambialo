package com.example.cambialoactualizado.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cambialoactualizado.core.util.ThousandSeparatorVisualTransformation
import com.example.cambialoactualizado.data.model.P2PInspectorOffer
import com.example.cambialoactualizado.ui.theme.BinanceYellow
import com.example.cambialoactualizado.ui.theme.TextoGris
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.floor

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
                                val amount = filtered.toDoubleOrNull() ?: 0.0
                                currentFiatAmount = amount
                                if (amount > 0 && offer.price > 0) {
                                    if (tradeType == "SELL") {
                                        val netUsdt = amount / offer.price
                                        val grossUsdt = netUsdt + comision
                                        currentNetUsdt = netUsdt
                                        currentFeeUsdt = comision
                                        currentGrossUsdt = grossUsdt
                                        usdtInput = dfResult.format(grossUsdt)
                                    } else {
                                        val rawTotal = amount / offer.price
                                        val cantidadTotal = floor(rawTotal * 100) / 100.0
                                        val recibes = maxOf(0.0, cantidadTotal - comision)
                                        currentGrossUsdt = cantidadTotal
                                        currentFeeUsdt = comision
                                        currentNetUsdt = recibes
                                        usdtInput = dfResult.format(recibes)
                                    }
                                } else {
                                    usdtInput = ""
                                }
                                isUpdatingFromFiat = false
                            }
                        }
                    },
                    label = { Text("Monto en $selectedFiat", color = TextoGris) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = TextoGris,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
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
                                        val netUsdt = maxOf(0.0, usdtVal - comision)
                                        val fiatCalculado = netUsdt * offer.price
                                        currentGrossUsdt = usdtVal
                                        currentFeeUsdt = comision
                                        currentNetUsdt = netUsdt
                                        currentFiatAmount = fiatCalculado
                                        fiatInput = dfResult.format(fiatCalculado)
                                    } else {
                                        val cantidadTotal = usdtVal + comision
                                        val fiatCalculado = cantidadTotal * offer.price
                                        currentNetUsdt = usdtVal
                                        currentGrossUsdt = cantidadTotal
                                        currentFeeUsdt = comision
                                        currentFiatAmount = fiatCalculado
                                        fiatInput = dfResult.format(fiatCalculado)
                                    }
                                } else {
                                    fiatInput = ""
                                }
                                isUpdatingFromUsdt = false
                            }
                        }
                    },
                    label = { Text("Monto en USDT", color = TextoGris) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        if (usdtInput.isNotEmpty() && (currentNetUsdt > 0 || currentGrossUsdt > 0)) {
                            IconButton(onClick = { showDetailsDialog = true }) {
                                Icon(Icons.Filled.Info, "Ver Detalles", tint = BinanceYellow)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = TextoGris,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDetailsDialog) {
        Dialog(onDismissRequest = { showDetailsDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color(0xFF2B3139), CircleShape))
                    }
                    Text("Detalles de la Orden", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))

                    if (tradeType == "SELL") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recibes", color = TextoGris, fontSize = 14.sp)
                            Text("${dfResult.format(currentFiatAmount)} $selectedFiat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Precio de USDT", color = TextoGris, fontSize = 14.sp)
                            Text("${priceFormat.format(offer.price)} $selectedFiat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vendes", color = TextoGris, fontSize = 14.sp)
                            Text("${dfUsdt.format(currentGrossUsdt)} USDT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("├─  Cantidad liberada", color = TextoGris, fontSize = 13.sp)
                            Text("${dfUsdt.format(currentNetUsdt)} USDT", color = Color.White, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("└─  Comisión", color = TextoGris, fontSize = 13.sp)
                            Text("-${dfUsdt.format(currentFeeUsdt)} USDT", color = Color.White, fontSize = 13.sp)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("You Pay", color = TextoGris, fontSize = 14.sp)
                            Text("${dfResult.format(currentFiatAmount)} $selectedFiat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Precio de USDT", color = TextoGris, fontSize = 14.sp)
                            Text("${priceFormat.format(offer.price)} $selectedFiat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recibes", color = TextoGris, fontSize = 14.sp)
                            Text("${dfUsdt.format(currentNetUsdt)} USDT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("├─  Cantidad total", color = TextoGris, fontSize = 13.sp)
                            Text("${dfUsdt.format(currentGrossUsdt)} USDT", color = Color.White, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("└─  Comisión", color = TextoGris, fontSize = 13.sp)
                            Text("-${dfUsdt.format(currentFeeUsdt)} USDT", color = Color.White, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { showDetailsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ok", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
