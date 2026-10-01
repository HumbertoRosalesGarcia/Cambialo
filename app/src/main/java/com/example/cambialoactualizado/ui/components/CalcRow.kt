package com.example.cambialoactualizado.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cambialoactualizado.core.util.ThousandSeparatorVisualTransformation
import com.example.cambialoactualizado.ui.theme.BotonLila
import com.example.cambialoactualizado.ui.theme.TextoBlanco
import com.example.cambialoactualizado.ui.theme.TextoGris

@Composable
fun CalcRow(label: String, value: String, onValueChange: (String) -> Unit, onCalculate: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = { newVal ->
                val standardized = newVal.replace(',', '.')
                val filtered = standardized.filter { char -> char.isDigit() || char == '.' }
                if (filtered.count { char -> char == '.' } <= 1) onValueChange(filtered)
            },
            label = { Text(label, color = TextoGris) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ThousandSeparatorVisualTransformation(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = TextoBlanco,
                focusedIndicatorColor = BotonLila,
                unfocusedTextColor = TextoBlanco,
                focusedTextColor = TextoBlanco,
                cursorColor = BotonLila
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCalculate()
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = BotonLila, contentColor = Color.Black)
        ) {
            Text("🧮 Calcular")
        }
    }
}
