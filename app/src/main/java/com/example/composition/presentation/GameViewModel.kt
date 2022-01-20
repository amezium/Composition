package com.example.composition.presentation

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.composition.R
import com.example.composition.data.GameRepositoryImpl
import com.example.composition.domain.entity.GameResult
import com.example.composition.domain.entity.GameSettings
import com.example.composition.domain.entity.Level
import com.example.composition.domain.entity.Question
import com.example.composition.domain.usecases.GenerateQuestionUseCase
import com.example.composition.domain.usecases.GetGameSettingsUseCases

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var gameSettings: GameSettings
    private lateinit var level: Level

    private val context = application

    private val _formattedTime = MutableLiveData<String>()
    val formattedTime: LiveData<String>
        get() = _formattedTime


    private val _question = MutableLiveData<Question>()
    val question: LiveData<Question>
        get() = _question

    private var countOfRightAnswer = 0
    private var countOfQuestion = 0

    private val repository = GameRepositoryImpl

    private val _percentOgRightsAnswer = MutableLiveData<Int>()
    val percentOfRightsAnswer: LiveData<Int>
        get() = _percentOgRightsAnswer

    private val _progressAnswer = MutableLiveData<String>()
    val progressAnswer: LiveData<String>
        get() = _progressAnswer

    private val _enoughCount = MutableLiveData<Boolean>()
    val enoughCount: LiveData<Boolean>
        get() = _enoughCount

    private val _enoughPercent = MutableLiveData<Boolean>()
    val enoughPercent: LiveData<Boolean>
        get() = _enoughPercent

    private val _minPercent = MutableLiveData<Int>()
    val minPercent: LiveData<Int>
        get() = _minPercent

    private val _gameResult = MutableLiveData<GameResult>()
    val gameResult: LiveData<GameResult>
        get() = _gameResult


    private val generateQuestionUseCase = GenerateQuestionUseCase(repository)
    private val getGameSettingsUseCases = GetGameSettingsUseCases(repository)

    private var timer: CountDownTimer? = null

    fun startGame(level: Level) {
        getGamesSettings(level)
        startTimer()
        updateProgress()
        generateQuestion()
    }

    fun chooseAnswer(number: Int) {
        checkAnswer(number)
        updateProgress()
        generateQuestion()
    }

    private fun checkAnswer(number: Int) {
        val rightAnswer = question.value?.rightAnswer
        if (number == rightAnswer) {
            countOfRightAnswer++
        }
        updateProgress()
        countOfQuestion++
    }

    private fun updateProgress() {
        val percent = calculatePercentOfRightAnswer()
        _percentOgRightsAnswer.value = percent
        _progressAnswer.value = String.format(
            context.getString(R.string.progress_answers),
            countOfRightAnswer, gameSettings.minCountRightAnswers
        )
        _enoughCount.value = countOfRightAnswer >= gameSettings.minCountRightAnswers
        _enoughPercent.value = percent >= gameSettings.minPercentRightAnswers
    }

    private fun calculatePercentOfRightAnswer(): Int {
        if (countOfQuestion == 0){
            return 0
        }
        return ((countOfRightAnswer / countOfQuestion.toDouble()) * 100).toInt()
    }

    private fun getGamesSettings(level: Level) {
        this.level = level
        this.gameSettings = getGameSettingsUseCases(level)
        _minPercent.value = gameSettings.minPercentRightAnswers
    }

    private fun startTimer() {
        timer = object : CountDownTimer(
            gameSettings.gameTimeInSeconds * MILLIS_IN_SECONDS, MILLIS_IN_SECONDS
        ) {
            override fun onTick(p0: Long) {
                _formattedTime.value = formattedTime(p0)
            }

            override fun onFinish() {
                finishGame()
            }
        }
        timer?.start()
    }

    private fun generateQuestion() {

        _question.value = generateQuestionUseCase(gameSettings.maxSumValue)
    }

    private fun formattedTime(countMillis: Long): String {
        val seconds = countMillis / MILLIS_IN_SECONDS
        val minutes = seconds / SECONDS_IN_MINUTES
        val leftSeconds = seconds - (minutes * SECONDS_IN_MINUTES)
        return String.format("%02d:%02d", minutes, leftSeconds)
    }


    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }

    private fun finishGame() {
        _gameResult.value = GameResult(
            enoughCount.value == true && enoughPercent.value == true,
            countOfRightAnswer,
            countOfQuestion,
            gameSettings
        )
    }

    companion object {
        const val MILLIS_IN_SECONDS = 1000L
        const val SECONDS_IN_MINUTES = 60
    }
}