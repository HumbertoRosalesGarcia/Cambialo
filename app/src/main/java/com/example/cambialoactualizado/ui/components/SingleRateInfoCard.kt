package com.example.cambialoactualizado.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cambialoactualizado.ui.theme.TextoBlanco
import com.example.cambialoactualizado.ui.theme.TextoGris

@Composable
fun SingleRateInfoCard(direction: String, tasaNeta: Double, tasaDia: Double, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(direction, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextoBlanco)
            Text("Neta: ${"%.2f".format(tasaNeta)}", color = TextoGris, fontSize = 13.sp)
            Text("Día: ${"%.2f".format(tasaDia)}", color = Color(0xFF81C995), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
