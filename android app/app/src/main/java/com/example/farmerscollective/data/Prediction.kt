package com.example.farmerscollective.data

data class Prediction(val date: String, val confidence: Float, val output: Float, val gain: Float, val loss: Float)
