package com.example.farmerscollective.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IntPriceDao {

    @Query("SELECT * FROM int_price_table")
    fun getAllPrices(): List<IntPriceEntry>

    @Query("SELECT * FROM int_price_table WHERE cropId = :cropId")
    fun getPricesByCropId(cropId: Int): List<IntPriceEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPrice(intPriceEntry: IntPriceEntry)

    @Query("DELETE FROM int_price_table")
    fun deleteAll()

}