package com.civicguard.ui.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civicguard.data.remote.dto.ComplaintResponse
import com.civicguard.data.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitizenDashboardViewModel @Inject constructor(
    private val repository: ComplaintRepository
) : ViewModel() {

    private val _complaints = MutableStateFlow<List<ComplaintResponse>>(emptyList())
    val complaints: StateFlow<List<ComplaintResponse>> = _complaints.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        observeComplaints()
        refresh()
    }

    private fun observeComplaints() {
        viewModelScope.launch {
            repository.complaints
                .catch { /* Handle error */ }
                .collect { list ->
                    _complaints.value = list
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshComplaints()
            _isRefreshing.value = false
        }
    }
}
