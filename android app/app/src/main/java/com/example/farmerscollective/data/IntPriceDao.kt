package com.example.farmerscollective.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IntPriceDao {

    @Query("SELECT * FROM int_price_table")
    fun getAllPrices(): LiveData<List<IntPriceEntry>>

    @Query("SELECT * FROM int_price_table WHERE country = :country")
    fun getPricesByCountry(country: String): LiveData<List<IntPriceEntry>>

    @Insert
    fun insertPrice(intPriceEntry: IntPriceEntry)

    @Query("DELETE FROM int_price_table")
    fun deleteAll()

}