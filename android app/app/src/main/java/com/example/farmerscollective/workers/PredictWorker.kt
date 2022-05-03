package com.example.farmerscollective.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.farmerscollective.data.Prediction
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class PredictWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection("KOTA_Predictions")
            .get()
            .addOnSuccessListener {
                val doc = it.last()

                val data = doc.data["data"] as ArrayList<HashMap<String, Any>>
                val temp = ArrayList<Prediction>()
                for(row in data) {
                    temp.add(Prediction(row["DATE"]!!.toString(), row["CONFIDENCE"]!!.toString().toFloat(), row["PREDICTED"]!!.toString().toFloat()))
                }

                temp.sortBy { value -> value.output }
                val file = File(applicationContext.filesDir, "predict.csv")

                csvWriter().open(file) {
                    for(row in temp) {
                        writeRow(row.date, row.confidence, row.output)
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