package com.example.farmerscollective.realtime

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.farmerscollective.R
import com.example.farmerscollective.utils.Utils.Companion.colors
import com.example.farmerscollective.utils.Utils.Companion.dates
import com.example.farmerscollective.utils.Utils.Companion.yearColors
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.util.*

// Crop Prices ViewModel
class CropPricesViewModel(application: Application) : AndroidViewModel(application) {

    // Private variables
    private val context = application
    private val _dataByYear = MutableLiveData<ArrayList<ILineDataSet>>()
    private val _dataByMandi = MutableLiveData<ArrayList<ILineDataSet>>()
    private val _selectedYears = MutableLiveData(arrayListOf<Int>())
    private val _mandi = MutableLiveData("MAHARASHTRA_NAGPUR_Price")
    private val _selectedMandis = MutableLiveData(arrayListOf("MAHARASHTRA_NAGPUR_Price"))
    private val _selectedMandisClean = MutableLiveData(arrayListOf("Maharashtra - Nagpur"))
    private val _year = MutableLiveData<Int>()
    private val _trends = MutableLiveData<Map<Int, List<Pair<String, Float>>>>(mutableMapOf())

    // LiveData variables, which get data from private variables above
    val dataByYear: LiveData<ArrayList<ILineDataSet>>
        get() = _dataByYear

    val dataByMandi: LiveData<ArrayList<ILineDataSet>>
        get() = _dataByMandi

    val trends: LiveData<Map<Int, List<Pair<String, Float>>>>
        get() = _trends

    val year: LiveData<Int>
        get() = _year

    private val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1)))
        LocalDate.now().year - 1
    else LocalDate.now().year

    // Code that is run when viewModel is initialized
    init {

        _selectedYears.value = arrayListOf(current)
        _year.value = current

        getDataByYear()
        getDataByMandi()
        getTrends(current)

    }

    // Function to get trends for a particular year
    private fun getTrends(current: Int) {
        val file1 = File(context.filesDir, "${_mandi.value}_${current}")
        val file2 = File(context.filesDir, "${_mandi.value}_${current + 1}")
        var min1 = Pair("", Float.MAX_VALUE)
        var min2 = Pair("", Float.MAX_VALUE)
        var max1 = Pair("", Float.MIN_VALUE)
        var max2 = Pair("", Float.MIN_VALUE)

        // Obtain maximum and minimum prices for a year
        if (file1.exists()) {
            csvReader().open(file1) {
                readAllAsSequence().forEachIndexed { i, it ->
                    val p = it[1].toFloat()
                    val m = it[0].substring(5, 7).toInt()
                    if (m > 6) {
                        if (min1.second > p) min1 =
                            Pair(it[0], p)
                        if (max1.second < p) max1 =
                            Pair(it[0], p)
                    }
                }
            }
        }

        if (file2.exists()) {
            csvReader().open(file2) {
                readAllAsSequence().forEachIndexed { i, it ->
                    val p = it[1].toFloat()
                    val m = it[0].substring(5, 7).toInt()

                    if (m < 7) {
                        if (min2.second > p) min2 =
                            Pair(it[0], p)
                        if (max2.second < p) max2 =
                            Pair(it[0], p)
                    }
                }
            }
        }

        // Filter the required data into the map
        val list = mutableListOf<Pair<String, Float>>()
        val dataMap = _trends.value as MutableMap<Int, List<Pair<String, Float>>>
        val min: Pair<String, Float> = if (min1.second < min2.second) {
            min1
        } else {
            min2
        }
        val max: Pair<String, Float> = if (max1.second > max2.second) {
            max1
        } else {
            max2
        }
        list.add(min)
        list.add(max)
        dataMap[current + 1] = list

        Log.e("abc", dataMap.toString())
        // Update trends value
        _trends.value = dataMap
    }

    // Function to get crop price data for a given year
    private fun getDataByYear() {

        val temp = ArrayList<ILineDataSet>()
        val trendsMap = mutableMapOf<Int, List<Pair<String, Float>>>()

        if (context.fileList().isNotEmpty()) {
            val mandi = _mandi.value!!
            val range = _selectedYears.value!!

            for (year in range) {
                // Select required files and add variables
                var min = Pair("", Float.MAX_VALUE)
                var max = Pair("", Float.MIN_VALUE)
                val file1 = File(context.filesDir, "${mandi}_${year}")
                val file2 = File(context.filesDir, "${mandi}_${year + 1}")
                val prices = mutableMapOf<String, Float>()

                if (file1.exists()) {
                    csvReader().open(file1) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            val start = LocalDate.of(year, 6, 30)
                            if (date.isAfter(start)) prices[ddmm] = it[1].toFloat()

                            val p = it[1].toFloat()
                            val m = it[0].substring(5, 7).toInt()
                            if (min.second > p) min =
                                Pair(DateFormatSymbols().months[m - 1] + " $year", p)
                            if (max.second < p) max =
                                Pair(DateFormatSymbols().months[m - 1] + " $year", p)

                        }
                    }
                }

                if (file2.exists()) {
                    csvReader().open(file2) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            val end = LocalDate.of(year + 1, 7, 1)

                            if (date.isBefore(end)) prices[ddmm] = it[1].toFloat()

                            val p = it[1].toFloat()
                            val m = it[0].substring(5, 7).toInt()
                            if (min.second > p) min =
                                Pair(DateFormatSymbols().months[m - 1] + " $year", p)
                            if (max.second < p) max =
                                Pair(DateFormatSymbols().months[m - 1] + " $year", p)

                        }
                    }
                }

                trendsMap[year] = listOf(min, max)
                val values1 = ArrayList<Entry>()
                val maxMap = mutableMapOf<Int, Pair<String, Float>>()

                for (date in dates) {
                    val i = dates.indexOf(date)

                    if (prices.containsKey(date) && !prices[date]!!.equals(0f)) {
                        values1.add(Entry(i.toFloat(), prices[date]!!))

                        val m = date.substring(3).toInt()
                        if (!maxMap.containsKey(m)) maxMap[m] = Pair(date, prices[date]!!)
                        else if (maxMap[m]!!.second < prices[date]!!) maxMap[m] =
                            Pair(date, prices[date]!!)

                    }

                }

                val values2 = ArrayList<Entry>()

                for (pair in maxMap.values) {
                    values2.add(Entry(dates.indexOf(pair.first).toFloat(), pair.second))
                }

                val dataset1 = LineDataSet(
                    values1,
                    year.toString() + "-" + (year + 1).toString().substring(2, 4)
                )
                dataset1.setDrawCircles(false)
                dataset1.color = yearColors[year]!!
                dataset1.lineWidth = 2f

                val dataset2 = LineDataSet(values2, "")
                dataset2.color = Color.TRANSPARENT
                dataset2.setDrawValues(false)
                dataset2.circleRadius = 5f
                dataset2.circleHoleRadius = 3f
                dataset2.setCircleColor(yearColors[year]!!)

                temp.add(dataset1)
                temp.add(dataset2)

            }

            _dataByYear.value = temp
        }
    }

    // Function to get crop price data for a given mandi
    private fun getDataByMandi() {

        val temp = ArrayList<ILineDataSet>()

        if (context.fileList().isNotEmpty()) {
            val array = _selectedMandis.value
            val arrayClean = _selectedMandisClean.value
            val year = _year.value!!

            val start = LocalDate.of(year, 6, 30)
            val end = LocalDate.of(year + 1, 7, 1)

            for (mandi in array!!) {
                val mandiClean = arrayClean?.get(array.indexOf(mandi))
                val file1 = File(context.filesDir, "${mandi}_${year}")
                val file2 = File(context.filesDir, "${mandi}_${year + 1}")

                val prices = mutableMapOf<String, Float>()

                if (file1.exists()) {
                    csvReader().open(file1) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            if (date.isAfter(start)) prices[ddmm] = it[1].toFloat()

                        }
                    }
                }

                if (file2.exists()) {
                    csvReader().open(file2) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            if (date.isBefore(end)) prices[ddmm] = it[1].toFloat()

                        }
                    }
                }

                val values1 = ArrayList<Entry>()
                val maxList = MutableList(12) { Pair("", 0.0f) }

                for (date in dates) {
                    val i = dates.indexOf(date)

                    if (prices.containsKey(date) && prices[date] != 0f) {
                        values1.add(Entry(i.toFloat(), prices[date]!!))

                        val m = date.substring(3).toInt() - 1
                        if (maxList[m].second < prices[date]!!) maxList[m] =
                            Pair(date, prices[date]!!)

                    }

                }

                val values2 = ArrayList<Entry>()

                for (pair in maxList) {
                    if (pair.second != 0f) values2.add(
                        Entry(
                            dates.indexOf(pair.first).toFloat(),
                            pair.second
                        )
                    )
                }

                val mandiColors = mutableMapOf<String, Int>()

                context.resources.getStringArray(R.array.mandi).forEachIndexed { i, str ->
                    mandiColors[str] = Color.parseColor(colors[i])
                }

                Collections.sort(values1, EntryXComparator())
                Collections.sort(values2, EntryXComparator())

                val dataset1 = LineDataSet(values1, mandiClean)
                dataset1.setDrawCircles(false)
                dataset1.color = mandiColors[mandi]!!
                dataset1.lineWidth = 2f

                temp.add(dataset1)

                val dataset2 = LineDataSet(values2, "")
                dataset2.color = Color.TRANSPARENT
                dataset2.setDrawValues(false)
                dataset2.circleRadius = 5f
                dataset2.circleHoleRadius = 3f
                dataset2.setCircleColor(mandiColors[mandi]!!)

                temp.add(dataset2)

            }

            _dataByMandi.value = temp
        }

    }

    // Function to select a year from chip (updates both graph and trends table)
    fun selectYear(year: Int, check: Boolean) {
        val newList = _selectedYears.value!!

        if (check) {
            if (!newList.contains(year)) {
                newList.add(year)
                getTrends(year)
            }
        } else {
            if (newList.size > 1) {
                newList.remove(year)
                val dataMap = _trends.value as MutableMap<Int, List<Pair<String, Float>>>
                dataMap.remove(year + 1)
                _trends.value = dataMap
            } else {
                Toast.makeText(context, "Atleast one year must be selected", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        _selectedYears.value = newList

        getDataByYear()
    }

    // Function to select a mandi from chip (only affects graph)
    fun selectMandi(mandi: String, mandiClean: String, check: Boolean) {
        val newList = _selectedMandis.value!!
        val newListClean = _selectedMandisClean.value!!

        if (check) {
            if (!newList.contains(mandi)) {
                newList.add(mandi)
                newListClean.add(mandiClean)
            }
        } else {
            if (newList.size > 1) {
                newList.remove(mandi)
                newListClean.remove(mandiClean)
            } else {
                Toast.makeText(context, "Atleast one mandi must be selected", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        Log.d("debugging $mandi", newList.toString())
        _selectedMandis.value = newList
        _selectedMandisClean.value = newListClean

        getDataByMandi()
    }

    // Function to change mandi
    fun changeMandi(mandi: String) {
        _mandi.value = mandi
        getDataByYear()
        for (year in _selectedYears.value!!) {
            getTrends(year)
        }
    }

    // Function to change year
    fun changeYear(year: Int) {
        _year.value = year
        getDataByMandi()
    }

    // Function to check if a year selected is already in selected years list
    fun checkYear(year: Int): Boolean {
        if (_selectedYears.value!!.contains(year)) {
            return true
        }
        return false
    }

    // Function to check if a mandi selected is already in selected mandis list
    fun checkMandi(mandi: String): Boolean {
        if (_selectedMandis.value!!.contains(mandi)) {
            return true
        }
        return false
    }

}