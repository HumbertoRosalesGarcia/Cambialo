package com.example.cambialoactualizado.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cambialoactualizado.data.model.P2PInspectorOffer
import com.example.cambialoactualizado.ui.components.FilterChipCustom
import com.example.cambialoactualizado.ui.components.InfoRow
import com.example.cambialoactualizado.ui.theme.BinanceGreen
import com.example.cambialoactualizado.ui.theme.BinanceYellow
import com.example.cambialoactualizado.ui.theme.TextoBlanco
import com.example.cambialoactualizado.ui.theme.TextoGris
import com.example.cambialoactualizado.ui.viewmodel.P2PInspectorViewModel
import java.util.Locale

@Composable
fun MerchantProfileDialog(offer: P2PInspectorOffer, viewModel: P2PInspectorViewModel, onDismiss: () -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Información", "Comentarios")

    LaunchedEffect(offer.advertiserNo) {
        if (offer.advertiserNo.isNotEmpty()) viewModel.loadMerchantDetails(offer.advertiserNo)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E2329),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(60.dp).background(Color(0xFF2B3139), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(offer.merchantName.take(1).uppercase(), color = BinanceYellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(offer.merchantName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    if (offer.userType == "merchant") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Filled.Star, "Comerciante", tint = BinanceYellow, modifier = Modifier.size(20.dp))
                    }
                }
                Text(
                    if (offer.userType == "merchant") "Comerciante Verificado" else "Usuario Regular",
                    color = if (offer.userType == "merchant") BinanceYellow else TextoGris,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFF2B3139))
                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = BinanceYellow,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = BinanceYellow
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, color = if (selectedTabIndex == index) BinanceYellow else TextoGris, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 16.dp)) {
                    val isLoadingDetails by viewModel.isLoadingDetails.collectAsState()
                    val stats by viewModel.selectedMerchantStats.collectAsState()

                    if (selectedTabIndex == 0) {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            if (isLoadingDetails) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = BinanceYellow)
                                }
                            } else {
                                InfoRow("Transacciones 30d", "${stats.monthOrderCount}")
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Promedio de finalización últ. 30d", "${String.format(Locale.US, "%.1f", stats.monthFinishRate)}%")
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Tiempo promedio de liberación", "${String.format(Locale.US, "%.2f", stats.avgReleaseTime)} minutos")
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Tiempo promedio de pago", "${String.format(Locale.US, "%.2f", stats.avgPayTime)} minutos")
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFF2B3139))
                                Spacer(modifier = Modifier.height(16.dp))
                                InfoRow("Tipo de trading", stats.tradingType)
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Registrado", "${stats.registerDays} Día(s) atrás")
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Primer comercio", "${stats.firstTradeDays} Día(s) atrás")
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Contrapartes de comercio", "${stats.counterpartyCount}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("Todos los comercios", color = TextoGris, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.5f)) {
                                        Text("${stats.totalTradeCount} Órdenes completadas", color = TextoBlanco, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Comprar ${stats.buyOrderCount} | Vender ${stats.sellOrderCount}", color = TextoGris, fontSize = 11.sp, textAlign = TextAlign.End)
                                    }
                                }
                            }
                            if (offer.terms.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFF2B3139))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Términos y condiciones de la oferta:", color = TextoGris, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(offer.terms, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(16.dp))
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
                            val filteredComments = when (filterType) {
                                "Positivos" -> comments.filter { it.isPositive }
                                "Negativos" -> comments.filter { !it.isPositive }
                                else -> comments
                            }
                            val totalOpiniones = posTotal + negTotal
                            val pctPositivas = if (totalOpiniones > 0) String.format(Locale.US, "%.1f", (posTotal.toDouble() / totalOpiniones) * 100) else "100.0"

                            Column(modifier = Modifier.fillMaxSize()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C20)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        Text("👍 $pctPositivas%", color = BinanceGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text(" | $totalOpiniones Opinión(es)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChipCustom("Todo", filterType == "Todos") { filterType = "Todos" }
                                    FilterChipCustom("Positiva($posTotal)", filterType == "Positivos") { filterType = "Positivos" }
                                    FilterChipCustom("Negativa($negTotal)", filterType == "Negativos") { filterType = "Negativos" }
                                }
                                if (isLoadingPage && currentPage > 1) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BinanceYellow) }
                                } else if (filteredComments.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                        Text("No hay comentarios disponibles.", color = TextoGris, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(filteredComments) { comment ->
                                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(28.dp).background(Color(0xFF2B3139), CircleShape), contentAlignment = Alignment.Center) {
                                                        Text(comment.userName.take(1).uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(comment.userName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(modifier = Modifier.padding(start = 36.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(comment.date, color = TextoGris, fontSize = 11.sp)
                                                    if (comment.payMethod.isNotEmpty()) Text(" | ${comment.payMethod}", color = TextoGris, fontSize = 11.sp)
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(modifier = Modifier.padding(start = 36.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(if (comment.isPositive) "👍" else "👎", fontSize = 14.sp)
                                                    if (comment.tag.isNotEmpty()) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(color = Color(0xFF332B15), shape = RoundedCornerShape(4.dp)) {
                                                            Text(comment.tag, color = BinanceYellow, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                        }
                                                    }
                                                }
                                                if (comment.content.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(comment.content, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 36.dp))
                                                }
                                                HorizontalDivider(color = Color(0xFF2B3139), modifier = Modifier.padding(top = 10.dp))
                                            }
                                        }
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = { viewModel.loadCommentsPage(offer.advertiserNo, currentPage - 1) },
                                                    enabled = currentPage > 1 && !isLoadingPage,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White, disabledContainerColor = Color.Transparent),
                                                    contentPadding = PaddingValues(0.dp),
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Text("<", fontWeight = FontWeight.Bold)
                                                }
                                                Text("Página $currentPage", color = TextoBlanco, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                                                Button(
                                                    onClick = { viewModel.loadCommentsPage(offer.advertiserNo, currentPage + 1) },
                                                    enabled = !isLoadingPage && comments.size >= 10,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White, disabledContainerColor = Color.Transparent),
                                                    contentPadding = PaddingValues(0.dp),
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Text(">", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Volver", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
