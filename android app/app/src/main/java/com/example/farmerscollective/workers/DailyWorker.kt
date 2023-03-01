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


class DailyWorker(appContext: Context, workerParams: WorkerParameters) :
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
                        val temp = mutableListOf<List<String>>()
                        Log.v("firebase", mandi)
                        val doc = it.last()


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
                            logWorkerEvent(doc.id, "WorkerFailure")
                        }
                    }
                    .addOnFailureListener {
                        logWorkerEvent("failed", "WorkerFailure")

                    }

            }

            db.collection("MAHARASHTRA_NAGPUR_Recommendation")
                .get()
                .addOnSuccessListener { res ->
                    val last = res.last()
                    val list = res.toList()
                    for (document in list.subList(list.size - 731, list.size)) {
                        val data =
                            document.data["data"] as ArrayList<HashMap<String, Any>>
                        val t = ArrayList<Prediction>()
                        for (row in data) {
                            t.add(
                                Prediction(
                                    row["DATE"]!!.toString(),
                                    row["CONFIDENCE"]!!.toString().toFloat(),
                                    row["MEAN_PRICE"]!!.toString().toFloat(),
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

                            val today =
                                File(applicationContext.filesDir, "dailyPredict.csv")


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
                                putBoolean("isDailyDataAvailable", true)
                                apply()
                            }

                            logWorkerEvent("daily", "WorkerSuccess")
                        }

                    }


                }
                .addOnFailureListener {
                    logWorkerEvent("daily", "WorkerFailure")
                }

            db.collection("weekly_MAHARASHTRA_NAGPUR_Recommendation")
                .get()
                .addOnSuccessListener { res ->
                    val last = res.last()
                    val list = res.toList()
                    for (document in list.subList(0, list.size)) {
                        if(document.id.substring(0,4).toInt() <= LocalDate.now().year && document.id.substring(0,4).toInt() >= LocalDate.now().year - 2) {
                            Log.e("TAG", document.id.substring(0,4).toInt().toString())
                            val data =
                                document.data["data"] as ArrayList<HashMap<String, Any>>
                            val t = ArrayList<Prediction>()
                            for (row in data) {
                                t.add(
                                    Prediction(
                                        row["DATE"]!!.toString(),
                                        row["CONFIDENCE"]!!.toString().toFloat(),
                                        row["MEAN_PRICE"]!!.toString().toFloat(),
                                        row["PREDICTED"]!!.toString().toFloat(),
                                        row["MEAN_GAIN"]!!.toString().toFloat(),
                                        row["MEAN_LOSS"]!!.toString().toFloat()
                                    )
                                )
                            }
                            Log.e("TAG", t.toString())

                            val file2 = File(
                                applicationContext.filesDir,
                                "weekly_predict_${document.id}.csv"
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

                                val thisWeek =
                                    File(applicationContext.filesDir, "weeklyPredict.csv")


                                csvWriter().open(thisWeek) {
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
                                    putBoolean("isWeeklyDataAvailable", true)
                                    apply()
                                }

                                logWorkerEvent("refresh", "WorkerSuccess")
                            }
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
                        }
                    }
                    val file = File(
                        applicationContext.filesDir,
                        "TELANGANA_ADILABAD_ODK.csv"
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

//            Toast.makeText(applicationContext, "Data loaded!", Toast.LENGTH_SHORT).show()
            // Indicate whether the work finished successfully with the Result

        }

        return Result.success()

    }

    private fun logWorkerEvent(id: String, event: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "worker-${id}")
        bundle.putString("type", "DailyWorker")
        analytics.logEvent(event, bundle)
    }
}