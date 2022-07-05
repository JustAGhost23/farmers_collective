package com.example.farmerscollective.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.farmerscollective.data.Prediction
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import android.net.ConnectivityManager
import android.widget.Toast
import com.example.farmerscollective.R
import java.time.LocalDate


class DataWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
            .isConnected
    }

    override fun doWork(): Result {

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        val array = applicationContext.resources.getStringArray(R.array.mandi)

        for(mandi in array) {
            db.collection(mandi)

                .get()
                .addOnSuccessListener {
                    val temp = mutableListOf<List<String>>()
                    Log.v("firebase", mandi)
//                        val doc = it.last()

                    it.forEachIndexed { i, doc ->

                        val prices = doc.data["data"] as ArrayList<HashMap<String, Any>>

                        try {
                            for(row in prices) {
                                temp.add(listOf(row["DATE"]!!.toString(), row["PRICE"]!!.toString()))
                            }

                            Log.d("debugging", temp.toString())

                            val file = File(applicationContext.filesDir, mandi)

                            csvWriter().open(file) {
                                for(row in temp) {
                                    writeRow(row)
                                }
                            }


                            if(array.indexOf(mandi) == array.size - 1) {
                                db.collection("TELANGANA_ADILABAD_Recommendation")
                                    .get()
                                    .addOnSuccessListener { res ->
                                        val document = res.last()

                                        val data = document.data["data"] as ArrayList<HashMap<String, Any>>
                                        val t = ArrayList<Prediction>()
                                        for(row in data) {
                                            t.add(Prediction(row["DATE"]!!.toString(), row["CONFIDENCE"]!!.toString().toFloat(), row["MEAN_PRICE"]!!.toString().toFloat(), row["PREDICTED"]!!.toString().toFloat(), row["MEAN_GAIN"]!!.toString().toFloat(), row["MEAN_LOSS"]!!.toString().toFloat()))
                                        }

                                        Log.d("debugging", t.toString())
                                        val file2 = File(applicationContext.filesDir, "predict.csv")

                                        csvWriter().open(file2) {
                                            for(row in t) {

                                                writeRow(row.date, row.confidence, row.predicted, row.output, row.gain, row.loss)
                                            }
                                        }

                                        val sharedPref = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                        with (sharedPref.edit()) {
                                            putBoolean("isDataAvailable", true)
                                            apply()
                                        }

                                    }
                                    .addOnFailureListener {
                                        Log.v("ViewModel", it.toString())
                                    }
                            }

                        }

                        catch(e: Exception) {
                            Log.d("Worker/$mandi", e.toString())
                        }

                    }

                }
                .addOnFailureListener {
                    Log.v("ViewModel", it.toString())

                }




        }



//            Toast.makeText(applicationContext, "Data loaded!", Toast.LENGTH_SHORT).show()
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}