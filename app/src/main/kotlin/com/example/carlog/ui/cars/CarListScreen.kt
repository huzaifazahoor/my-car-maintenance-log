@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.carlog.ui.cars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carlog.data.AppDatabase
import com.example.carlog.data.Car
import kotlinx.coroutines.launch

// "Cultus 2008 EFI"
fun Car.title(): String =
    listOf(name, modelYear?.toString() ?: "", variant).filter { it.isNotBlank() }.joinToString(" ")

@Composable
fun CarListScreen(onOpenCar: (Long) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val cars by remember { db.carDao().getAll() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Car Log") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAdd = true }) { Text("+ Add car") }
        },
    ) { padding ->
        if (cars.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No cars yet. Tap \"+ Add car\" to add your first one.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(cars, key = { it.id }) { car ->
                    OutlinedCard(onClick = { onOpenCar(car.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(car.title(), style = MaterialTheme.typography.titleMedium)
                            if (car.plate.isNotBlank()) {
                                Text(
                                    car.plate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        CarDialog(
            car = null,
            onDismiss = { showAdd = false },
            onSave = { newCar ->
                scope.launch { db.carDao().insert(newCar) }
                showAdd = false
            },
        )
    }
}

// Used for both "Add car" (car = null) and "Edit car".
@Composable
fun CarDialog(car: Car?, onDismiss: () -> Unit, onSave: (Car) -> Unit) {
    var name by rememberSaveable { mutableStateOf(car?.name ?: "") }
    var year by rememberSaveable { mutableStateOf(car?.modelYear?.toString() ?: "") }
    var variant by rememberSaveable { mutableStateOf(car?.variant ?: "") }
    var plate by rememberSaveable { mutableStateOf(car?.plate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (car == null) "Add car" else "Edit car") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    placeholder = { Text("Cultus") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Model year") },
                    placeholder = { Text("2008") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = variant,
                    onValueChange = { variant = it },
                    label = { Text("Variant") },
                    placeholder = { Text("EFI") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it.uppercase() },
                    label = { Text("Plate number") },
                    placeholder = { Text("LEB 2970") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        Car(
                            id = car?.id ?: 0,
                            name = name.trim(),
                            modelYear = year.toIntOrNull(),
                            variant = variant.trim(),
                            plate = plate.trim(),
                        )
                    )
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
