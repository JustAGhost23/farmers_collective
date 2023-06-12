package com.example.farmerscollective.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.checkerframework.checker.nullness.qual.NonNull

@Entity(tableName = "int_price_table", primaryKeys = ["date", "cropId"])
data class IntPriceEntry(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "cropId") val cropId: Int,
    @ColumnInfo(name = "price") val price: Float
)
