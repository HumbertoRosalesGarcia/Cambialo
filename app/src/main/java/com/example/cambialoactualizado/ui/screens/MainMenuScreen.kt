package com.example.cambialoactualizado.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cambialoactualizado.data.model.MarketRates
import com.example.cambialoactualizado.ui.components.AnimatedMenuButton
import com.example.cambialoactualizado.ui.components.MercadoBcvInfo
import com.example.cambialoactualizado.ui.navigation.Screen
import com.example.cambialoactualizado.ui.theme.BotonLila
import com.example.cambialoactualizado.ui.theme.FondoOscuro
import com.example.cambialoactualizado.ui.theme.TextoBlanco

@Composable
fun MainMenu(rates: MarketRates, onNavigate: (Screen) -> Unit, onExit: () -> Unit) {
    var showInfoModal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {

        // MODAL ANIMADO CON LA INFORMACIÓN
        if (showInfoModal) {
            Dialog(onDismissRequest = { showInfoModal = false }) {
                AnimatedVisibility(
                    visible = showInfoModal,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            MercadoBcvInfo(rates = rates, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showInfoModal = false },
                                colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cerrar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // MENÚ DE OPCIONES
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp, start = 24.dp, end = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "title_animation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = -15f,
                targetValue = 15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation"
            )

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

            // BOTÓN DE MERCADO Y BCV (DEBAJO DE LA OPCIÓN 5)
            if (rates.compraPesos > 0.0 && rates.compraBolivar > 0.0) {
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { showInfoModal = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7FFFD4), // Color Aguamarina
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📊 MERCADO Y BCV", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
