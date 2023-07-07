package com.example.farmerscollective.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.checkerframework.checker.nullness.qual.NonNull

// Data Class for International price entries stored on Firebase
// Added annotations to store data in Room
@Entity(tableName = "int_price_table", primaryKeys = ["date", "cropId"])
data class IntPriceEntry(
    // Date
    @ColumnInfo(name = "date")
    val date: String,

    // Crop Id (Refer to internationalCropName array in strings.xml)
    @ColumnInfo(name = "cropId")
    val cropId: Int,

    // Price
    @ColumnInfo(name = "price")
    val price: Float
)
