package com.civicguard.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.civicguard.app.data.MockDataStore
import com.civicguard.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(MockDataStore.getUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun loginAsCitizen(name: String) {
        MockDataStore.setUser(name = name, role = "citizen")
        _currentUser.value = MockDataStore.getUser()
    }

    fun loginAsOfficial(name: String) {
        // Defaults to mock official data for simplicity
        MockDataStore.setUser(
            name = name, 
            role = "official", 
            email = "official@civicguard.gov.in", 
            department = "Public Works", 
            designation = "City Engineer"
        )
        _currentUser.value = MockDataStore.getUser()
    }

    fun logout() {
        MockDataStore.logout()
        _currentUser.value = null
    }
}
