package com.example.cambialoactualizado.ui.screens.pesobolivar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cambialoactualizado.data.model.MarketRates
import com.example.cambialoactualizado.ui.components.AnimatedMenuButton
import com.example.cambialoactualizado.ui.components.MercadoBcvInfo
import com.example.cambialoactualizado.ui.navigation.Screen
import com.example.cambialoactualizado.ui.theme.FondoOscuro
import com.example.cambialoactualizado.ui.theme.TextoBlanco

@Composable
fun PesosBolivaresMenuScreen(rates: MarketRates, onNavigate: (Screen) -> Unit) {
    BackHandler { onNavigate(Screen.Menu) }
    Box(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {
        if (rates.compraPesos > 0.0) {
            MercadoBcvInfo(rates = rates, modifier = Modifier.align(Alignment.TopStart).padding(top = 32.dp, start = 24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 120.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("💱", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Pesos / Bolívares", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextoBlanco, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedMenuButton("🇨🇴 1. ¿CUÁNTOS PESOS TIENES? 💵", 100) { onNavigate(Screen.PesosABolivares) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🇻🇪 2. ¿CUÁNTOS BOLÍVARES QUIERES? 💰", 200) { onNavigate(Screen.BolivaresAPesos) }
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { onNavigate(Screen.Menu) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(0.5f).height(50.dp)
            ) {
                Text("🔙 Volver al Menú")
            }
        }
    }
}
