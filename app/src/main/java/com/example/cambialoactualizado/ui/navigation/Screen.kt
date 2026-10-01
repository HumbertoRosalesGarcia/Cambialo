package com.example.cambialoactualizado.ui.navigation

sealed class Screen {
    object Loading : Screen()
    object Error : Screen()
    object Menu : Screen()
    object P2PInspector : Screen()

    // Menú 2: Pesos / Bolívares
    object PesosBolivaresMenu : Screen()
    object PesosABolivares : Screen()
    object BolivaresAPesos : Screen()

    // Menú 3: Bolívares / Pesos
    object BolivaresPesosMenu : Screen()
    object VenColBolivaresAPesos : Screen()
    object VenColPesosABolivares : Screen()

    // Menú 4: USDT
    object CombinedMenu : Screen()
    object PesosAUsdt : Screen()
    object BolivaresAUsdt : Screen()
    object UsdtAPesos : Screen()
    object UsdtABolivares : Screen()
}
