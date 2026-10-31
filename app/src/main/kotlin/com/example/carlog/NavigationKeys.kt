package com.example.carlog

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Each screen in the app is one "key" on the back stack.
@Serializable data object CarList : NavKey

@Serializable data class CarDetail(val carId: Long) : NavKey

// entryId = null means "add new", a number means "edit this one".
@Serializable data class WorkForm(val carId: Long, val entryId: Long? = null) : NavKey
