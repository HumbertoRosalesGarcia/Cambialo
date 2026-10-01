package com.example.cambialoactualizado.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

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
            Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
        }
    } else {
        val url = "https://api.whatsapp.com/send?phone=$numero&text=${Uri.encode(mensaje)}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
        }
    }
}
