package com.example.composition.domain.entity

import java.io.Serializable

data class GameSettings(
    val maxSumValue: Int,
    val minCountRightAnswers: Int,
    val minPercentRightAnswers: Int,
    val gameTimeInSeconds: Int
): Serializable