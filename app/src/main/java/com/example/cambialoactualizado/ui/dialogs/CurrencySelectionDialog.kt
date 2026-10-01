package com.example.cambialoactualizado.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cambialoactualizado.core.constants.allFiatCurrencies
import com.example.cambialoactualizado.core.constants.fiatDetails
import com.example.cambialoactualizado.ui.theme.BinanceYellow
import com.example.cambialoactualizado.ui.theme.TextoGris

@Composable
fun CurrencySelectionDialog(currentFiat: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filteredList = remember(query) {
        allFiatCurrencies.filter { code ->
            val details = fiatDetails[code] ?: Pair("🏳️", "Desconocido")
            code.contains(query, ignoreCase = true) || details.second.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E2329),
            modifier = Modifier.fillMaxHeight(0.8f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Seleccionar Divisa", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Ej: COP o Colombia...", color = TextoGris) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = TextoGris,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredList) { fiat ->
                        val details = fiatDetails[fiat] ?: Pair("🏳️", "Desconocido")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(fiat) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$fiat ${details.first} (${details.second})", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            if (fiat == currentFiat) Text("✓", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = Color(0xFF2B3139))
                    }
                }
            }
        }
    }
}
