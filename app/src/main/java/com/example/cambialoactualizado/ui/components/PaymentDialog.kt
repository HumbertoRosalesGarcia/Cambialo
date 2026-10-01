package com.example.cambialoactualizado.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cambialoactualizado.core.constants.NUMERO_WHATSAPP
import com.example.cambialoactualizado.core.util.abrirWhatsAppConComprobante
import com.example.cambialoactualizado.ui.theme.BotonLila
import com.example.cambialoactualizado.ui.theme.FondoOscuro
import com.example.cambialoactualizado.ui.theme.TextoBlanco
import com.example.cambialoactualizado.ui.theme.TextoGris
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PaymentDialog(
    currency: String,
    selectedOption: Int,
    cantidadAEnviar: String,
    cantidadARecibir: String,
    onDismiss: () -> Unit
) {
    var qrExpanded by remember { mutableStateOf<Int?>(null) }
    var qrBankName by remember { mutableStateOf("") }
    var animateContent by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var comprobanteUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            comprobanteUri = uri
            if (uri != null) {
                val msn = "Hola, aquí te envío mi comprobante de pago"
                abrirWhatsAppConComprobante(context, NUMERO_WHATSAPP, msn, uri)
            }
        }
    )

    val dialogTitle = when (selectedOption) {
        1 -> "🏦 Datos de Cuenta"
        2 -> if (currency == "BOLIVARES") "📱 Pago Móvil" else "🔑 Pago por Llaves"
        3 -> "📲 Códigos QR"
        else -> "Detalles de Pago"
    }

    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = FondoOscuro) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dialogTitle, color = TextoBlanco, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Text("❌", fontSize = 18.sp) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedOption == 3 && (currency == "PESOS" || currency == "BOLIVARES")) {
                    Text(
                        "🏦 Selecciona un Banco para ver el QR",
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val bancosQR = if (currency == "PESOS") {
                        listOf(
                            "Bancolombia" to 0,
                            "Nequi" to 0,
                            "Davivienda" to 0,
                            "Daviplata" to 0,
                            "Banco de Bogotá" to 0,
                            "Nu Bank" to 0
                        )
                    } else {
                        listOf("Bancamiga" to 0, "Pago Móvil" to 0)
                    }

                    AnimatedVisibility(
                        visible = animateContent,
                        enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    ) {
                        Column {
                            bancosQR.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { (nombre, icono) ->
                                        Button(
                                            onClick = { qrExpanded = icono; qrBankName = nombre },
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (qrBankName == nombre) BotonLila else Color(0xFF2B3139),
                                                contentColor = if (qrBankName == nombre) Color.Black else TextoBlanco
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                nombre,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(
                        targetState = Pair(qrExpanded, qrBankName),
                        transitionSpec = { scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith fadeOut(tween(150)) },
                        label = "qr_animation"
                    ) { (icono, nombre) ->
                        if (icono != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                                ) {
                                    Text("QR de $nombre", color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                                    if (icono != 0) {
                                        Image(
                                            painter = painterResource(id = icono),
                                            contentDescription = "Código QR $nombre",
                                            modifier = Modifier
                                                .size(280.dp)
                                                .background(Color.White, RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        )
                                    } else {
                                        Text("⚠️ Imagen QR no cargada aún", color = Color.Gray, modifier = Modifier.padding(32.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2329)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                if (currency == "PESOS") {
                                    if (selectedOption == 1) {
                                        Text("🏦 BANCOLOMBIA", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Nombre", "Humberto Rosales García")
                                        CopyableItem("Tipo de Cuenta", "Ahorro")
                                        CopyableItem("Número de Cuenta", "91280165986")
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("📱 NEQUI", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Nombre", "Humberto Rosales García")
                                        CopyableItem("Número", "3337337769")
                                    } else if (selectedOption == 2) {
                                        Text("🔑 LLAVES DISPONIBLES", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Bancolombia", "@humberto502")
                                        CopyableItem("Banco de Bogotá", "BB502")
                                        CopyableItem("Davivienda", "@DAVI3337337769")
                                    }
                                } else if (currency == "BOLIVARES") {
                                    if (selectedOption == 1) {
                                        Text("🏦 CUENTA BANCAMIGA", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Nombre", "Liliana Isabel Martinez")
                                        CopyableItem("Cédula", "20195627")
                                        CopyableItem("Cuenta", "01720608786084779187")
                                    } else if (selectedOption == 2) {
                                        Text("📱 PAGO MÓVIL", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Banco", "0172 (Bancamiga)")
                                        CopyableItem("Teléfono", "04120216596")
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("✅ ¡QUEDAMOS ATENTOS AL COMPROBANTE!", color = Color(0xFF81C995), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                if (selectedOption != 3 || (selectedOption == 3 && qrExpanded != null)) {
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(color = Color(0xFF2B3139), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Paso 2: Adjunta tu comprobante", color = Color(0xFF81C995), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (comprobanteUri == null) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = TextoBlanco),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(55.dp)
                                ) {
                                    Text("📸 Seleccionar Comprobante", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A28)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("✅ Imagen lista para enviar", color = Color(0xFF81C995), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val msn = "Hola, aquí te envío mi comprobante de pago"
                                                abrirWhatsAppConComprobante(context, NUMERO_WHATSAPP, msn, comprobanteUri)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(55.dp)
                                        ) {
                                            Text("📲 Volver a enviar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        }) {
                                            Text("Cambiar imagen", color = TextoGris)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
