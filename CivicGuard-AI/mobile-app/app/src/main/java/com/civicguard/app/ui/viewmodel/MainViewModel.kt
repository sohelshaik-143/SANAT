package com.civicguard.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.civicguard.app.data.MockDataStore
import com.civicguard.app.model.Complaint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _complaints = MutableStateFlow<List<Complaint>>(MockDataStore.getComplaints())
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    fun refreshComplaints() {
        _complaints.value = MockDataStore.getComplaints()
    }

    fun addComplaint(type: String, location: String, lat: Double, lng: Double, description: String, reporter: String, imageUrl: String?) {
        val id = "C-2026-${(1000..9999).random()}"
        val complaint = Complaint(
            id = id,
            type = type,
            location = location,
            lat = lat,
            lng = lng,
            status = "Pending",
            date = "Mar 28, 2026 10:00 AM", // Mocked date
            description = description,
            reporter = reporter,
            department = "General Triage",
            aiConfidence = 85,
            imageUrl = imageUrl
        )
        MockDataStore.addComplaint(complaint)
        refreshComplaints()
    }

    fun updateStatus(id: String, status: String) {
        MockDataStore.updateComplaintStatus(id, status)
        refreshComplaints()
    }
}
