@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.carlog.ui.cars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carlog.data.AppDatabase
import com.example.carlog.data.WorkEntry
import com.example.carlog.ui.common.ConfirmDeleteDialog
import com.example.carlog.ui.common.formatCost
import com.example.carlog.ui.common.formatDate
import com.example.carlog.ui.common.formatKm
import kotlinx.coroutines.launch

enum class SortOrder(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    KM_HIGH("Highest km"),
    TYPE("Work type A-Z"),
}

@Composable
fun CarDetailScreen(
    carId: Long,
    onBack: () -> Unit,
    onAddWork: () -> Unit,
    onOpenWork: (Long) -> Unit,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val car by remember(carId) { db.carDao().getById(carId) }
        .collectAsStateWithLifecycle(initialValue = null)
    val entries by remember(carId) { db.workEntryDao().getForCar(carId) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(SortOrder.NEWEST) }
    var fromDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var toDate by rememberSaveable { mutableStateOf<Long?>(null) }

    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Search + date filter + sort, all done in memory (fast enough for years of history).
    val shown = remember(entries, query, sort, fromDate, toDate) {
        val q = query.trim()
        val from = fromDate
        val to = toDate
        val filtered = entries
            .filter { e ->
                q.isEmpty() ||
                    e.workType.contains(q, ignoreCase = true) ||
                    e.mechanic.contains(q, ignoreCase = true) ||
                    e.details.contains(q, ignoreCase = true)
            }
            .filter { e -> (from == null || e.date >= from) && (to == null || e.date <= to) }
        when (sort) {
            SortOrder.NEWEST -> filtered.sortedWith(compareByDescending<WorkEntry> { it.date }.thenByDescending { it.id })
            SortOrder.OLDEST -> filtered.sortedWith(compareBy<WorkEntry> { it.date }.thenBy { it.id })
            SortOrder.KM_HIGH -> filtered.sortedByDescending { it.km ?: -1 }
            SortOrder.TYPE -> filtered.sortedBy { it.workType.lowercase() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work history") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = {
                    TextButton(onClick = { showEdit = true }) { Text("Edit") }
                    TextButton(onClick = { showDelete = true }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddWork) { Text("+ Add work") }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Car info
            item {
                car?.let { c ->
                    Column {
                        Text(c.title(), style = MaterialTheme.typography.headlineSmall)
                        if (c.plate.isNotBlank()) {
                            Text(
                                c.plate,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Search, sort, date filter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search type, mechanic, details") },
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.weight(1f)) {
                            OutlinedButton(onClick = { showSortMenu = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(sort.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                SortOrder.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        onClick = {
                                            sort = option
                                            showSortMenu = false
                                        },
                                    )
                                }
                            }
                        }
                        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                            Text(if (fromDate == null) "All dates" else "Dates set", maxLines = 1)
                        }
                    }
                    val from = fromDate
                    val to = toDate
                    if (from != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "From ${formatDate(from)} to ${formatDate(to ?: from)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = {
                                fromDate = null
                                toDate = null
                            }) { Text("Clear") }
                        }
                    }
                    Text(
                        "${shown.size} of ${entries.size} entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            when {
                entries.isEmpty() -> item {
                    Text(
                        "No work yet. Tap \"+ Add work\" to add the first entry.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
                shown.isEmpty() -> item {
                    Text(
                        "Nothing matches your search or dates.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
                else -> items(shown, key = { it.id }) { entry ->
                    WorkCard(entry = entry, onClick = { onOpenWork(entry.id) })
                }
            }
        }
    }

    if (showEdit) {
        car?.let { c ->
            CarDialog(
                car = c,
                onDismiss = { showEdit = false },
                onSave = { updated ->
                    scope.launch { db.carDao().update(updated) }
                    showEdit = false
                },
            )
        }
    }

    if (showDelete) {
        ConfirmDeleteDialog(
            title = "Delete car?",
            message = "This deletes ${car?.name ?: "this car"} and all of its work history. You can't undo this.",
            onDismiss = { showDelete = false },
            onConfirm = {
                showDelete = false
                val c = car
                if (c != null) {
                    scope.launch {
                        db.carDao().delete(c) // work history goes too (cascade)
                        onBack()
                    }
                }
            },
        )
    }

    if (showDatePicker) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = fromDate,
            initialSelectedEndDateMillis = toDate,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fromDate = state.selectedStartDateMillis
                    toDate = state.selectedEndDateMillis ?: state.selectedStartDateMillis
                    showDatePicker = false
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DateRangePicker(
                state = state,
                modifier = Modifier.weight(1f),
                title = { Text("Filter by date", Modifier.padding(start = 24.dp, top = 16.dp)) },
            )
        }
    }
}

@Composable
private fun WorkCard(entry: WorkEntry, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.workType,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    formatDate(entry.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val facts = listOfNotNull(
                entry.km?.let { formatKm(it) },
                entry.mechanic.ifBlank { null },
                entry.cost?.let { formatCost(it) },
            )
            if (facts.isNotEmpty()) {
                Text(
                    facts.joinToString("  |  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (entry.details.isNotBlank()) {
                Text(
                    entry.details,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
