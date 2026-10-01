package com.example.cambialoactualizado.ui.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambialoactualizado.ui.screens.ErrorScreen
import com.example.cambialoactualizado.ui.screens.LoadingScreen
import com.example.cambialoactualizado.ui.screens.MainMenu
import com.example.cambialoactualizado.ui.screens.P2PInspectorScreen
import com.example.cambialoactualizado.ui.screens.bolivarpeso.BolivaresPesosMenuScreen
import com.example.cambialoactualizado.ui.screens.bolivarpeso.VenColBolivaresAPesosScreen
import com.example.cambialoactualizado.ui.screens.bolivarpeso.VenColPesosABolivaresScreen
import com.example.cambialoactualizado.ui.screens.pesobolivar.BolivaresToPesosScreen
import com.example.cambialoactualizado.ui.screens.pesobolivar.PesosBolivaresMenuScreen
import com.example.cambialoactualizado.ui.screens.pesobolivar.PesosToBolivaresScreen
import com.example.cambialoactualizado.ui.screens.usdt.BolivaresToUsdtScreen
import com.example.cambialoactualizado.ui.screens.usdt.CombinedMenuScreen
import com.example.cambialoactualizado.ui.screens.usdt.PesosToUsdtScreen
import com.example.cambialoactualizado.ui.screens.usdt.UsdtToBolivaresScreen
import com.example.cambialoactualizado.ui.screens.usdt.UsdtToPesosScreen
import com.example.cambialoactualizado.ui.viewmodel.ExchangeViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(viewModel: ExchangeViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val rates by viewModel.rates.collectAsState()
    val gananciaPB by viewModel.gananciaPB.collectAsState()
    val gananciaBP by viewModel.gananciaBP.collectAsState()
    val gananciaVenCol by viewModel.gananciaVenCol.collectAsState()
    val context = LocalContext.current

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == Screen.Loading) fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            else slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn() togetherWith slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut()
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.Loading -> LoadingScreen()
            Screen.Error -> ErrorScreen(onRetry = { viewModel.fetchRatesAndNavigate(Screen.Menu) })
            Screen.Menu -> MainMenu(rates = rates, onNavigate = { viewModel.fetchRatesAndNavigate(it) }, onExit = { (context as? Activity)?.finish() })
            Screen.P2PInspector -> P2PInspectorScreen { viewModel.navigateTo(Screen.Menu) }

            // Menú 2: Pesos / Bolívares
            Screen.PesosBolivaresMenu -> PesosBolivaresMenuScreen(rates) { viewModel.navigateTo(it) }
            Screen.PesosABolivares -> PesosToBolivaresScreen(rates, gananciaPB, { viewModel.updateGananciaPB(it) }) { viewModel.navigateTo(Screen.PesosBolivaresMenu) }
            Screen.BolivaresAPesos -> BolivaresToPesosScreen(rates, gananciaBP, { viewModel.updateGananciaBP(it) }) { viewModel.navigateTo(Screen.PesosBolivaresMenu) }

            // Menú 3: Bolívares / Pesos
            Screen.BolivaresPesosMenu -> BolivaresPesosMenuScreen(rates) { viewModel.navigateTo(it) }
            Screen.VenColBolivaresAPesos -> VenColBolivaresAPesosScreen(rates, gananciaVenCol, { viewModel.updateGananciaVenCol(it) }) { viewModel.navigateTo(Screen.BolivaresPesosMenu) }
            Screen.VenColPesosABolivares -> VenColPesosABolivaresScreen(rates, gananciaVenCol, { viewModel.updateGananciaVenCol(it) }) { viewModel.navigateTo(Screen.BolivaresPesosMenu) }

            // Menú 4: USDT
            Screen.CombinedMenu -> CombinedMenuScreen(rates) { viewModel.navigateTo(it) }
            Screen.PesosAUsdt -> PesosToUsdtScreen(rates) { viewModel.navigateTo(Screen.CombinedMenu) }
            Screen.BolivaresAUsdt -> BolivaresToUsdtScreen(rates) { viewModel.navigateTo(Screen.CombinedMenu) }
            Screen.UsdtAPesos -> UsdtToPesosScreen(rates) { viewModel.navigateTo(Screen.CombinedMenu) }
            Screen.UsdtABolivares -> UsdtToBolivaresScreen(rates) { viewModel.navigateTo(Screen.CombinedMenu) }
        }
    }
}
