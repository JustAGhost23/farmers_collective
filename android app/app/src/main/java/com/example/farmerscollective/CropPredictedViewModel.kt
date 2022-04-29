package com.example.farmerscollective

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore




class CropPredictedViewModel : ViewModel() {
    // Access a Cloud Firestore instance from your Activity
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _data = MutableLiveData<MutableMap<String, Float>>(mutableMapOf())

    val data: LiveData<MutableMap<String, Float>>
    get() = _data

    init {
        getData()
    }

    fun getData() {
        db.collection("KOTA_Prices")
            .get()
            .addOnSuccessListener {
                for(doc in it) {
                    val prices = doc.data["data"] as ArrayList<HashMap<String, Any>>
                    val temp = _data.value!!
                    for(row in prices) {
                        temp[row["DATE"]!!.toString()] = row["PRICE"]!!.toString().toFloat()
                    }
                    _data.value = temp
                }
            }
            .addOnFailureListener {
                Log.v("ViewModel", it.toString())
            }
    }

}