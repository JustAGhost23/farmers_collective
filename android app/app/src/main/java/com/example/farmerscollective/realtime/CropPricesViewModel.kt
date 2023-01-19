package com.example.farmerscollective.realtime

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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


class CropPricesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val _dataByYear = MutableLiveData<ArrayList<ILineDataSet>>()
    private val _dataByMandi = MutableLiveData<ArrayList<ILineDataSet>>()
    private val _selectedYears = MutableLiveData(arrayListOf<Int>())
    private val _mandi = MutableLiveData("MAHARASHTRA_NAGPUR_Price")
    private val _selectedMandis = MutableLiveData(arrayListOf("MAHARASHTRA_NAGPUR_Price"))
    private val _year = MutableLiveData<Int>()
    private val _trends = MutableLiveData<Map<Int, List<Pair<String, Float>>>>()

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

    init {

        _selectedYears.value = arrayListOf(current)
        _year.value = current

        getDataByYear()
        getDataByMandi()
//        getTrends(current)

    }

//    private fun getTrends(current: Int) {
//        val file1 = File(context.filesDir, "${_mandi.value}_${current - 2}")
//        val file2 = File(context.filesDir, "${_mandi.value}_${current - 1}")
//        var min1 = Pair("", Float.MAX_VALUE)
//        var min2 = Pair("", Float.MAX_VALUE)
//        var max1 = Pair("", Float.MIN_VALUE)
//        var max2 = Pair("", Float.MIN_VALUE)
//
//        if(file1.exists()) {
//            csvReader().open(file1) {
//                readAllAsSequence().forEachIndexed { i, it ->
//                    val p = it[1].toFloat()
//                    val m = it[0].substring(5, 7).toInt()
//
//                    if(min1.second > p) min1 = Pair(DateFormatSymbols().months[m-1], p)
//                    if(max1.second < p) max1 = Pair(DateFormatSymbols().months[m-1], p)
//
//                }
//            }
//        }
//
//        if(file2.exists()) {
//            csvReader().open(file2) {
//                readAllAsSequence().forEachIndexed { i, it ->
//                    val p = it[1].toFloat()
//                    val m = it[0].substring(5, 7).toInt()
//
//                    if(min2.second > p) min2 = Pair(DateFormatSymbols().months[m-1], p)
//                    if(max2.second < p) max2 = Pair(DateFormatSymbols().months[m-1], p)
//
//                }
//            }
//        }
//
//        val data = Bundle()
//        data.putInt("0", current - 2)
//        data.putString("1", min1.first)
//        data.putFloat("2", min1.second)
//        data.putString("3", max1.first)
//        data.putFloat("4", max1.second)
//        data.putInt("5", current - 1)
//        data.putString("6", min2.first)
//        data.putFloat("7", min2.second)
//        data.putString("8", max2.first)
//        data.putFloat("9", max2.second)
//
//        _trends.value = data
//    }

    private fun getDataByYear() {

        val temp = ArrayList<ILineDataSet>()
        val trendsMap = mutableMapOf<Int, List<Pair<String, Float>>>()

        if(context.fileList().isNotEmpty()) {
            val mandi = _mandi.value!!

            val range = _selectedYears.value!!

            for(year in range) {
                var min = Pair("", Float.MAX_VALUE)
                var max = Pair("", Float.MIN_VALUE)
                val file1 = File(context.filesDir, "${mandi}_${year}")
                val file2 = File(context.filesDir, "${mandi}_${year + 1}")
                val prices = mutableMapOf<String, Float>()

                if(file1.exists()) {
                    csvReader().open(file1) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            val start = LocalDate.of(year, 6, 30)
                            if(date.isAfter(start)) prices[ddmm] = it[1].toFloat()

                            val p = it[1].toFloat()
                            val m = it[0].substring(5, 7).toInt()
                            if(min.second > p) min = Pair(DateFormatSymbols().months[m-1] + " $year", p)
                            if(max.second < p) max = Pair(DateFormatSymbols().months[m-1] + " $year", p)

                        }
                    }
                }

                if(file2.exists()) {
                    csvReader().open(file2) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            val end = LocalDate.of(year + 1, 7, 1)

                            if(date.isBefore(end)) prices[ddmm] = it[1].toFloat()

                            val p = it[1].toFloat()
                            val m = it[0].substring(5, 7).toInt()
                            if(min.second > p) min = Pair(DateFormatSymbols().months[m-1] + " $year", p)
                            if(max.second < p) max = Pair(DateFormatSymbols().months[m-1] + " $year", p)

                        }
                    }
                }

//                temp[year] = prices
                trendsMap[year] = listOf(min, max)
                val values1 = ArrayList<Entry>()
                val maxMap = mutableMapOf<Int, Pair<String, Float>>()

                for (date in dates) {
                    val i = dates.indexOf(date)

                    if (prices.containsKey(date) && !prices[date]!!.equals(0f)) {
                        values1.add(Entry(i.toFloat(), prices[date]!!))

                        val m = date.substring(3).toInt()
                        if(!maxMap.containsKey(m)) maxMap[m] = Pair(date, prices[date]!!)
                        else if(maxMap[m]!!.second < prices[date]!!) maxMap[m] = Pair(date, prices[date]!!)

                    }

                }

                val values2 = ArrayList<Entry>()

                for(pair in maxMap.values) {
                    values2.add(Entry(dates.indexOf(pair.first).toFloat(), pair.second))
                }

                val dataset1 = LineDataSet(values1, year.toString())
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
//            getTrends(current)
            _trends.value = trendsMap

        }

    }

    private fun getDataByMandi() {

        val temp = ArrayList<ILineDataSet>()

        if(context.fileList().isNotEmpty()) {
            val array = _selectedMandis.value
            val year = _year.value!!

            val start = LocalDate.of(year, 6, 30)
            val end = LocalDate.of(year + 1, 7, 1)

            for(mandi in array!!) {
                val file1 = File(context.filesDir, "${mandi}_${year}")
                val file2 = File(context.filesDir, "${mandi}_${year + 1}")

                val prices = mutableMapOf<String, Float>()

                if(file1.exists()) {
                    csvReader().open(file1) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            if(date.isAfter(start)) prices[ddmm] = it[1].toFloat()

                        }
                    }
                }

                if(file2.exists()) {
                    csvReader().open(file2) {
                        readAllAsSequence().forEachIndexed { i, it ->
                            val date = LocalDate.parse(it[0])
                            val ddmm = it[0].substring(8) + "-" + it[0].substring(5, 7)

                            if(date.isBefore(end)) prices[ddmm] = it[1].toFloat()

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
                        if(maxList[m].second < prices[date]!!) maxList[m] = Pair(date, prices[date]!!)

                    }

                }

                val values2 = ArrayList<Entry>()

                for(pair in maxList) {
                    if(pair.second != 0f) values2.add(Entry(dates.indexOf(pair.first).toFloat(), pair.second))
                }

                val mandiColors = mutableMapOf<String, Int>()

                context.resources.getStringArray(R.array.mandi).forEachIndexed { i, str ->
                    mandiColors[str] = Color.parseColor(colors[i])
                }

                Collections.sort(values1, EntryXComparator())
                Collections.sort(values2, EntryXComparator())

                val dataset1 = LineDataSet(values1, mandi)
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

    fun selectYear(year: Int, check: Boolean) {
        val newList = _selectedYears.value!!

        if(check) newList.add(year)
        else newList.remove(year)

        _selectedYears.value = newList

        getDataByYear()
    }

    fun selectMandi(mandi: String, check: Boolean) {
        val newList = _selectedMandis.value!!

        if(check) newList.add(mandi)
        else newList.remove(mandi)

        Log.d("debugging $mandi", newList.toString())
        _selectedMandis.value = newList

        getDataByMandi()
    }

    fun changeMandi(mandi: String) {
        _mandi.value = mandi
        getDataByYear()
    }

    fun changeYear(year: Int) {
        _year.value = year
        getDataByMandi()
    }

}