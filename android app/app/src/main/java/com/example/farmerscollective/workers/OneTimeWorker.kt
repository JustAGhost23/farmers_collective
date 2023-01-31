package com.example.farmerscollective.workers

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.farmerscollective.R
import com.example.farmerscollective.data.OdkSubmission
import com.example.farmerscollective.data.Prediction
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class OneTimeWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val analytics = FirebaseAnalytics.getInstance(applicationContext)

    override suspend fun doWork(): Result {

        withContext(Dispatchers.IO) {
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()

            val array = applicationContext.resources.getStringArray(R.array.mandi)

            for (mandi in array) {
                db.collection(mandi)
                    .get()
                    .addOnSuccessListener {

                        Log.v("firebase", mandi)
//                        val doc = it.last()
                        it.forEachIndexed { i, doc ->

                            val temp = mutableListOf<List<String>>()
                            val prices = doc.data["data"] as ArrayList<HashMap<String, Any>>

                            try {
                                for (row in prices) {
                                    temp.add(
                                        listOf(
                                            row["DATE"]!!.toString(),
                                            row["PRICE"]!!.toString()
                                        )
                                    )
                                }

                                Log.d("debugging", temp.toString())

                                val file = File(applicationContext.filesDir, "${mandi}_${doc.id}")

                                csvWriter().open(file) {
                                    for (row in temp) {
                                        writeRow(row)
                                    }
                                }

                            } catch (e: Exception) {
                                Log.d("Worker/$mandi", e.toString())
                            }

                        }

                    }
                    .addOnFailureListener {
                        Log.v("ViewModel", it.toString())

                    }

            }

            db.collection("MAHARASHTRA_NAGPUR_Recommendation")
                .get()
                .addOnSuccessListener { res ->
                    val last = res.last()
                    val list = res.toList()
                    for (document in list.subList(
                        list.size - 731,
                        list.size
                    )) {
                        val data =
                            document.data["data"] as ArrayList<HashMap<String, Any>>
                        val t = ArrayList<Prediction>()
                        for (row in data) {
                            t.add(
                                Prediction(
                                    row["DATE"]!!.toString(),
                                    row["CONFIDENCE"]!!.toString()
                                        .toFloat(),
                                    row["MEAN_PRICE"]!!.toString()
                                        .toFloat(),
                                    row["PREDICTED"]!!.toString().toFloat(),
                                    row["MEAN_GAIN"]!!.toString().toFloat(),
                                    row["MEAN_LOSS"]!!.toString().toFloat()
                                )
                            )
                        }

                        val file2 = File(
                            applicationContext.filesDir,
                            "predict_${document.id}.csv"
                        )


                        csvWriter().open(file2) {
                            for (row in t) {

                                writeRow(
                                    row.date,
                                    row.confidence,
                                    row.predicted,
                                    row.output,
                                    row.gain,
                                    row.loss
                                )
                            }
                        }

                        if (document == last) {

                            val today = File(
                                applicationContext.filesDir,
                                "predict.csv"
                            )


                            csvWriter().open(today) {
                                for (row in t) {

                                    writeRow(
                                        row.date,
                                        row.confidence,
                                        row.predicted,
                                        row.output,
                                        row.gain,
                                        row.loss
                                    )
                                }
                            }

                            val sharedPref =
                                applicationContext.getSharedPreferences(
                                    "prefs",
                                    Context.MODE_PRIVATE
                                )
                            with(sharedPref.edit()) {
                                putBoolean("isDataAvailable", true)
                                apply()
                            }

                            logWorkerEvent("refresh", "WorkerSuccess")
                        }

                    }


                }
                .addOnFailureListener {
                    logWorkerEvent("failure", "WorkerFailure")
                }

            db.collection("TELANGANA_ADILABAD_ODK")
                .get()
                .addOnSuccessListener {
                    val t: ArrayList<OdkSubmission> = arrayListOf()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    it.documents.forEach {doc ->
                        doc.data?.forEach {
                            val mapArray: ArrayList<HashMap<String, Any>> = it.value as ArrayList<HashMap<String, Any>>
                            for(map in mapArray) {
                                if(map.containsKey("CROP_NAME")) {
                                    val m: HashMap<String, Any> = map["CROP_NAME"] as HashMap<String, Any>
                                    t.add(
                                        OdkSubmission(
                                            if(m["LOCAL_TRADER_ID"].toString() != "null") Integer.parseInt(m["LOCAL_TRADER_ID"].toString()) else null,
                                            m["MANDAL_ID"] as String?,
                                            if(m["MARKET_ID"].toString() != "null") Integer.parseInt(m["MARKET_ID"].toString()) else null,
                                            m["PERSON_FILLING_DATA_ID"] as String?,
                                            m["RATE_OFFERED_ID"] as Long,
                                            LocalDate.parse(map["DATE_CT_ID"].toString(), formatter),
                                        )
                                    )
                                }
                            }
                            val file = File(
                                applicationContext.filesDir,
                                "${doc.id}.csv"
                            )
                            csvWriter().open(file) {
                                for (row in t) {

                                    writeRow(
                                        row.localTraderId,
                                        row.mandalId,
                                        row.marketId,
                                        row.personFillingId,
                                        row.price,
                                        row.date,
                                    )
                                }
                            }
                            Log.e("debugging", t.toString())
                        }
                    }
                }

        }

        return Result.success()

    }

    private fun logWorkerEvent(id: String, event: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "worker-${id}")
        bundle.putString("type", "OneTimeWorker")
        analytics.logEvent(event, bundle)
    }
}