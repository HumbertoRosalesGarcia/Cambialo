package com.example.cambialoactualizado.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cambialoactualizado.data.model.MarketRates
import com.example.cambialoactualizado.ui.theme.TextoGris

@Composable
fun MercadoBcvInfo(rates: MarketRates, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.Start, modifier = modifier) {
        Text("📊 MERCADO Y BCV", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Compra COP: ${rates.compraPesos}", color = TextoGris, fontSize = 10.sp)
        Text("Venta COP: ${rates.ventaPesos}", color = TextoGris, fontSize = 10.sp)
        Text("Compra VES: ${rates.compraBolivar}", color = TextoGris, fontSize = 10.sp)
        Text("Venta VES: ${rates.ventaBolivar}", color = TextoGris, fontSize = 10.sp)
        Text("Tasa BCV: ${rates.bcv}", color = TextoGris, fontSize = 10.sp)
    }
}
