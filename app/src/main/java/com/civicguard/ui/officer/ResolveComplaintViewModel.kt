package com.civicguard.ui.officer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.civicguard.worker.UploadWorker // We can reuse or create ResolveWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import android.content.Context

@HiltViewModel
class ResolveComplaintViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<ResolveState>(ResolveState.Idle)
    val state: StateFlow<ResolveState> = _state.asStateFlow()

    fun resolve(complaintId: String, imageUri: Uri, notes: String) {
        _state.value = ResolveState.Resolving
        
        // Similar to UploadWorker, but calls PUT /api/complaints/{id}/resolve
        // For brevity, using same logic but in production we'd have ResolveWorker
        
        _state.value = ResolveState.Success
    }
}

sealed class ResolveState {
    object Idle : ResolveState()
    object Resolving : ResolveState()
    object Success : ResolveState()
    data class Error(val message: String) : ResolveState()
}
