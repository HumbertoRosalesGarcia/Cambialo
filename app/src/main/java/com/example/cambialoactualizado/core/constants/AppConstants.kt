package com.example.cambialoactualizado.core.constants

const val NUMERO_WHATSAPP = "573337337769"
const val LOCAL_API_URL = "http://158.247.123.136:3000"

val fiatDetails = mapOf(
    "COP" to Pair("🇨🇴", "Colombia"),
    "VES" to Pair("🇻🇪", "Venezuela"),
    "BRL" to Pair("🇧🇷", "Brasil"),
    "ARS" to Pair("🇦🇷", "Argentina"),
    "USD" to Pair("🇺🇸", "Estados Unidos"),
    "EUR" to Pair("🇪🇺", "Eurozona"),
    "MXN" to Pair("🇲🇽", "México"),
    "PEN" to Pair("🇵🇪", "Perú"),
    "CLP" to Pair("🇨🇱", "Chile")
)

val allFiatCurrencies = fiatDetails.keys.toList().sorted()

val bankOptionsByFiat = mapOf(
    "COP" to listOf(
        "BancolombiaSA", "Nequi", "Daviplata", "DaviviendaSA", "Llaves_Bre_B", "BBVA",
        "Banco_Caja_Social", "Banco_Falabella", "Banco_Itau_Colombia", "Banco_Popular",
        "Banco_de_Bogota", "DAVIBank", "Efecty", "Global66", "Movii", "Powwi",
        "Trans_Bank", "Uala_Colombia"
    ),
    "VES" to listOf(
        "PagoMovil", "Banesco", "Mercantil", "Provincial", "Bancamiga",
        "BBVA_Provincial", "Banco_de_Venezuela", "BNC", "BOD"
    ),
    "BRL" to listOf(
        "PIX", "Nubank", "PicPay", "Banco_Inter", "Banco_do_Brasil",
        "Itau", "Bradesco", "Caixa", "Santander", "Transferencia_Bancaria"
    )
)
