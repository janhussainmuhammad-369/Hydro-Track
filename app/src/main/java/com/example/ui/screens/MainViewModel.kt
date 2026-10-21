package com.example.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.repository.WaterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel(
    private val repository: WaterRepository,
    private val currentUserId: String,
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String get() = dateFormat.format(Date())

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Observe user profile
    val userProfile: StateFlow<UiState<UserProfile>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile>> { profile ->
            UiState.Success(
                profile ?: UserProfile(
                    userId = currentUserId,
                    dailyGoalMl = 2000L,
                    unit = "ml",
                    reminderEnabled = true,
                    reminderIntervalMinutes = 60L,
                    reminderStartTime = "08:00",
                    reminderEndTime = "22:00"
                )
            )
        }
        .catch { error ->
            Log.w(TAG, "Error observing profile", error)
            emit(UiState.Error(error.message ?: "Failed to observe profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Observe water logs
    val waterLogs: StateFlow<UiState<List<WaterLog>>> = repository.observeWaterLogs(currentUserId)
        .map<List<WaterLog>, UiState<List<WaterLog>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing logs", error)
            emit(UiState.Error(error.message ?: "Failed to observe water logs"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Today's logs derived from waterLogs
    val todayLogs: StateFlow<List<WaterLog>> = waterLogs.map { state ->
        if (state is UiState.Success) {
            val todayStr = todayDateString
            state.data.filter { it.dateString == todayStr }
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // Today's total intake in ml
    val todayTotalMl: StateFlow<Long> = todayLogs.map { logs ->
        logs.sumOf { it.amountMl }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = 0L
    )

    // Weekly day-by-day intake map (Last 7 days: "yyyy-MM-dd" -> total ml)
    val past7DaysData: StateFlow<List<DayIntake>> = waterLogs.map { state ->
        val logs = if (state is UiState.Success) state.data else emptyList()
        val calendar = Calendar.getInstance()
        val result = mutableListOf<DayIntake>()

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = dateFormat.format(cal.time)
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
            val totalMl = logs.filter { it.dateString == dateStr }.sumOf { it.amountMl }
            result.add(DayIntake(dateString = dateStr, dayLabel = dayName, totalMl = totalMl))
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // Current Streak calculation
    val streakDays: StateFlow<Int> = combine(waterLogs, userProfile) { logsState, profileState ->
        val logs = (logsState as? UiState.Success)?.data ?: emptyList()
        val goal = (profileState as? UiState.Success)?.data?.dailyGoalMl ?: 2000L

        var streak = 0
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)
        val todaySum = logs.filter { it.dateString == todayStr }.sumOf { it.amountMl }

        // If today's goal reached, count today and check previous days
        var checkCal = Calendar.getInstance()
        if (todaySum >= goal) {
            streak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check if streak was active up to yesterday
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        for (i in 0..60) {
            val prevDate = dateFormat.format(checkCal.time)
            val daySum = logs.filter { it.dateString == prevDate }.sumOf { it.amountMl }
            if (daySum >= goal) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        streak
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = 0
    )

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun quickLog(amountMl: Long, beverageType: String = "Water", container: String = "Glass") {
        viewModelScope.launch {
            val result = repository.addWaterLog(
                userId = currentUserId,
                amountMl = amountMl,
                beverageType = beverageType,
                container = container,
                dateString = todayDateString
            )
            if (result.isSuccess) {
                _userMessage.value = "Added +$amountMl ml $beverageType!"
            } else {
                _userMessage.value = "Failed to add intake log."
            }
        }
    }

    fun deleteLog(logId: String) {
        viewModelScope.launch {
            val result = repository.deleteWaterLog(currentUserId, logId)
            if (result.isSuccess) {
                _userMessage.value = "Intake entry deleted."
            } else {
                _userMessage.value = "Could not delete entry."
            }
        }
    }

    fun updateGoal(newGoalMl: Long) {
        viewModelScope.launch {
            val current = (userProfile.value as? UiState.Success)?.data ?: UserProfile(userId = currentUserId)
            val updated = current.copy(dailyGoalMl = newGoalMl)
            val result = repository.saveUserProfile(updated)
            if (result.isSuccess) {
                _userMessage.value = "Daily target updated to ${newGoalMl}ml!"
            }
        }
    }

    fun updateReminders(enabled: Boolean, intervalMinutes: Long, startTime: String, endTime: String) {
        viewModelScope.launch {
            val current = (userProfile.value as? UiState.Success)?.data ?: UserProfile(userId = currentUserId)
            val updated = current.copy(
                reminderEnabled = enabled,
                reminderIntervalMinutes = intervalMinutes,
                reminderStartTime = startTime,
                reminderEndTime = endTime
            )
            val result = repository.saveUserProfile(updated)
            if (result.isSuccess) {
                _userMessage.value = if (enabled) "Reminders active every ${intervalMinutes}m" else "Reminders paused"
            }
        }
    }

    fun updateUnit(unit: String) {
        viewModelScope.launch {
            val current = (userProfile.value as? UiState.Success)?.data ?: UserProfile(userId = currentUserId)
            repository.saveUserProfile(current.copy(unit = unit))
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}

data class DayIntake(
    val dateString: String,
    val dayLabel: String,
    val totalMl: Long
)
