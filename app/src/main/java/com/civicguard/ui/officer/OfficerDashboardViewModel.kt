package com.civicguard.ui.officer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civicguard.data.remote.ComplaintApi
import com.civicguard.data.remote.dto.ComplaintResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfficerDashboardViewModel @Inject constructor(
    private val api: ComplaintApi
) : ViewModel() {

    private val _assignedComplaints = MutableStateFlow<List<ComplaintResponse>>(emptyList())
    val assignedComplaints: StateFlow<List<ComplaintResponse>> = _assignedComplaints.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchAssigned()
    }

    fun fetchAssigned() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Call /api/complaints/assigned (Role-based access handled by JWT)
                // Simplified for now: Assume results come in properly
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }
}
