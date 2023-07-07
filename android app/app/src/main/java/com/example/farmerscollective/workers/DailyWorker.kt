package com.example.farmerscollective.workers

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.farmerscollective.R
import com.example.farmerscollective.data.IntPriceEntry
import com.example.farmerscollective.data.OdkSubmission
import com.example.farmerscollective.data.Prediction
import com.example.farmerscollective.data.PriceDatabase
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.common.math.DoubleMath
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// CoroutineWorker that runs every 24 hours, thus refreshing data stored on the device.
// Data is pulled from firebase and put into CSV Files/Room Database.
class DailyWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    // Firebase Analytics instance
    private val analytics = FirebaseAnalytics.getInstance(applicationContext)

    // International Prices Dao
    private val dao = PriceDatabase.getDatabase(applicationContext).intPriceDao()

    override suspend fun doWork(): Result {

        withContext(Dispatchers.IO) {
            // Firebase Firestore Instance
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()

            // List of Mandis (from strings.xml)
            val array = applicationContext.resources.getStringArray(R.array.mandi)

            // Below code gets data from firebase regarding daily prices of crops.
            // This data is then added to CSV Files, with the name "${mandi}_${doc.id}"
            // mandi -> Name of mandi whose crop price data is added.
            // doc.id -> Specific year whose crop price data is added.
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

                            // Writing data to csv file
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

            // Below code gets data from firebase regarding daily price recommendations for Nagpur for the past 2 years.
            // This data is then added to CSV Files, where file naming is as follows:
            // 1) Today's prediction -> "dailyPredict.csv" (checking if document from firebase is the last one in the list)
            // 2) Past Predictions -> "predict_${document.id}.csv" (where document.id is the date of the prediction)
            // Past Predictions also includes today's prediction with date
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
                        // Writing data to csv file
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

                            // This stores a boolean variable in shared preferences indicating daily prediction data is available
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


            // Below code gets data from firebase regarding weekly price recommendations for Nagpur for the past 2 years.
            // This data is then added to CSV Files, where file naming is as follows:
            // 1) This week's prediction -> "weeklyPredict.csv" (checking if document from firebase is the last one in the list)
            // 2) Past Predictions -> "weekly_predict_${document.id}.csv" (where document.id is the date of the prediction)
            // Past Predictions also includes this week's prediction with date
            db.collection("weekly_MAHARASHTRA_NAGPUR_Recommendation")
                .get()
                .addOnSuccessListener { res ->
                    val last = res.last()
                    val list = res.toList()
                    for (document in list.subList(0, list.size)) {
                        if (document.id.substring(0, 4)
                                .toInt() <= LocalDate.now().year && document.id.substring(0, 4)
                                .toInt() >= LocalDate.now().year - 2
                        ) {
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

                            // Writing data to csv file
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

                                // This stores a boolean variable in shared preferences indicating weekly prediction data is available
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

            // Below code gets data from firebase regarding international crop prices.
            // This data is stored directly into a room database under the "int_prices_table" table.
            db.collection("Trading_Prices")
                .get()
                .addOnSuccessListener { it ->
                    val t: ArrayList<IntPriceEntry> = arrayListOf()
                    it.documents.forEach { doc ->
                        doc.data?.forEach {
                            val mapArray: ArrayList<HashMap<String, Any>> =
                                it.value as ArrayList<HashMap<String, Any>>
                            for (map in mapArray) {
                                t.add(
                                    IntPriceEntry(
                                        map["date"] as String,
                                        1,
                                        map["soyabean"].toString().toFloat(),
                                    )
                                )
                                t.add(
                                    IntPriceEntry(
                                        map["date"] as String,
                                        2,
                                        map["cotton"].toString().toFloat(),
                                    )
                                )
                            }
                        }
                    }
                    // Adding data to room database
                    CoroutineScope(Dispatchers.IO).launch {
                        for (priceEntry in t) {
                            dao.insertPrice(priceEntry)
                        }
                    }
                }


            // Below code gets data from firebase regarding ODK Submissions for Adilabad.
            // All data obtained is added to the "TELANGANA_ADILABAD_ODK.csv" file.
            // For different crops stored on Firebase,
            // "CROP_NAME" -> Soybean
            // "CROP_NAME_1" -> Red Gram
            // "CROP_NAME_2" -> Cotton
            // For other details for each ODKSubmission object field, refer to the data class.
            db.collection("TELANGANA_ADILABAD_ODK")
                .get()
                .addOnSuccessListener { it ->
                    val t: ArrayList<OdkSubmission> = arrayListOf()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    it.documents.forEach { doc ->
                        doc.data?.forEach {
                            val mapArray: ArrayList<HashMap<String, Any>> =
                                it.value as ArrayList<HashMap<String, Any>>
                            for (map in mapArray) {
                                if (map.containsKey("CROP_NAME")) {
                                    // Soybean
                                    val m: HashMap<String, Any> =
                                        map["CROP_NAME"] as HashMap<String, Any>
                                    if (m["RATE_OFFERED_ID"] != null) {
                                        t.add(
                                            OdkSubmission(
                                                1,
                                                if (m["LOCAL_TRADER_ID"].toString() != "null") Integer.parseInt(
                                                    m["LOCAL_TRADER_ID"].toString()
                                                ) else -1,
                                                m["MANDAL_ID"] as String?,
                                                if (m["MARKET_ID"].toString() != "null") Integer.parseInt(
                                                    m["MARKET_ID"].toString()
                                                ) else -1,
                                                m["PERSON_FILLING_DATA_ID"] as String?,
                                                m["RATE_OFFERED_ID"] as Long,
                                                LocalDate.parse(
                                                    map["DATE_CT_ID"].toString(),
                                                    formatter
                                                ),
                                            )
                                        )
                                    }
                                } else if (map.containsKey("CROP_NAME_1")) {
                                    // Red Gram
                                    val m: HashMap<String, Any> =
                                        map["CROP_NAME_1"] as HashMap<String, Any>
                                    if (m["RATE_OFFERED_1_ID"].toString() != "null") {
                                        t.add(
                                            OdkSubmission(
                                                2,
                                                if (m["LOCAL_TRADER_1_ID"].toString() != "null") Integer.parseInt(
                                                    m["LOCAL_TRADER_1_ID"].toString()
                                                ) else -1,
                                                m["MANDAL_1_ID"] as String?,
                                                if (m["MARKET_1_ID"].toString() != "null") Integer.parseInt(
                                                    m["MARKET_1_ID"].toString()
                                                ) else -1,
                                                m["PERSON_FILLING_DATA_1_ID"] as String?,
                                                m["RATE_OFFERED_1_ID"] as Long,
                                                LocalDate.parse(
                                                    map["DATE_CT_1_ID"].toString(),
                                                    formatter
                                                ),
                                            )
                                        )
                                    }
                                } else if (map.containsKey("CROP_NAME_2")) {
                                    // Cotton
                                    val m: HashMap<String, Any> =
                                        map["CROP_NAME_2"] as HashMap<String, Any>
                                    if (m["RATE_OFFERED_2_ID"] != null) {
                                        t.add(
                                            OdkSubmission(
                                                3,
                                                if (m["LOCAL_TRADER_2_ID"].toString() != "null") Integer.parseInt(
                                                    m["LOCAL_TRADER_2_ID"].toString()
                                                ) else -1,
                                                m["MANDAL_2_ID"] as String?,
                                                if (m["MARKET_2_ID"].toString() != "null") Integer.parseInt(
                                                    m["MARKET_2_ID"].toString()
                                                ) else -1,
                                                m["PERSON_FILLING_DATA_2_ID"] as String?,
                                                m["RATE_OFFERED_2_ID"] as Long,
                                                LocalDate.parse(
                                                    map["DATE_CT_2_ID"].toString(),
                                                    formatter
                                                ),
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Writing data to csv file
                    val file = File(
                        applicationContext.filesDir,
                        "TELANGANA_ADILABAD_ODK.csv"
                    )
                    csvWriter().open(file) {
                        for (row in t) {

                            writeRow(
                                row.cropId,
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

        return Result.success()

    }

    // Below function logs data to firebase analytics for every log event in the worker
    private fun logWorkerEvent(id: String, event: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "worker-${id}")
        bundle.putString("type", "DailyWorker")
        analytics.logEvent(event, bundle)
    }
}