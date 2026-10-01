package com.example.cambialoactualizado.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambialoactualizado.core.constants.bankOptionsByFiat
import com.example.cambialoactualizado.core.constants.fiatDetails
import com.example.cambialoactualizado.core.util.ThousandSeparatorVisualTransformation
import com.example.cambialoactualizado.core.util.loadFavorites
import com.example.cambialoactualizado.core.util.saveFavorites
import com.example.cambialoactualizado.data.model.P2PFavorite
import com.example.cambialoactualizado.data.model.P2PInspectorOffer
import com.example.cambialoactualizado.ui.dialogs.BankMultiSelectionDialog
import com.example.cambialoactualizado.ui.dialogs.CurrencySelectionDialog
import com.example.cambialoactualizado.ui.dialogs.MerchantCalculatorDialog
import com.example.cambialoactualizado.ui.dialogs.MerchantProfileDialog
import com.example.cambialoactualizado.ui.theme.BinanceGreen
import com.example.cambialoactualizado.ui.theme.BinanceRed
import com.example.cambialoactualizado.ui.theme.BinanceYellow
import com.example.cambialoactualizado.ui.theme.FondoOscuro
import com.example.cambialoactualizado.ui.theme.TextoGris
import com.example.cambialoactualizado.ui.viewmodel.P2PInspectorViewModel
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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
        CurrencySelectionDialog(currentFiat = selectedFiat, onDismiss = { showCurrencyDialog = false }) { fiat ->
            selectedFiat = fiat
            showCurrencyDialog = false
        }
    }

    if (showBankModal) {
        BankMultiSelectionDialog(bankList = currentBankOptions, initialSelected = selectedBanks, onDismiss = { showBankModal = false }) { newSelected ->
            selectedBanks = newSelected
            showBankModal = false
        }
    }

    if (showReplaceDialog) {
        AlertDialog(
            onDismissRequest = { showReplaceDialog = false },
            containerColor = Color(0xFF1E2329),
            title = { Text("Favorito existente", color = BinanceYellow, fontWeight = FontWeight.Bold) },
            text = { Text("Ya existe un favorito guardado como '$pendingFavNameToSave'. ¿Deseas reemplazarlo?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    val existingId = favorites.first { it.name == pendingFavNameToSave }.id
                    val updatedFav = P2PFavorite(existingId, pendingFavNameToSave, selectedFiat, tradeType, searchAmount, isVerifiedOnly, selectedBanks)
                    val newList = favorites.map { if (it.id == existingId) updatedFav else it }
                    saveFavorites(prefs, newList)
                    favorites = newList
                    showReplaceDialog = false
                    showSaveFavDialog = false
                    favNameInput = ""
                    Toast.makeText(context, "Favorito reemplazado", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Sí, reemplazar", color = BinanceYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplaceDialog = false }) {
                    Text("No", color = TextoGris)
                }
            }
        )
    }

    if (showSaveFavDialog && !showReplaceDialog) {
        Dialog(onDismissRequest = { showSaveFavDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E2329), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐ Guardar como Favorito", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = favNameInput,
                        onValueChange = { favNameInput = it },
                        label = { Text("Nombre para esta búsqueda", color = TextoGris) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = TextoGris,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showSaveFavDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancelar", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val finalName = favNameInput.trim().ifEmpty { "Favorito ${favorites.size + 1}" }
                                pendingFavNameToSave = finalName
                                if (favorites.any { it.name.equals(finalName, ignoreCase = true) }) {
                                    showReplaceDialog = true
                                } else {
                                    val newFav = P2PFavorite(
                                        java.util.UUID.randomUUID().toString(),
                                        finalName,
                                        selectedFiat,
                                        tradeType,
                                        searchAmount,
                                        isVerifiedOnly,
                                        selectedBanks
                                    )
                                    val newList = favorites + newFav
                                    saveFavorites(prefs, newList)
                                    favorites = newList
                                    showSaveFavDialog = false
                                    favNameInput = ""
                                    Toast.makeText(context, "Favorito guardado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes favoritos guardados aún.", color = TextoGris)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(favorites) { fav ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B3139)), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f).clickable {
                                                selectedFiat = fav.fiat
                                                tradeType = fav.tradeType
                                                selectedBanks = fav.banks
                                                searchAmount = fav.amount
                                                isVerifiedOnly = fav.isVerified
                                                showFavListDialog = false
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        ) {
                                            Text(fav.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            val operation = if (fav.tradeType == "BUY") "Comprar" else "Vender"
                                            val amountTxt = if (fav.amount.isNotEmpty()) " - Monto: ${fav.amount}" else ""
                                            val banksTxt = if (fav.banks.isNotEmpty()) "\nBancos: ${fav.banks.joinToString { it.replace("_", " ") }}" else "\nBancos: Todos"
                                            Text("$operation USDT con ${fav.fiat}$amountTxt$banksTxt", color = BinanceYellow, fontSize = 12.sp)
                                        }
                                        IconButton(onClick = {
                                            val newList = favorites.filter { it.id != fav.id }
                                            saveFavorites(prefs, newList)
                                            favorites = newList
                                        }) {
                                            Text("🗑️", fontSize = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { showFavListDialog = false },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B0E11), contentColor = Color.White)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }

    if (selectedMerchant != null) {
        MerchantProfileDialog(offer = selectedMerchant!!, viewModel = viewModel, onDismiss = { selectedMerchant = null })
    }
    if (selectedMerchantForCalc != null) {
        MerchantCalculatorDialog(offer = selectedMerchantForCalc!!, selectedFiat = selectedFiat, tradeType = tradeType, onDismiss = { selectedMerchantForCalc = null })
    }

    var verificationOfferForDialog by remember { mutableStateOf<P2PInspectorOffer?>(null) }
    if (verificationOfferForDialog != null) {
        AlertDialog(
            onDismissRequest = { verificationOfferForDialog = null },
            containerColor = Color(0xFF1E2329),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️ ", fontSize = 18.sp)
                    Text("Verificación de seguridad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    "Para este anuncio, los takers deben proporcionarle al anunciante documentos adicionales después de crear órdenes.",
                    color = Color(0xFFC9CCD2),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { verificationOfferForDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Text("🔙", fontSize = 24.sp) }
                Text("Inspector P2P", color = BinanceYellow, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
            IconButton(onClick = { showFavListDialog = true }) { Text("⭐", fontSize = 24.sp) }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0B0E11), RoundedCornerShape(8.dp)).padding(4.dp)) {
                    Button(
                        onClick = { tradeType = "BUY" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (tradeType == "BUY") BinanceGreen else Color.Transparent, contentColor = Color.White),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Comprar USDT", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { tradeType = "SELL" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (tradeType == "SELL") BinanceRed else Color.Transparent, contentColor = Color.White),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Vender USDT", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCurrencyDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceYellow),
                        border = BorderStroke(1.dp, BinanceYellow),
                        modifier = Modifier.weight(0.4f).height(50.dp)
                    ) {
                        val flagInfo = fiatDetails[selectedFiat] ?: Pair("🏳️", "")
                        Text("${flagInfo.first} $selectedFiat", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { showBankModal = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (selectedBanks.isNotEmpty()) Color.Black else Color.White,
                            containerColor = if (selectedBanks.isNotEmpty()) BinanceYellow else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (selectedBanks.isNotEmpty()) BinanceYellow else TextoGris),
                        modifier = Modifier.weight(0.6f).height(50.dp)
                    ) {
                        val labelText = when {
                            selectedBanks.isEmpty() -> "Pago: Todos"
                            selectedBanks.size == 1 -> selectedBanks.first().replace("_", " ")
                            else -> "${selectedBanks.size} Bancos"
                        }
                        Text(labelText, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchAmount,
                        onValueChange = { newVal ->
                            val filtered = newVal.replace(',', '.').filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) searchAmount = filtered
                        },
                        label = { Text("Monto a cambiar", color = TextoGris, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ThousandSeparatorVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = TextoGris,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(0.55f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.weight(0.45f).padding(top = 4.dp)) {
                        Switch(
                            checked = isVerifiedOnly,
                            onCheckedChange = { isVerifiedOnly = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = BinanceYellow,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = { showSaveFavDialog = true }, modifier = Modifier.size(32.dp)) {
                            Text("💾", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BinanceYellow)
            }
        } else if (offers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No hay ofertas para estos filtros.", color = BinanceRed, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(offers) { offer ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedMerchant = offer }.padding(vertical = 4.dp)
                                ) {
                                    Box(modifier = Modifier.size(26.dp).background(Color(0xFF2B3139), CircleShape), contentAlignment = Alignment.Center) {
                                        Text(offer.merchantName.take(1).uppercase(), color = BinanceYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(offer.merchantName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    if (offer.userType == "merchant") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.Star, "Comerciante", tint = BinanceYellow, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Button(
                                    onClick = { selectedMerchantForCalc = offer },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("🧮 Calcular", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(3.dp).background(BinanceYellow, CircleShape).padding(end = 4.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(method, color = Color(0xFFC9CCD2), fontSize = 11.sp)
                                        }
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
                                        Surface(
                                            color = Color(0xFF2B3139),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.clickable { verificationOfferForDialog = offer }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("🛡️", fontSize = 9.sp)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = "Verificación", color = TextoGris, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                                Button(
                                    onClick = { selectedMerchant = offer },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Ver Perfil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
