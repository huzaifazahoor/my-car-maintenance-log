package com.example.carlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY name COLLATE NOCASE")
    fun getAll(): Flow<List<Car>>

    @Query("SELECT * FROM cars WHERE id = :id")
    fun getById(id: Long): Flow<Car?>

    @Insert
    suspend fun insert(car: Car): Long

    @Update
    suspend fun update(car: Car)

    @Delete
    suspend fun delete(car: Car)
}

@Dao
interface WorkEntryDao {
    // Newest first. Search, sort and date filter happen in the ViewModel.
    @Query("SELECT * FROM work_entries WHERE carId = :carId ORDER BY date DESC, id DESC")
    fun getForCar(carId: Long): Flow<List<WorkEntry>>

    @Query("SELECT * FROM work_entries WHERE id = :id")
    suspend fun getById(id: Long): WorkEntry?

    @Insert
    suspend fun insert(entry: WorkEntry): Long

    @Update
    suspend fun update(entry: WorkEntry)

    @Delete
    suspend fun delete(entry: WorkEntry)
}