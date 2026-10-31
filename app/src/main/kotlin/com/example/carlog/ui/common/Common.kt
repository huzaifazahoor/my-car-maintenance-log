package com.example.carlog.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

// Dates are saved as midnight UTC, which is what the date picker gives us.
fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply { timeZone = UTC }.format(Date(millis))

fun todayUtcMillis(): Long {
    val now = Calendar.getInstance()
    return Calendar.getInstance(UTC).apply {
        clear()
        set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

fun formatKm(km: Int): String = String.format(Locale.getDefault(), "%,d km", km)

fun formatCost(cost: Double): String = String.format(Locale.getDefault(), "Rs %,.0f", cost)

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
