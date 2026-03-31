package com.civicguard.ui.citizen

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.civicguard.worker.UploadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class SubmissionState {
    object Idle : SubmissionState()
    object Submitting : SubmissionState()
    object Success : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

@HiltViewModel
class SubmitComplaintViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val state: StateFlow<SubmissionState> = _state.asStateFlow()

    fun submitComplaint(
        title: String,
        description: String,
        category: String,
        imageUri: Uri,
        lat: Double,
        lng: Double,
        pincode: String,
        city: String,
        state: String
    ) {
        _state.value = SubmissionState.Submitting
        
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("description", description)
            .putString("category", category)
            .putString("imageUri", imageUri.toString())
            .putDouble("latitude", lat)
            .putDouble("longitude", lng)
            .putString("pincode", pincode)
            .putString("city", city)
            .putString("state", state)
            .build()
            
        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(inputData)
            .build()
            
        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
        
        _state.value = SubmissionState.Success
    }
}
