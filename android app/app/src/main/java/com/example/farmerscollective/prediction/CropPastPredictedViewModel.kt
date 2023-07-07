package com.example.farmerscollective.prediction

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.farmerscollective.data.Prediction
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.time.LocalDate

// ViewModel for Crop Past Predicted Fragment
class CropPastPredictedViewModel(application: Application) : AndroidViewModel(application) {
    // Private variables
    val context = application


    private val _recomm = MutableLiveData<ArrayList<List<String>>>(arrayListOf())
    private val _graph = MutableLiveData<List<Map<String, Float>>>(listOf(mapOf(), mapOf()))
    private val _date = MutableLiveData<LocalDate>()
    private val _price = MutableLiveData(0.0f)
    private val _maxProf = MutableLiveData(Pair("N/A", 0.0f))

    // LiveData variables, getting data from above private variables
    val recomm: LiveData<ArrayList<List<String>>>
        get() = _recomm

    val graph: LiveData<List<Map<String, Float>>>
        get() = _graph

    val date: LiveData<LocalDate>
        get() = _date

    val maxProf: LiveData<Pair<String, Float>>
        get() = _maxProf

    val price: LiveData<Float>
        get() = _price


    // Code run when viewModel is initialized
    init {
        _date.value = LocalDate.now().minusDays(30)
        getPastRecomm()
    }

    // Function to get Past Recommendations
    private fun getPastRecomm() {

        // Initial Variables
        val date = _date.value!!
        val map1 = mutableMapOf<String, Float>()
        val map2 = mutableMapOf<String, Float>()
        var temp = ArrayList<Prediction>()
        var maxProf = Pair("N/A", 0.0f)

        // Obtain csv file
        val file = File(context.filesDir, "predict_${date}.csv")

        if (file.exists()) {
            // Add price data for current year from Nagpur
            val year = date.year
            val currYear = File(context.filesDir, "MAHARASHTRA_NAGPUR_Price_${year}")

            if (currYear.exists()) {
                csvReader().open(currYear) {

                    readAllAsSequence().forEach {
                        val dt = LocalDate.parse(it[0])
                        if (dt.isAfter(date.minusDays(1)) && dt.isBefore(date.plusDays(30))) {
                            if (dt.isEqual(date)) _price.value = it[1].toFloat()
                            map2[it[0]] = it[1].toFloat()
                            if (it[1].toFloat() > maxProf.second) {
                                maxProf = Pair(it[0], it[1].toFloat())
                            }
                        }
                    }

                }
            }

            // If there are less than 30 days left in the year, add data from csv file 1 year ahead
            if (date.plusDays(29).isAfter(LocalDate.of(year, 12, 31))) {
                val nextYear = File(context.filesDir, "MAHARASHTRA_NAGPUR_Price_${year + 1}")

                if (nextYear.exists()) {
                    csvReader().open(nextYear) {

                        readAllAsSequence().forEach {
                            val dt = LocalDate.parse(it[0])
                            if (dt.isAfter(date.minusDays(1)) && dt.isBefore(date.plusDays(30))) {
                                if (dt.isEqual(date)) _price.value = it[1].toFloat()
                                map2[it[0]] = it[1].toFloat()
                                if (it[1].toFloat() > maxProf.second) {
                                    maxProf = Pair(it[0], it[1].toFloat())
                                }
                            }
                        }

                    }
                }
            }

            // Read csv file and add data to a temp list
            csvReader().open(file) {

                readAllAsSequence().forEach {
//                    val dt = LocalDate.parse(it[0])
//                    if(dt.isAfter(date.minusDays(1)))
                    temp.add(
                        Prediction(
                            it[0],
                            it[1].toFloat(),
                            it[2].toFloat(),
                            it[3].toFloat(),
                            it[4].toFloat(),
                            it[5].toFloat()
                        )
                    )
                    map1[it[0]] = it[2].toFloat()
                }

            }


        }

        // Get max profit value
        _maxProf.value = if (map2.containsKey(_date.value!!.toString()))
            Pair(maxProf.first, maxProf.second - map2[_date.value!!.toString()]!!)
        else Pair("N/A", 0.0f)

        // Sort temp list
        temp.sortBy { value -> value.output }
        val values = arrayListOf<List<String>>()

        // Update values in recommendations with new recommendations
        if (temp.isNotEmpty()) {
            Log.v("ok", temp.toString())
            temp = ArrayList(temp.reversed().subList(1, 4))

            // for each value in temp, format accordingly and add into values to be added to recommendations
            temp.forEach { item ->
                val l = (1 - item.confidence)
                val g = item.confidence

                var profit = "N/A"
                if (map2.containsKey(item.date) && map2.containsKey(_date.value!!.toString())) profit =
                    (map2[item.date]!! - map2[_date.value!!.toString()]!!).toString()
                values.add(
                    listOf(
                        item.date,
                        l.toString(),
                        g.toString(),
                        (item.loss - map1[_date.value!!.toString()]!!).toString(),
                        (item.gain - map1[_date.value!!.toString()]!!).toString(),
                        profit
                    )
                )
            }

            _recomm.value = values
        } else {
            _recomm.value = ArrayList()
        }
        // Update values in graph list
        _graph.value = listOf(map1, map2)
    }

    // Function to change date and refresh Past Recommendations
    fun changeDate(newDate: LocalDate) {
        _date.value = if (newDate.isBefore(LocalDate.now().plusDays(1)))
            newDate
        else LocalDate.now()

        getPastRecomm()

    }
}