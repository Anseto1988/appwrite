package com.example.snacktrack.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.snacktrack.utils.DateUtils

/**
 * TextField für deutsche Datumseingabe mit automatischer Formatierung
 */
@Composable
fun GermanDateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Geburtsdatum",
    placeholder: String = "TT.MM.JJJJ",
    isError: Boolean = false,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Automatische Formatierung während der Eingabe
            val formatted = DateUtils.formatInputWhileTyping(newValue)
            onValueChange(formatted)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        isError = isError,
        supportingText = {
            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "Format: TT.MM.JJJJ (z.B. 15.03.2020)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        singleLine = true
    )
}
