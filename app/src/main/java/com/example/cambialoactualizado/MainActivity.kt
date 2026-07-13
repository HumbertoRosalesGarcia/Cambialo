package com.example.cambialoactualizado

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI
import kotlin.math.roundToInt

// --- COLORES PERSONALIZADOS ---
val FondoOscuro = Color(0xFF202124)
val BotonLila = Color(0xFFD0BCFF)
val TextoBlanco = Color.White
val TextoGris = Color.LightGray

// --- TU NÚMERO DE WHATSAPP ---
const val NUMERO_WHATSAPP = "573001234567"

// --- MODELOS DE DATOS ---
data class OfertaP2P(val precio: Double, val minimo: Double)
data class MarketRates(
    val compraPesos: Double = 0.0,
    val ventaPesos: Double = 0.0,
    val compraBolivar: Double = 0.0,
    val ventaBolivar: Double = 0.0,
    val bcv: Double = 0.0
)

// --- ESTADOS DE NAVEGACIÓN ---
sealed class Screen {
    object Loading : Screen()
    object Error : Screen()
    object Menu : Screen()
    object PesosABolivares : Screen()
    object BolivaresAPesos : Screen()
    object UsdtMenu : Screen()
    object UsdtAPesos : Screen()
    object UsdtABolivares : Screen()
}

// --- VIEWMODEL ---
class ExchangeViewModel : ViewModel() {
    private val _rates = MutableStateFlow(MarketRates())
    val rates: StateFlow<MarketRates> = _rates

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Loading)
    val currentScreen: StateFlow<Screen> = _currentScreen

    val gananciaDecimalPB = 0.07
    val gananciaDecimalBP = 0.25

    init {
        viewModelScope.launch(Dispatchers.IO) {
            actualizarTasasSilenciosamente()
            withContext(Dispatchers.Main) {
                if (_rates.value.compraPesos > 0.0) _currentScreen.value = Screen.Menu else _currentScreen.value = Screen.Error
            }
            while(true) {
                delay(5000)
                actualizarTasasSilenciosamente()
            }
        }
    }

    private suspend fun actualizarTasasSilenciosamente() {
        val bancosColombia = listOf("Bancolombia", "BancolombiaSA", "Nequi")
        val bancosVenezuela = listOf("Bancamiga", "PagoMovil")

        val compraPesos = obtenerMejorPrecioP2P("COP", "BUY", bancosColombia, "8400")?.precio ?: 0.0
        val ventaPesos = obtenerMejorPrecioP2P("COP", "SELL", bancosColombia, "8400")?.precio ?: 0.0
        val compraBolivar = obtenerMejorPrecioP2P("VES", "BUY", bancosVenezuela, "220")?.precio ?: 0.0
        val ventaBolivar = obtenerMejorPrecioP2P("VES", "SELL", bancosVenezuela, "220")?.precio ?: 0.0
        val tasaBcv = obtenerTasaBCV()

        withContext(Dispatchers.Main) {
            if (compraPesos > 0.0 && ventaPesos > 0.0 && compraBolivar > 0.0 && ventaBolivar > 0.0) {
                _rates.value = MarketRates(compraPesos, ventaPesos, compraBolivar, ventaBolivar, tasaBcv)
            }
        }
    }

    fun fetchRatesAndNavigate(targetScreen: Screen) {
        _currentScreen.value = Screen.Loading
        viewModelScope.launch(Dispatchers.IO) {
            actualizarTasasSilenciosamente()
            withContext(Dispatchers.Main) {
                if (_rates.value.compraPesos == 0.0) _currentScreen.value = Screen.Error else _currentScreen.value = targetScreen
            }
        }
    }

    fun navigateTo(screen: Screen) { _currentScreen.value = screen }

    private fun obtenerMejorPrecioP2P(fiat: String, tradeType: String, bancos: List<String>, montoBuscado: String): OfertaP2P? {
        return try {
            val url = URI("https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val filtroPago = if (bancos.isNotEmpty()) bancos.joinToString(prefix = "[", postfix = "]", separator = ", ") { "\"$it\"" } else "[]"
            val filtroMonto = if (montoBuscado.isNotEmpty()) """"transAmount": "$montoBuscado",""" else ""

            val jsonInputString = """{"asset": "USDT", "fiat": "$fiat", "tradeType": "$tradeType", "payTypes": $filtroPago, $filtroMonto "countries": [], "page": 1, "rows": 1, "publisherType": "merchant", "proMerchantAds": false, "shieldMerchantAds": false, "filterType": "all"}"""

            OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val precio = """"price":"([\d.]+)"""".toRegex().find(response)?.groups?.get(1)?.value?.toDoubleOrNull()
                val minimo = """"minSingleTransAmount":"([\d.]+)"""".toRegex().find(response)?.groups?.get(1)?.value?.toDoubleOrNull()
                if (precio != null && minimo != null) OfertaP2P(precio, minimo) else null
            } else null
        } catch (e: Exception) { null }
    }

    private fun obtenerTasaBCV(): Double {
        var tasa = 0.0
        try {
            val url1 = URI("https://ve.dolarapi.com/v1/dolares/oficial").toURL()
            val conn1 = url1.openConnection() as HttpURLConnection
            conn1.requestMethod = "GET"
            conn1.setRequestProperty("Accept", "application/json")
            conn1.connectTimeout = 5000
            conn1.readTimeout = 5000

            if (conn1.responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(conn1.inputStream)).use { it.readText() }
                val match = """"promedio"\s*:\s*([\d.]+)""".toRegex().find(response) ?: """"venta"\s*:\s*([\d.]+)""".toRegex().find(response)
                tasa = match?.groups?.get(1)?.value?.toDoubleOrNull() ?: 0.0
            }
        } catch (e: Exception) { }

        if (tasa == 0.0) {
            try {
                val url2 = URI("https://pydolarvenezuela-api.vercel.app/api/v1/dollar?page=bcv").toURL()
                val conn2 = url2.openConnection() as HttpURLConnection
                conn2.requestMethod = "GET"
                conn2.setRequestProperty("Accept", "application/json")
                conn2.connectTimeout = 5000
                conn2.readTimeout = 5000

                if (conn2.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(conn2.inputStream)).use { it.readText() }
                    val match = """"price"\s*:\s*([\d.]+)""".toRegex().find(response)
                    tasa = match?.groups?.get(1)?.value?.toDoubleOrNull() ?: 0.0
                }
            } catch (e: Exception) { }
        }
        return tasa
    }
}

// --- ACTIVITY PRINCIPAL ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = FondoOscuro) {
                    AppNavigation()
                }
            }
        }
    }
}

// --- NAVEGADOR ANIMADO ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(viewModel: ExchangeViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val rates by viewModel.rates.collectAsState()
    val context = LocalContext.current

    var showWarning by remember { mutableStateOf(true) }

    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            containerColor = Color(0xFF303134),
            title = {
                Text("⚠️ Advertencia de Mercado 📉", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            },
            text = {
                Text("El mercado de divisas cambia cada segundo ⏱️. Tenga en cuenta que el cambio puede variar en contra o a su favor dependiendo de la volatilidad actual del mercado 📊.", color = TextoBlanco)
            },
            confirmButton = {
                Button(
                    onClick = { showWarning = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)
                ) {
                    Text("👍 Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == Screen.Loading) {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            } else {
                slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut()
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.Loading -> LoadingScreen()
            Screen.Error -> ErrorScreen(onRetry = { viewModel.fetchRatesAndNavigate(Screen.Menu) })
            Screen.Menu -> MainMenu(
                rates = rates,
                gananciaPB = viewModel.gananciaDecimalPB,
                gananciaBP = viewModel.gananciaDecimalBP,
                onNavigate = { target -> viewModel.fetchRatesAndNavigate(target) },
                onExit = { (context as? Activity)?.finish() }
            )
            Screen.PesosABolivares -> PesosToBolivaresScreen(rates, viewModel.gananciaDecimalPB) { viewModel.navigateTo(Screen.Menu) }
            Screen.BolivaresAPesos -> BolivaresToPesosScreen(rates, viewModel.gananciaDecimalBP) { viewModel.navigateTo(Screen.Menu) }
            Screen.UsdtMenu -> UsdtMenuScreen { viewModel.navigateTo(it) }
            Screen.UsdtAPesos -> UsdtToPesosScreen(rates) { viewModel.navigateTo(Screen.UsdtMenu) }
            Screen.UsdtABolivares -> UsdtToBolivaresScreen(rates) { viewModel.navigateTo(Screen.UsdtMenu) }
        }
    }
}

// --- FUNCIÓN PARA ENVIAR IMAGEN Y TEXTO A WHATSAPP ---
fun abrirWhatsAppConComprobante(context: Context, numero: String, mensaje: String, imageUri: Uri?) {
    if (imageUri != null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, mensaje)
            putExtra("jid", "$numero@s.whatsapp.net")
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        }
    } else {
        val url = "https://api.whatsapp.com/send?phone=$numero&text=${Uri.encode(mensaje)}"
        val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        }
    }
}

// --- VENTANA EMERGENTE (DIALOG) ANIMADA PARA TODOS LOS MÉTODOS DE PAGO ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PaymentDialog(currency: String, selectedOption: Int, cantidadAEnviar: String, cantidadARecibir: String, onDismiss: () -> Unit) {
    var qrExpanded by remember { mutableStateOf<Int?>(null) }
    var qrBankName by remember { mutableStateOf("") }

    // Estado para controlar la animación de entrada de los elementos
    var animateContent by remember { mutableStateOf(false) }

    var comprobanteUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> comprobanteUri = uri }
    )

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val dialogTitle = when (selectedOption) {
        1 -> "🏦 Datos de Cuenta"
        2 -> if (currency == "BOLIVARES") "📱 Pago Móvil" else "🔑 Pago por Llaves"
        3 -> "📲 Códigos QR"
        else -> "Detalles de Pago"
    }

    // Disparador de la animación un instante después de abrir la ventana
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = FondoOscuro) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dialogTitle, color = TextoBlanco, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Text("❌", fontSize = 18.sp) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SI ES LA OPCIÓN DE QR
                if (selectedOption == 3 && (currency == "PESOS" || currency == "BOLIVARES")) {
                    Text("🏦 Selecciona un Banco para ver el QR", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))

                    val bancosQR = if (currency == "PESOS") {
                        listOf(
                            "Bancolombia" to R.drawable.qr_bancolombia,
                            "Nequi" to R.drawable.qr_nequi,
                            "Davivienda" to R.drawable.qr_davivienda,
                            "Daviplata" to R.drawable.qr_daviplata,
                            "Banco de Bogotá" to R.drawable.qr_bdb,
                            "Nu Bank" to R.drawable.qr_nu
                        )
                    } else {
                        listOf("Bancamiga" to 0, "Pago Móvil" to 0)
                    }

                    // Botones de Bancos Animados
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
                                                containerColor = if (qrBankName == nombre) BotonLila else Color(0xFF4A4B4E),
                                                contentColor = if (qrBankName == nombre) Color.Black else TextoBlanco
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Animación del código QR
                    AnimatedContent(
                        targetState = Pair(qrExpanded, qrBankName),
                        transitionSpec = { scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith fadeOut(tween(150)) },
                        label = "qr_animation"
                    ) { (icono, nombre) ->
                        if (icono != null) {
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF303134)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                    Text("QR de $nombre", color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                                    if (icono != 0) {
                                        Image(
                                            painter = painterResource(id = icono),
                                            contentDescription = "Código QR $nombre",
                                            modifier = Modifier.size(280.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp)
                                        )
                                    } else {
                                        Text("⚠️ Imagen QR no cargada aún", color = Color.Gray, modifier = Modifier.padding(32.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                // SI ES OPCIÓN DE CUENTAS O LLAVES (AHORA CON BOTÓN DE COPIAR)
                else {
                    AnimatedVisibility(
                        visible = animateContent,
                        enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF303134)),
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
                                        Divider(color = Color.Gray, thickness = 1.dp)
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
                                        CopyableItem("Banco Popular", "BPHRG50201")
                                        CopyableItem("Daviplata", "@PLATA333733769")
                                        CopyableItem("NU BANK", "HRG502")
                                    }
                                } else if (currency == "BOLIVARES") {
                                    if (selectedOption == 1) {
                                        Text("🏦 CUENTA BANCAMIGA", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Nombre", "Liliana Isabel Martinez Hernandez")
                                        CopyableItem("Cédula", "20195627")
                                        CopyableItem("Cuenta", "01720608786084779187")
                                    } else if (selectedOption == 2) {
                                        Text("📱 PAGO MÓVIL", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CopyableItem("Banco", "0172 (Bancamiga)")
                                        CopyableItem("Teléfono", "04120216596")
                                        CopyableItem("Cédula", "20195627")
                                        CopyableItem("Nombre", "Liliana Isabel Martinez Hernandez")
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Text("✅ ¡QUEDAMOS ATENTOS AL COMPROBANTE!", color = Color(0xFF81C995), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                // --- SECCIÓN UNIFICADA: ADJUNTAR Y ENVIAR COMPROBANTE ---
                if (selectedOption != 3 || (selectedOption == 3 && qrExpanded != null)) {

                    AnimatedVisibility(
                        visible = animateContent,
                        enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Divider(color = Color(0xFF4A4B4E), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(24.dp))

                            Text("Paso 2: Adjunta tu comprobante", color = Color(0xFF81C995), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (comprobanteUri == null) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4B4E), contentColor = TextoBlanco),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(55.dp)
                                ) {
                                    Text("📸 Seleccionar Comprobante", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A28)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("✅ Imagen lista para enviar", color = Color(0xFF81C995), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val metodoPago = if (selectedOption == 3) "el QR de $qrBankName" else if (selectedOption == 1) "Cuenta Bancaria" else if (currency == "BOLIVARES") "Pago Móvil" else "Llaves"
                                                val msn = "Hola! Acabo de transferir *${cantidadAEnviar}* a través de $metodoPago. Espero recibir *${cantidadARecibir}*. Aquí adjunto mi comprobante:"
                                                abrirWhatsAppConComprobante(context, NUMERO_WHATSAPP, msn, comprobanteUri)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(55.dp)
                                        ) {
                                            Text("📲 Enviar por WhatsApp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { comprobanteUri = null }) { Text("Cambiar imagen", color = TextoGris) }
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

// --- MÓDULO DE SELECCIÓN DE PAGOS ---
@Composable
fun PaymentSection(currency: String, isVisible: Boolean, cantidadAEnviar: String, cantidadARecibir: String) {
    var step by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf(0) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (!isVisible) { step = 0; selectedOption = 0; showPaymentDialog = false }
    }

    if (showPaymentDialog) {
        PaymentDialog(currency, selectedOption, cantidadAEnviar, cantidadARecibir) { showPaymentDialog = false }
    }

    AnimatedVisibility(visible = isVisible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
            if (step == 0) {
                Button(onClick = { step = 1 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C995), contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) {
                    Text("💳 Proceder con el Pago", fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = step >= 1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("¿Cómo deseas pagar? 👇", color = TextoBlanco, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (currency == "PESOS") {
                            Button(onClick = { selectedOption = 1; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🏦 Cuenta", fontSize = 13.sp) }
                            Button(onClick = { selectedOption = 2; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🔑 Llaves", fontSize = 13.sp) }
                            Button(onClick = { selectedOption = 3; step = 2; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("📱 QR", fontSize = 13.sp) }
                        } else if (currency == "BOLIVARES") {
                            Button(onClick = { selectedOption = 1; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🏦 Cuenta", fontSize = 13.sp) }
                            Button(onClick = { selectedOption = 2; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("📱 P. Móvil", fontSize = 13.sp) }
                            Button(onClick = { selectedOption = 3; step = 2; showPaymentDialog = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("📱 QR", fontSize = 13.sp) }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTES REUTILIZABLES ---
@Composable
fun CopyableItem(label: String, value: String) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = TextoGris, fontSize = 12.sp)
            Text(text = value, color = TextoBlanco, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val clip = ClipData.newPlainText(label, value)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.background(Color(0xFF4A4B4E), CircleShape).size(40.dp)
        ) {
            Text("📋", fontSize = 18.sp)
        }
    }
}

@Composable
fun AnimatedMenuButton(text: String, delayMillis: Int, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isVisible = true
    }

    AnimatedVisibility(visible = isVisible, enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn() + scaleIn()) {
        Button(
            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth(0.85f).height(60.dp).padding(vertical = 4.dp)
        ) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun CalcRow(label: String, value: String, onValueChange: (String) -> Unit, onCalculate: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value, onValueChange = onValueChange, label = { Text(label, color = TextoGris) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedIndicatorColor = TextoBlanco, focusedIndicatorColor = BotonLila, unfocusedTextColor = TextoBlanco, focusedTextColor = TextoBlanco, cursorColor = BotonLila),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onCalculate() }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) {
            Text("🧮 Calcular")
        }
    }
}

@Composable
fun AnimatedResult(resultText: String, noteText: String = "") {
    AnimatedVisibility(visible = resultText.isNotEmpty() && !resultText.contains("¡Se ha encontrado"), enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color(0xFF303134), RoundedCornerShape(16.dp)).padding(16.dp)) {
            Text(resultText, color = TextoBlanco, fontSize = 18.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            if (noteText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(noteText, color = Color(0xFF81C995), fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// --- PANTALLAS ---
@Composable
fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation1 by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "rot1")
    val rotation2 by infiniteTransition.animateFloat(360f, 0f, infiniteRepeatable(tween(1500, easing = LinearEasing)), label = "rot2")
    val rotation3 by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing)), label = "rot3")

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(color = BotonLila, startAngle = rotation1, sweepAngle = 270f, useCenter = false, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
                drawArc(color = Color(0xFF81C995), startAngle = rotation2, sweepAngle = 180f, useCenter = false, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round), topLeft = Offset(15f, 15f), size = Size(size.width - 30f, size.height - 30f))
                drawArc(color = Color(0xFFFFD700), startAngle = rotation3, sweepAngle = 90f, useCenter = false, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round), topLeft = Offset(30f, 30f), size = Size(size.width - 60f, size.height - 60f))
            }
            Text("💱", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Buscando la mejor tasa del mercado...", color = TextoBlanco, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("📶 Error de red. Revisa tu conexión 🔌", color = MaterialTheme.colorScheme.error, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)) { Text("🔙 Volver al Menú") }
    }
}

@Composable
fun MainMenu(rates: MarketRates, gananciaPB: Double, gananciaBP: Double, onNavigate: (Screen) -> Unit, onExit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {

        if (rates.compraPesos > 0.0 && rates.compraBolivar > 0.0) {
            val tasaNetaPB = rates.compraPesos / rates.compraBolivar
            val tasaDiaPB = tasaNetaPB + (tasaNetaPB * gananciaPB)

            val tasaNetaBP = rates.compraBolivar / rates.compraPesos
            val tasaDiaBP = tasaNetaBP + (tasaNetaBP * gananciaBP)

            AnimatedVisibility(visible = true, enter = fadeIn(tween(1000)), modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 24.dp, start = 16.dp, end = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("📊 MERCADO Y BCV", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Compra COP: ${rates.compraPesos}", color = TextoGris, fontSize = 10.sp)
                        Text("Venta COP: ${rates.ventaPesos}", color = TextoGris, fontSize = 10.sp)
                        Text("Compra VES: ${rates.compraBolivar}", color = TextoGris, fontSize = 10.sp)
                        Text("Venta VES: ${rates.ventaBolivar}", color = TextoGris, fontSize = 10.sp)
                        Text("Tasa BCV: ${rates.bcv}", color = TextoGris, fontSize = 10.sp)
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF303134).copy(alpha = 0.8f)), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                            Text("🇨🇴 ➔ 🇻🇪", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoBlanco)
                            Text("Neta: ${"%.2f".format(tasaNetaPB)}", color = TextoGris, fontSize = 11.sp)
                            Text("Día: ${"%.2f".format(tasaDiaPB)}", color = Color(0xFF81C995), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("🇻🇪 ➔ 🇨🇴", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoBlanco)
                            Text("Neta: ${"%.2f".format(tasaNetaBP)}", color = TextoGris, fontSize = 11.sp)
                            Text("Día: ${"%.2f".format(tasaDiaBP)}", color = Color(0xFF81C995), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(top = 100.dp, bottom = 24.dp, start = 24.dp, end = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val infiniteTransition = rememberInfiniteTransition(label = "title_animation")
            val rotation by infiniteTransition.animateFloat(initialValue = -15f, targetValue = 15f, animationSpec = infiniteRepeatable(animation = tween(800, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "rotation")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💱", fontSize = 28.sp, modifier = Modifier.rotate(rotation))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ELIGE EL TIPO DE\nCAMBIO", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextoBlanco, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(8.dp))
                Text("💱", fontSize = 28.sp, modifier = Modifier.rotate(-rotation))
            }
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedMenuButton("🇨🇴 1. DE PESOS A\nBOLÍVARES 🇻🇪", 100) { onNavigate(Screen.PesosABolivares) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🇻🇪 2. DE BOLÍVARES A\nPESOS 🇨🇴", 200) { onNavigate(Screen.BolivaresAPesos) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🪙 3. CAMBIO DE USDT", 300) { onNavigate(Screen.UsdtMenu) }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedMenuButton("🚪 4. SALIR DEL PROGRAMA 👋", 400) { onExit() }
        }
    }
}

// --- SUBMENÚ DE USDT ---
@Composable
fun UsdtMenuScreen(onNavigate: (Screen) -> Unit) {
    BackHandler { onNavigate(Screen.Menu) }
    Box(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🪙", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("CAMBIO DE USDT", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextoBlanco, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedMenuButton("🪙 1. DE USDT A PESOS 🇨🇴", 100) { onNavigate(Screen.UsdtAPesos) }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedMenuButton("🪙 2. DE USDT A BOLÍVARES 🇻🇪", 200) { onNavigate(Screen.UsdtABolivares) }
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { onNavigate(Screen.Menu) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4B4E), contentColor = Color.White),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(0.5f).height(50.dp)
            ) {
                Text("🔙 Volver al Menú")
            }
        }
    }
}

@Composable
fun PesosToBolivaresScreen(rates: MarketRates, ganancia: Double, onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    var pesosInput by remember { mutableStateOf("") }
    var bolivaresInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("✨ ¡Se ha encontrado la mejor tasa para ti! 🎯") }
    var noteText by remember { mutableStateOf("") }

    var enviarMensaje by remember { mutableStateOf("") }
    var recibirMensaje by remember { mutableStateOf("") }

    var profitCop by remember { mutableStateOf(0.0) }
    var profitUsdt by remember { mutableStateOf(0.0) }

    val tasaNeta = rates.compraPesos / rates.compraBolivar
    val tasaDelDia = tasaNeta + (tasaNeta * ganancia)

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        AnimatedVisibility(visible = resultText.contains("¡Se ha encontrado")) { Text(resultText, color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(modifier = Modifier.height(16.dp))

        CalcRow("🇨🇴 ¿Cuántos Pesos tienes? 💵", pesosInput, { pesosInput = it; bolivaresInput = ""; resultText = ""; noteText = ""; profitCop = 0.0; profitUsdt = 0.0 }) {
            val amount = pesosInput.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val bolivares = amount / tasaDelDia
                resultText = "✅ ${amount} COP serían Bs ${"%.2f".format(bolivares)}"
                enviarMensaje = "${amount.roundToInt()} COP"
                recibirMensaje = "Bs ${"%.2f".format(bolivares)}"

                val usdPesos = amount / rates.compraPesos
                val usdBolivares = if (rates.bcv > 0) bolivares / rates.bcv else 0.0
                noteText = "💡 Son $${"%.2f".format(usdPesos)} y tus bolívares serían $${"%.2f".format(usdBolivares)} al BCV ⚖️"

                profitCop = amount - (bolivares * tasaNeta)
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalcRow("🇻🇪 ¿Cuántos BS quieres? 💰", bolivaresInput, { bolivaresInput = it; pesosInput = ""; resultText = ""; noteText = ""; profitCop = 0.0; profitUsdt = 0.0 }) {
            val amount = bolivaresInput.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val pesos = (amount * tasaDelDia).roundToInt()
                resultText = "🏦 Para recibir Bs ${amount} debes consignar ${pesos} COP"
                enviarMensaje = "${pesos} COP"
                recibirMensaje = "Bs ${amount}"

                val usdPesos = pesos / rates.compraPesos
                val usdBolivares = if (rates.bcv > 0) amount / rates.bcv else 0.0
                noteText = "💡 Son $${"%.2f".format(usdPesos)} y tus bolívares serían $${"%.2f".format(usdBolivares)} al BCV ⚖️"

                profitCop = pesos.toDouble() - (amount * tasaNeta)
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedResult(resultText, noteText)

        AnimatedVisibility(visible = profitCop > 0) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF303134).copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤑 Ganancia Neta de la Operación", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${profitCop.roundToInt()} COP  ≈  $${"%.2f".format(profitUsdt)} USDT", color = Color(0xFF81C995), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        PaymentSection("PESOS", resultText.isNotEmpty() && !resultText.contains("¡Se ha encontrado"), enviarMensaje, recibirMensaje)
    }
}

@Composable
fun BolivaresToPesosScreen(rates: MarketRates, ganancia: Double, onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    var bolivaresInput by remember { mutableStateOf("") }
    var pesosInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("✨ ¡Se ha encontrado la mejor tasa para ti! 🎯") }
    var noteText by remember { mutableStateOf("") }

    var enviarMensaje by remember { mutableStateOf("") }
    var recibirMensaje by remember { mutableStateOf("") }

    var profitCop by remember { mutableStateOf(0.0) }
    var profitUsdt by remember { mutableStateOf(0.0) }

    val tasaNeta = rates.compraBolivar / rates.compraPesos
    val tasaDelDia = tasaNeta + (tasaNeta * ganancia)

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        AnimatedVisibility(visible = resultText.contains("¡Se ha encontrado")) { Text(resultText, color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(modifier = Modifier.height(16.dp))

        CalcRow("🇻🇪 ¿Cuántos BS tienes? 💰", bolivaresInput, { bolivaresInput = it; pesosInput = ""; resultText = ""; noteText = ""; profitCop = 0.0; profitUsdt = 0.0 }) {
            val amount = bolivaresInput.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val pesos = (amount / tasaDelDia).roundToInt()
                resultText = "✅ Bs ${amount} serían ${pesos} COP"
                enviarMensaje = "Bs ${amount}"
                recibirMensaje = "${pesos} COP"

                val usdBolivares = if (rates.bcv > 0) amount / rates.bcv else 0.0
                val usdPesos = pesos / rates.compraPesos
                noteText = "💡 Son $${"%.2f".format(usdBolivares)} al BCV y tus pesos serían $${"%.2f".format(usdPesos)} ⚖️"

                profitCop = (amount / tasaNeta) - pesos.toDouble()
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalcRow("🇨🇴 ¿Cuántos Pesos quieres? 💵", pesosInput, { pesosInput = it; bolivaresInput = ""; resultText = ""; noteText = ""; profitCop = 0.0; profitUsdt = 0.0 }) {
            val amount = pesosInput.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                val bolivares = amount * tasaDelDia
                resultText = "🏦 Para recibir ${amount} COP debes consignar Bs ${"%.2f".format(bolivares)}"
                enviarMensaje = "Bs ${"%.2f".format(bolivares)}"
                recibirMensaje = "${amount} COP"

                val usdPesos = amount / rates.compraPesos
                val usdBolivares = if (rates.bcv > 0) bolivares / rates.bcv else 0.0
                noteText = "💡 Son $${"%.2f".format(usdBolivares)} al BCV y tus pesos serían $${"%.2f".format(usdPesos)} ⚖️"

                profitCop = (bolivares / tasaNeta) - amount
                profitUsdt = if (rates.compraPesos > 0) profitCop / rates.compraPesos else 0.0
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedResult(resultText, noteText)

        AnimatedVisibility(visible = profitCop > 0) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF303134).copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤑 Ganancia Neta de la Operación", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${profitCop.roundToInt()} COP  ≈  $${"%.2f".format(profitUsdt)} USDT", color = Color(0xFF81C995), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        PaymentSection("BOLIVARES", resultText.isNotEmpty() && !resultText.contains("¡Se ha encontrado"), enviarMensaje, recibirMensaje)
    }
}

@Composable
fun UsdtToPesosScreen(rates: MarketRates, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputAmount by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("✨ ¡Se ha encontrado la mejor tasa para ti! 🎯") }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        AnimatedVisibility(visible = resultText.contains("¡Se ha encontrado")) { Text(resultText, color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(modifier = Modifier.height(16.dp))

        CalcRow("🪙 ¿Cuántos USDT venderás? 🇨🇴", inputAmount, { inputAmount = it; resultText = "" }) {
            val amount = inputAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0) resultText = "💸 Recibirá:\n${(amount * rates.ventaPesos).roundToInt()} COP"
        }

        Spacer(modifier = Modifier.height(48.dp))
        AnimatedResult(resultText)
    }
}

@Composable
fun UsdtToBolivaresScreen(rates: MarketRates, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var inputAmount by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("✨ ¡Se ha encontrado la mejor tasa para ti! 🎯") }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        AnimatedVisibility(visible = resultText.contains("¡Se ha encontrado")) { Text(resultText, color = TextoBlanco, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(modifier = Modifier.height(16.dp))

        CalcRow("🪙 ¿Cuántos USDT venderás? 🇻🇪", inputAmount, { inputAmount = it; resultText = "" }) {
            val amount = inputAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0) resultText = "💸 Recibirá:\nBs ${"%.2f".format(amount * rates.ventaBolivar)}"
        }

        Spacer(modifier = Modifier.height(48.dp))
        AnimatedResult(resultText)
    }
}