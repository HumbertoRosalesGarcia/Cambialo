package com.example.cambialoactualizado.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cambialoactualizado.core.constants.formatBankDisplayName
import com.example.cambialoactualizado.ui.theme.BinanceYellow
import com.example.cambialoactualizado.ui.theme.TextoGris

@Composable
fun BankMultiSelectionDialog(
    bankList: List<String>,
    initialSelected: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(initialSelected.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredBanks = remember(searchQuery, bankList) {
        bankList.filter { formatBankDisplayName(it).contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E2329),
            modifier = Modifier.fillMaxHeight(0.85f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pagar con (Selecciona Varios)", color = BinanceYellow, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar banco...", color = TextoGris) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = TextoGris,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredBanks) { bank ->
                        val isSelected = tempSelected.contains(bank)
                        val displayName = formatBankDisplayName(bank)
                        Surface(
                            onClick = { tempSelected = if (isSelected) tempSelected - bank else tempSelected + bank },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BinanceYellow else Color(0xFF2B3139),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                                Text(
                                    displayName,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { tempSelected = emptySet() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.Gray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restablecer")
                    }
                    Button(
                        onClick = { onConfirm(tempSelected.toList()) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar (${tempSelected.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
