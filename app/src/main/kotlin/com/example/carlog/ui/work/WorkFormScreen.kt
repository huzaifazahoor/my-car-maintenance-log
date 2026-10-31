@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.carlog.ui.work

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carlog.data.AppDatabase
import com.example.carlog.data.WorkEntry
import com.example.carlog.ui.common.ConfirmDeleteDialog
import com.example.carlog.ui.common.formatDate
import com.example.carlog.ui.common.todayUtcMillis
import kotlinx.coroutines.launch

// Tap one to fill "Work type". You can still type anything.
private val QUICK_TYPES = listOf("Oil change", "Tuning", "Oil filter", "Air filter", "Brakes", "Tyres")

@Composable
fun WorkFormScreen(carId: Long, entryId: Long?, onDone: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).workEntryDao() }
    val scope = rememberCoroutineScope()
    val isEdit = entryId != null

    var loaded by rememberSaveable { mutableStateOf(!isEdit) }
    var workType by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableLongStateOf(todayUtcMillis()) }
    var km by rememberSaveable { mutableStateOf("") }
    var mechanic by rememberSaveable { mutableStateOf("") }
    var cost by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    // When editing, fill the form once from the database.
    LaunchedEffect(entryId) {
        if (entryId != null && !loaded) {
            dao.getById(entryId)?.let { e ->
                workType = e.workType
                date = e.date
                km = e.km?.toString() ?: ""
                mechanic = e.mechanic
                cost = e.cost?.let { c -> if (c % 1.0 == 0.0) c.toLong().toString() else c.toString() } ?: ""
                details = e.details
            }
            loaded = true
        }
    }

    fun buildEntry() = WorkEntry(
        id = entryId ?: 0,
        carId = carId,
        workType = workType.trim(),
        date = date,
        km = km.toIntOrNull(),
        mechanic = mechanic.trim(),
        cost = cost.toDoubleOrNull(),
        details = details.trim(),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit work" else "Add work") },
                navigationIcon = { TextButton(onClick = onDone) { Text("Cancel") } },
                actions = {
                    TextButton(
                        enabled = loaded && workType.isNotBlank(),
                        onClick = {
                            val entry = buildEntry()
                            scope.launch {
                                if (isEdit) dao.update(entry) else dao.insert(entry)
                                onDone()
                            }
                        },
                    ) { Text("Save") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = workType,
                onValueChange = { workType = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Work type *") },
                singleLine = true,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QUICK_TYPES.forEach { type ->
                    FilterChip(
                        selected = workType == type,
                        onClick = { workType = type },
                        label = { Text(type) },
                    )
                }
            }
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Date: ${formatDate(date)}")
            }
            OutlinedTextField(
                value = km,
                onValueChange = { km = it.filter { c -> c.isDigit() }.take(7) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Odometer (km)") },
                placeholder = { Text("e.g. 154000") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = mechanic,
                onValueChange = { mechanic = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mechanic / shop") },
                singleLine = true,
            )
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cost (Rs)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Details") },
                placeholder = { Text("Which oil, parts changed, anything to remember") },
                minLines = 4,
            )
            if (isEdit) {
                TextButton(onClick = { showDelete = true }) {
                    Text("Delete this entry", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }

    if (showDelete) {
        ConfirmDeleteDialog(
            title = "Delete entry?",
            message = "This work entry will be removed. You can't undo this.",
            onDismiss = { showDelete = false },
            onConfirm = {
                showDelete = false
                val entry = buildEntry()
                scope.launch {
                    dao.delete(entry)
                    onDone()
                }
            },
        )
    }
}
