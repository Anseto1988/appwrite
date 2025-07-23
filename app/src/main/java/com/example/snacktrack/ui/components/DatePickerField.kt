package com.example.snacktrack.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.snacktrack.utils.DateUtils
import java.time.LocalDate

/**
 * TextField mit DatePicker für die Auswahl eines Datums
 */
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Geburtsdatum",
    placeholder: String = "Datum auswählen",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Konvertiere String zu LocalDate falls vorhanden
    val currentDate = remember(value) {
        if (value.isNotBlank()) {
            DateUtils.parseGermanDate(value)
        } else {
            null
        }
    }

    // Öffne DatePicker wenn das Feld angeklickt wird
    LaunchedEffect(isPressed) {
        if (isPressed) {
            showDatePicker = true
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = { /* Read-only - nur über DatePicker änderbar */ },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier,
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Datum auswählen"
            )
        },
        isError = isError,
        supportingText = {
            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        singleLine = true,
        interactionSource = interactionSource
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                val germanDate = DateUtils.formatToGerman(date)
                onValueChange(germanDate)
                showDatePicker = false
            },
            onDismissRequest = {
                showDatePicker = false
            },
            initialDate = currentDate
        )
    }
}
