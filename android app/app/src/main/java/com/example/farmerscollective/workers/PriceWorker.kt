package com.example.farmerscollective.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.InputStream

class PriceWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection("KOTA_Prices")
            .get()
            .addOnSuccessListener {
                val doc = it.last()
                val prices = doc.data["data"] as ArrayList<HashMap<String, Any>>
                val temp = mutableListOf(listOf("DATE", "PRICE"))
                for(row in prices) {
                    temp.add(listOf(row["DATE"]!!.toString(), row["PRICE"]!!.toString()))
                }

                val file = File(applicationContext.filesDir, "prices.csv")

                csvWriter().open(file) {
                    for(row in temp) {
                        writeRow(row)
                    }
                }

                Log.v("Worker", "Done")

            }
            .addOnFailureListener {
                Log.v("ViewModel", it.toString())
            }
        // Indicate whether the work finished successfully with the Result
        return Result.success()

    }
}