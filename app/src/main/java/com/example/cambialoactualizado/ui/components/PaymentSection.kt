package com.example.cambialoactualizado.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.cambialoactualizado.ui.theme.BotonLila
import com.example.cambialoactualizado.ui.theme.TextoBlanco

@Composable
fun PaymentSection(
    currency: String,
    isVisible: Boolean,
    cantidadAEnviar: String,
    cantidadARecibir: String
) {
    var step by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf(0) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            step = 0
            selectedOption = 0
            showPaymentDialog = false
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            currency = currency,
            selectedOption = selectedOption,
            cantidadAEnviar = cantidadAEnviar,
            cantidadARecibir = cantidadARecibir
        ) {
            showPaymentDialog = false
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
            if (step == 0) {
                Button(
                    onClick = { step = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C995), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("💳 Proceder con el Pago", fontWeight = FontWeight.Bold)
                }
            }
            AnimatedVisibility(visible = step >= 1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("¿Cómo deseas pagar? 👇", color = TextoBlanco, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (currency == "PESOS") {
                            Button(
                                onClick = { selectedOption = 1; showPaymentDialog = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)
                            ) {
                                Text("🏦 Cuenta", fontSize = 13.sp)
                            }
                            Button(
                                onClick = { selectedOption = 2; showPaymentDialog = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)
                            ) {
                                Text("🔑 Llaves", fontSize = 13.sp)
                            }
                            Button(
                                onClick = { selectedOption = 3; step = 2; showPaymentDialog = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)
                            ) {
                                Text("📱 QR", fontSize = 13.sp)
                            }
                        } else if (currency == "BOLIVARES") {
                            Button(
                                onClick = { selectedOption = 1; showPaymentDialog = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)
                            ) {
                                Text("🏦 Cuenta", fontSize = 13.sp)
                            }
                            Button(
                                onClick = { selectedOption = 2; showPaymentDialog = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)
                            ) {
                                Text("📱 P. Móvil", fontSize = 13.sp)
                            }
                            Button(
                                onClick = { selectedOption = 3; step = 2; showPaymentDialog = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)
                            ) {
                                Text("📱 QR", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
