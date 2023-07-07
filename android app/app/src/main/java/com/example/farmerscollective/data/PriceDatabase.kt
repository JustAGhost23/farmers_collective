package com.example.farmerscollective.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Create Room Database
@Database(entities = [IntPriceEntry::class], version = 1, exportSchema = false)
public abstract class PriceDatabase : RoomDatabase() {

    // Dao for International Prices
    abstract fun intPriceDao(): IntPriceDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: PriceDatabase? = null

        fun getDatabase(context: Context): PriceDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PriceDatabase::class.java,
                    "price_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}