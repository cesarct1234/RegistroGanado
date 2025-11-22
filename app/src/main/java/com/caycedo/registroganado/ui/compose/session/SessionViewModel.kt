package com.caycedo.registroganado.ui.compose.session

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionViewModel : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _rol = MutableStateFlow<String?>(null)
    val rol: StateFlow<String?> = _rol

    fun setSession(id: String, role: String) {
        _userId.value = id
        _rol.value = role
    }

    fun clearSession() {
        _userId.value = null
        _rol.value = null
    }
}
