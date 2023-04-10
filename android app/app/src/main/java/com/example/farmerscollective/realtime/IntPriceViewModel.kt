package com.example.farmerscollective.realtime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.farmerscollective.data.PriceDatabase
import com.example.farmerscollective.utils.Utils.Companion.countryList

class IntPriceViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PriceDatabase.getDatabase(application).intPriceDao()
    var prices = dao.getAllPrices()

    private val _country = MutableLiveData<Int>(0)
    val country: LiveData<Int>
    get() = _country

    fun changeCountry(index: Int) {
        _country.value = index
        prices = dao.getPricesByCountry(countryList[index])
    }

}