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
    private val _dateSelect = MutableLiveData<ArrayList<OdkSubmission?>>(arrayListOf())
    private val _filter = MutableLiveData(0)
    private val _list = MutableLiveData<MutableMap<LocalDate, ArrayList<OdkSubmission?>>>(
        mutableMapOf()
    )

    private val _view = MutableLiveData(0)
    private val _crop = MutableLiveData(0)

    val list: LiveData<MutableMap<LocalDate, ArrayList<OdkSubmission?>>>
    get() = _list

    val dateSelect: LiveData<ArrayList<OdkSubmission?>>
    get() = _dateSelect

    val filter: LiveData<Int>
    get() = _filter

    val view: LiveData<Int>
    get() = _view

    val crop: LiveData<Int>
    get() = _crop

    init {
        loadList()
    }

    private fun condition(odk: OdkSubmission): Boolean {
        val today = LocalDate.now()
        if(odk.date == null) return false
        return when(_filter.value) {
            0 -> today.minusDays(7).isBefore(odk.date)
            1 -> today.minusDays(28).isBefore(odk.date)
            else -> true
        }
    }

    fun loadList() {
        val value = mutableMapOf<LocalDate, ArrayList<OdkSubmission?>>()

        if(context.fileList().isNotEmpty()) {
            val file = File(context.filesDir, "TELANGANA_ADILABAD_ODK.csv")

            if(file.exists()) {
                csvReader().open(file) {
                    readAllAsSequence().forEachIndexed { i, it ->
                        Log.i("TAG", it.toString())
                        val dt = LocalDate.parse(it[6])
                        val odk = OdkSubmission(
                            it[0].toInt(),
                            it[1]?.toInt(),
                            it[2],
                            it[3]?.toInt(),
                            it[4],
                            it[5].toLong(),
                            dt
                        )

                        if(condition(odk) && odk.cropId == crop.value?.plus(1)) {
                            if(!value.containsKey(dt)) value[dt] = arrayListOf()
                            value[dt]!!.add(odk)
                        }
                    }
                }
            }
        }
        Log.d(this.toString(), value.toString())
        _list.value = value
    }

    fun filter(selection: Int) {
        _filter.value = selection
        loadList()
    }

    fun chooseCrop(selection: Int) {
        _crop.value = selection
        loadList()
    }

    fun view(s: Int) {
        _view.value = s
    }

    fun selectSubmission(date: LocalDate) {
        _dateSelect.value = _list.value!![date]
    }

}