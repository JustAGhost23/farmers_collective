package com.example.farmerscollective.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "int_price_table")
data class IntPriceEntry(
    @PrimaryKey(autoGenerate = false) val subId: Int,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "country") val country: String,
    @ColumnInfo(name = "price") val price: Float
)
