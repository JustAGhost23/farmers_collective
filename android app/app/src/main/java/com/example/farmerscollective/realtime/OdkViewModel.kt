package com.example.farmerscollective.realtime

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.farmerscollective.data.OdkSubmission
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.time.LocalDate

class OdkViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val _filter = MutableLiveData(2)
    private val _list = MutableLiveData<MutableMap<LocalDate, ArrayList<OdkSubmission?>>>(
        mutableMapOf()
    )
    private val _crop = MutableLiveData(0)
    private val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1)))
        LocalDate.now().year - 1
    else LocalDate.now().year
    private val _year = MutableLiveData(current)

    val list: LiveData<MutableMap<LocalDate, ArrayList<OdkSubmission?>>>
        get() = _list

    val filter: LiveData<Int>
        get() = _filter

    val crop: LiveData<Int>
        get() = _crop

    init {
        loadList()
    }

    private fun condition(odk: OdkSubmission): Boolean {
        val today = LocalDate.now()
        if (odk.date == null) return false
        return when (_filter.value) {
            0 -> today.minusDays(7).isBefore(odk.date)
            1 -> today.minusDays(28).isBefore(odk.date)
            else -> true
        }
    }

    private fun loadList() {
        val value = mutableMapOf<LocalDate, ArrayList<OdkSubmission?>>()

        if (context.fileList().isNotEmpty()) {
            val file = File(context.filesDir, "TELANGANA_ADILABAD_ODK.csv")

            if (file.exists()) {
                csvReader().open(file) {
                    readAllAsSequence().forEachIndexed { i, it ->
                        val dt = LocalDate.parse(it[6])
                        val odk = OdkSubmission(
                            it[0].toInt(),
                            it[1].toInt(),
                            it[2],
                            it[3].toInt(),
                            it[4],
                            it[5].toLong(),
                            dt
                        )
                        if (condition(odk) && odk.cropId == crop.value?.plus(1)) {
                            if (dt.year == _year.value && dt.month.value > 6) {
                                if (!value.containsKey(dt)) value[dt] = arrayListOf()
                                value[dt]!!.add(odk)
                            } else if (dt.year == _year.value?.plus(1) && dt.month.value < 7) {
                                if (!value.containsKey(dt)) value[dt] = arrayListOf()
                                value[dt]!!.add(odk)
                            }
                        }
                    }
                }
            }
        }
        Log.e(this.toString(), value.toString())
        _list.value = value
    }

//    fun filter(selection: Int) {
//        _filter.value = selection
//        loadList()
//    }

    fun chooseCrop(selection: Int) {
        _crop.value = selection
        loadList()
    }

    fun changeYear(year: Int) {
        _year.value = year

    }

}