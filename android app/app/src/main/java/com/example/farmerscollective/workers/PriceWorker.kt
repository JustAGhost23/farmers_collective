package com.example.farmerscollective.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.farmerscollective.R
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.InputStream

class PriceWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val array = applicationContext.resources.getStringArray(R.array.mandi)

        for(mandi in array) {
            db.collection(mandi)

                .get()
                .addOnSuccessListener {
                    val temp = mutableListOf(listOf("DATE", "PRICE"))
                    Log.v("firebase", mandi)
                    val doc = it.last()
                    val prices = doc.data["data"] as ArrayList<HashMap<String, Any>>

                    try {
                        for(row in prices) {
                            temp.add(listOf(row["DATE"]!!.toString(), row["PRICE"]!!.toString()))
                        }

                        Log.d("debugging", temp.toString())

                        val file = File(applicationContext.filesDir, "$mandi.csv")

                        csvWriter().open(file) {
                            for(row in temp) {
                                writeRow(row)
                            }
                        }

                    }

                    catch(e: Exception) {
                        Log.d("Worker/$mandi", e.toString())
                    }


                }
                .addOnFailureListener {
                    Log.v("ViewModel", it.toString())
                }
        }

        Log.v("Worker", "Hogaya mera")

        // Indicate whether the work finished successfully with the Result
        return Result.success()

    }
}