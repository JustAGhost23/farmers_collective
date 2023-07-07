package com.example.farmerscollective.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IntPriceDao {

    // SQL Query to get all price entries from the international prices table
    @Query("SELECT * FROM int_price_table")
    fun getAllPrices(): List<IntPriceEntry>

    // SQL Query to get all price entries of a specific crop from the international prices table
    @Query("SELECT * FROM int_price_table WHERE cropId = :cropId")
    fun getPricesByCropId(cropId: Int): List<IntPriceEntry>

    // SQL Query to upsert a price entry into the international prices table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPrice(intPriceEntry: IntPriceEntry)

    // SQL Query to delete all price entries from the international prices table
    @Query("DELETE FROM int_price_table")
    fun deleteAll()

}