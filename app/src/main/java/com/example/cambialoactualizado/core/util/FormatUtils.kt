package com.example.cambialoactualizado.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class ThousandSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val parts = originalText.split('.')
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1] else null
        val formattedInt = intPart.reversed().chunked(3).joinToString(".").reversed()
        val formattedText = if (decPart != null) "$formattedInt,$decPart" else if (originalText.endsWith(".")) "$formattedInt," else formattedInt

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = 0
                var originalOffset = 0
                for (i in formattedText.indices) {
                    if (originalOffset == offset) break
                    if (formattedText[i] != '.') originalOffset++
                    transformedOffset++
                }
                return minOf(transformedOffset, formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = 0
                for (i in 0 until minOf(offset, formattedText.length)) {
                    if (formattedText[i] != '.') originalOffset++
                }
                return minOf(originalOffset, originalText.length)
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
