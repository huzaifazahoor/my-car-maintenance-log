package com.example.carlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// One row per car. Example: Cultus, 2008, EFI, LEB 2970
@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val modelYear: Int?,
    val variant: String,
    val plate: String,
)

// One row per job done on a car.
// When a car is deleted, all its work entries are deleted too (CASCADE).
@Entity(
    tableName = "work_entries",
    foreignKeys = [
        ForeignKey(
            entity = Car::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("carId")],
)
data class WorkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val carId: Long,
    val workType: String,      // e.g. "Oil change", "Tuning"
    val date: Long,            // stored as epoch millis
    val km: Int?,              // optional odometer reading
    val mechanic: String,
    val cost: Double?,         // optional
    val details: String,
)