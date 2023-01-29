package com.example.farmerscollective.data

data class Prediction(
    val date: String,
    val confidence: Float,
    val predicted: Float,
    val output: Float,
    var gain: Float,
    var loss: Float,
)
